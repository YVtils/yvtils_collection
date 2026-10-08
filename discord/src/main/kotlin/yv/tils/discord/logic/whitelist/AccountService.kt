package yv.tils.discord.logic.whitelist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.configs.SaveFile
import yv.tils.utils.modules.Core
import yv.tils.utils.logger.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.util.UUID

/** All entry points share this lock, including replacement, persistence and role completion. */
object AccountService {
    private val transactions = AccountTransactions()

    class Failure(val reason: String) : Exception(reason)
    class RoleFailure(val entry: WhitelistEntry, cause: Throwable) : Exception("roles", cause)

    suspend fun <T> server(action: () -> T): T = withContext(Dispatchers.IO) {
        Core.instance.server.scheduler.callSyncMethod(Core.instance, java.util.concurrent.Callable { action() }).get()
    }

    private suspend fun resolve(input: String, user: String): WhitelistEntry = withContext(Dispatchers.IO) {
        val name = input.trim()
        val uuidInput = runCatching { UUID.fromString(name) }.getOrNull()
        if (uuidInput == null && !WhitelistLogic.isValidUsername(name)) throw Failure("invalid")
        val verify = ConfigFile.getValueAsBoolean("whitelistFeature.settings.checkMinecraftAccount") ?: true
        if (!verify) {
            val player = if (uuidInput == null) Core.instance.server.getOfflinePlayer(name) else server {
                Bukkit.getOfflinePlayer(uuidInput)
            }
            return@withContext WhitelistEntry(user, player.name ?: name, player.uniqueId.toString())
        }
        val endpoint = if (uuidInput == null) "https://api.minecraftservices.com/minecraft/profile/lookup/name/$name"
        else "https://api.minecraftservices.com/minecraft/profile/lookup/$uuidInput"
        val connection = URI(endpoint).toURL().openConnection() as java.net.HttpURLConnection
        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            if (connection.responseCode == 404 || connection.responseCode == 204) throw Failure("invalid")
            if (connection.responseCode != 200) throw Failure("lookup")
            val profile =
                connection.inputStream.bufferedReader().use { Json.parseToJsonElement(it.readText()).jsonObject }
            val id = profile.getValue("id").jsonPrimitive.content.replace("-", "")
            val uuid = UUID.fromString(
                "${id.substring(0, 8)}-${id.substring(8, 12)}-${id.substring(12, 16)}-${
                    id.substring(
                        16,
                        20
                    )
                }-${id.substring(20)}"
            )
            WhitelistEntry(user, profile.getValue("name").jsonPrimitive.content, uuid.toString())
        } catch (failure: Failure) {
            throw failure
        } catch (failure: Exception) {
            Logger.warn("Minecraft account lookup failed: ${failure.message}")
            throw Failure("lookup")
        } finally {
            connection.disconnect()
        }
    }

    /** Validate a proposed replacement without changing access; execution rechecks under the same lock. */
    suspend fun prepareReplacement(name: String, expected: WhitelistEntry): WhitelistEntry {
        val candidate = resolve(name, expected.discordUserID)
        return transactions.exclusive {
            RegistrationPolicy.conflict(WhitelistLogic.getAllEntries(), candidate, expected)?.let { throw Failure(it) }
            if (server { Bukkit.getOfflinePlayer(UUID.fromString(candidate.minecraftUUID)).isWhitelisted }) {
                throw Failure("unlinkedWhitelist")
            }
            candidate
        }
    }

    suspend fun register(
        name: String,
        user: String,
        guild: String?,
        actor: String,
        replacing: WhitelistEntry? = null
    ): WhitelistEntry {
        val candidate = resolve(name, user)
        return transactions.exclusive {
            RegistrationPolicy.conflict(WhitelistLogic.getAllEntries(), candidate, replacing)?.let { throw Failure(it) }
            val player = server { Bukkit.getOfflinePlayer(UUID.fromString(candidate.minecraftUUID)) }
            if (server { player.isWhitelisted }) throw Failure("unlinkedWhitelist")
            val previous = replacing?.let { server { Bukkit.getOfflinePlayer(UUID.fromString(it.minecraftUUID)) } }
            val previousWhitelisted = previous?.let { server { it.isWhitelisted } } ?: false
            try {
                transactions.persist(
                    change = {
                        server {
                            player.isWhitelisted = true
                            check(player.isWhitelisted)
                            previous?.isWhitelisted = false
                            check(previous == null || !previous.isWhitelisted)
                        }
                    },
                    save = {
                        SaveFile().commit(
                            WhitelistLogic.getAllEntries().filterNot { it.discordUserID == user } + candidate)
                    },
                    rollback = {
                        server {
                            player.isWhitelisted = false; previous?.isWhitelisted = previousWhitelisted
                        }
                    })
            } catch (error: Exception) {
                Logger.error("Account transaction failed, actor=$actor: ${error.stackTraceToString()}")
                throw Failure("storage")
            }
            if (previous != null) runCatching { server { previous.player?.kick() } }.onFailure { Logger.warn("Old account kick failed: $it") }
            Logger.info("Account ${if (replacing == null) "registered" else "replaced"}: $candidate; actor=$actor")
            roles(candidate, guild, true, actor)
            candidate
        }
    }

    suspend fun remove(user: String, guild: String?, actor: String, expected: WhitelistEntry? = null): WhitelistEntry =
        transactions.exclusive {
            val entry = WhitelistLogic.getEntryByDiscordID(user) ?: throw Failure("missing")
            if (expected != null && expected != entry) throw Failure("stale")
            val player = server { Bukkit.getOfflinePlayer(UUID.fromString(entry.minecraftUUID)) }
            val wasWhitelisted = server { player.isWhitelisted }
            try {
                transactions.persist(
                    change = { server { player.isWhitelisted = false; check(!player.isWhitelisted) } },
                    save = { SaveFile().commit(WhitelistLogic.getAllEntries().filterNot { it.discordUserID == user }) },
                    rollback = { server { player.isWhitelisted = wasWhitelisted } })
            } catch (error: Exception) {
                Logger.error("Unlink failed, actor=$actor: ${error.stackTraceToString()}")
                throw Failure("storage")
            }
            runCatching { server { player.player?.kick() } }.onFailure { Logger.warn("Unlinked account kick failed: $it") }
            Logger.info("Account removed: $entry; actor=$actor")
            roles(entry, guild, false, actor)
            entry
        }

    private suspend fun roles(entry: WhitelistEntry, guild: String?, add: Boolean, actor: String) {
        if (entry.discordUserID.startsWith("~")) return
        try {
            withContext(Dispatchers.IO) {
                WhitelistLogic.changeRoles(
                    entry.discordUserID,
                    guild ?: ConfigFile.getValueAsString("mainGuild") ?: error("Guild unavailable"),
                    add
                )
            }
        } catch (error: Exception) {
            Logger.warn("Account operation completed but roles failed: $entry; actor=$actor; ${error.message}")
            throw RoleFailure(entry, error)
        }
    }
}
