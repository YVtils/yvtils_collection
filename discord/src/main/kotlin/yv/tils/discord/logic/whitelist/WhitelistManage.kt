/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.logic.whitelist

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.AppLogic
import yv.tils.discord.utils.DiscordUser
import yv.tils.utils.apis.MojangAPI
import yv.tils.utils.apis.MojangAPI.ErrorResponse
import yv.tils.utils.apis.MojangAPI.SuccessfulResponse
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.concurrent.TimeUnit

class WhitelistManage: ListenerAdapter() {
    companion object {
        val whitelistChannelID = ConfigFile.getValueAsString("whitelistFeature.channel")
        val ignoreBotMessages = ConfigFile.getValueAsBoolean("general.settings.ignoreBotMessages") ?: true
        val verifyMinecraftAccount =
            ConfigFile.getValueAsBoolean("whitelistFeature.settings.checkMinecraftAccount") ?: true

        class InvalidAccountException: Exception("Invalid Minecraft account")
        class AlreadyWhitelistedException: Exception("Account is already whitelisted")

        /**
         * Cache for account replacements.
         * Maps Discord user IDs to new Minecraft usernames.
         */
        val accountReplaceCache = mutableMapOf<String, String>()

        class AlreadyInCacheException: Exception("User is already in the cache")
        class NoCacheException: Exception("No cache found for the user")

        /**
         * Adds a user to the account replacement cache.
         * @param userID The Discord user ID to add.
         * @param minecraftName The new Minecraft username to associate with the user ID.
         * @param canStartWith If true, checks if any key in the cache starts with the user ID.
         *                     If false, checks for an exact match.
         * @throws AlreadyInCacheException if the user ID is already in the cache.
         */
        fun addToCache(userID: String, minecraftName: String, canStartWith: Boolean = true) {
            if (checkInCache(userID, canStartWith)) {
                throw AlreadyInCacheException()
            }
            accountReplaceCache[userID] = minecraftName
        }

        /**
         * Removes a user from the account replacement cache.
         * @param userID The Discord user ID to remove.
         * @param canStartWith If true, checks if any key in the cache starts with the user ID.
         *                     If false, checks for an exact match.
         * @throws NoCacheException if the user ID is not found in the cache.
         */
        fun removeFromCache(userID: String, canStartWith: Boolean = false) {
            if (! checkInCache(userID, canStartWith)) {
                throw NoCacheException()
            }
            accountReplaceCache.remove(userID)
        }

        /**
         * Retrieves the cached Minecraft username for a given Discord user ID.
         * @param userID The Discord user ID to retrieve the cached Minecraft username for.
         * @param canStartWith If true, checks if any key in the cache starts with the user ID.
         *                    If false, checks for an exact match.
         * @return The cached Minecraft username, or null if not found.
         * @throws NoCacheException if the user ID is not found in the cache.
         */
        fun getCacheEntry(userID: String, canStartWith: Boolean = false): String? {
            if (! checkInCache(userID, canStartWith)) {
                throw NoCacheException()
            }

            return if (canStartWith) {
                accountReplaceCache.keys.firstOrNull { it.startsWith(userID) }?.let { accountReplaceCache[it] }
            } else {
                accountReplaceCache[userID]
            }
        }

        /**
         * Checks if a user is in the account replacement cache.
         * @param userID The Discord user ID to check.
         * @param canStartWith If true, checks if any key in the cache starts with the user ID.
         *                     If false, checks for an exact match.
         * @return true if the user ID is in the cache, false otherwise.
         */
        fun checkInCache(userID: String, canStartWith: Boolean = false): Boolean {
            return if (canStartWith) {
                accountReplaceCache.keys.any { it.startsWith(userID) }
            } else {
                accountReplaceCache.containsKey(userID)
            }
        }

        /**
         * Retrieves a map of cached Minecraft usernames for a given Discord user ID.
         * @param userID The Discord user ID to retrieve the cached Minecraft usernames for.
         * @param canStartWith If true, checks if any key in the cache starts with the user ID.
         *                     If false, checks for an exact match.
         * @return A map of cached Minecraft usernames associated with the user ID.
         * @throws NoCacheException if the user ID is not found in the cache.
         */
        fun getCacheEntryAsMap(userID: String, canStartWith: Boolean = false): Map<String, String> {
            if (! checkInCache(userID, canStartWith)) {
                throw NoCacheException()
            }

            return if (canStartWith) {
                accountReplaceCache.filterKeys { it.startsWith(userID) }
            } else {
                accountReplaceCache.filterKeys { it == userID }
            }
        }
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        CoroutineHandler.launchTask(
            task = {
                if (!e.channel.type.isMessage) return@launchTask
                if (ignoreBotMessages && e.author.isBot) { return@launchTask } else { if (e.author.id == AppLogic.appID) return@launchTask }
                if (e.channelType.compareTo(ChannelType.TEXT) != 0) return@launchTask
                if (e.channel.id != whitelistChannelID) return@launchTask

                val channel = e.channel.asTextChannel()
                val userID = e.author.id

                val name = e.message.contentRaw
                val messageID = e.messageId

                channel.deleteMessageById(messageID).queue()

                if (WhitelistLogic.containsEntry(userID)) {
                    val oldName = WhitelistLogic.getEntryByDiscordID(userID)?.minecraftName ?: "Unknown"

                    try {
                        addToCache(userID, name)
                    } catch (_: AlreadyInCacheException) {
                        channel.sendMessageComponents(
                            WhitelistComponents().accountErrorContainer(
                                LanguageHandler.getRawMessage(
                                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_ALREADY_CACHED.key,
                                    params = mapOf(
                                        "user" to e.author.effectiveName
                                    )
                                )
                            )
                        ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.SECONDS)

                        Logger.info(
                            LanguageHandler.getMessage(
                                RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_ALREADY_CACHED.key,
                                mapOf(
                                    "user" to e.author.effectiveName
                                )
                            )
                        )

                        return@launchTask
                    }

                    channel.sendMessageComponents(
                        WhitelistComponents().accountChangePromptContainer(
                            oldName = oldName,
                            newName = name
                        )
                    ).useComponentsV2().complete().delete().queueAfter(1, TimeUnit.MINUTES, {
                        accountReplaceCache.remove(userID)
                    }, { /* ignore if already deleted */ })

                    return@launchTask
                }

                try {
                    linkAccount(name, userID, e.guild.id, e.author)
                    channel.sendMessageComponents(
                        WhitelistComponents().accountAddContainer(name)
                    ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.SECONDS)
                } catch (ex: Exception) {
                    when (ex) {
                        is AlreadyWhitelistedException -> {
                            channel.sendMessageComponents(
                                WhitelistComponents().accountAlreadyListedContainer(name)
                            ).useComponentsV2().complete().delete().queueAfter(15, TimeUnit.SECONDS)

                            Logger.info(
                                LanguageHandler.getMessage(
                                    RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ALREADY_LISTED.key,
                                    mapOf(
                                        "discordAccount" to DiscordUser.parseIDToName(userID),
                                        "minecraftAccount" to name,
                                        "user" to e.author.effectiveName,
                                    )
                                )
                            )

                            return@launchTask
                        }
                        is InvalidAccountException -> {
                            channel.sendMessageComponents(
                                WhitelistComponents().invalidAccountContainer(name)
                            ).useComponentsV2().complete().delete().queueAfter(15, TimeUnit.SECONDS)

                            Logger.info(
                                LanguageHandler.getMessage(
                                    RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_INVALID.key,
                                    mapOf(
                                        "discordAccount" to DiscordUser.parseIDToName(userID),
                                        "minecraftAccount" to name,
                                        "user" to e.author.effectiveName,
                                    )
                                )
                            )
                            return@launchTask
                        }
                        else -> {
                            channel.sendMessageComponents(
                                WhitelistComponents().accountErrorContainer(ex.message ?: "-")
                            ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.SECONDS)

                            Logger.warn(
                                LanguageHandler.getMessage(
                                    RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ERROR.key,
                                    mapOf(
                                        "discordAccount" to DiscordUser.parseIDToName(userID),
                                        "minecraftAccount" to name,
                                        "user" to e.author.effectiveName,
                                        "error" to (ex.message ?: "Unknown error")
                                    )
                                )
                            )

                            return@launchTask
                        }
                    }
                }
            },
            isOnce = true
        )
    }

    /**
     * Unlinks a Minecraft account from a Discord user.
     * @param userID The Discord user ID to unlink the account from.
     * @param guildID Optional Discord guild ID to remove roles from the user.
     * @param initiator The User who initiated the unlinking action, for logging purposes.
     * @throws InvalidAccountException if the account is not found or cannot be unlinked.
     * @throws Exception for any other errors that occur during the unlinking process.
     * @return The WhitelistEntry that was unlinked, or null if not found.
     */
    fun unlinkAccount(userID: String, guildID: String? = null, initiator: User? = null): WhitelistEntry {
        val removedEntry: WhitelistEntry

        try {
            val entry = WhitelistLogic.getEntryByDiscordID(userID) ?: throw InvalidAccountException()
            val player = MojangAPI().nameToOfflinePlayer(entry.minecraftName)

            WhitelistLogic.removeEntry(userID, player)

            removedEntry = entry
        } catch (e: Exception) {
            Logger.error("Failed to unlink account for user ID $userID: ${e.message}")
            throw InvalidAccountException()
        }

        try {
            if (guildID != null && !userID.startsWith("~")) {
                WhitelistLogic.removeRolesFromMember(userID, guildID)
            }
        } catch (e: Exception) {
            Logger.error("Failed to remove roles from member with user ID $userID in guild $guildID: ${e.message}. The user was still unlinked, but roles could not be removed.")
        }

        Logger.info(
            LanguageHandler.getMessage(
                RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_REMOVED.key,
                mapOf(
                    "discordAccount" to DiscordUser.parseIDToName(userID),
                    "minecraftAccount" to removedEntry.minecraftName,
                    "user" to (initiator?.effectiveName ?: "Unknown")
                )
            )
        )

        return removedEntry
    }

    /**
     * Links a Minecraft account to a Discord user.
     * @param name The Minecraft username to link.
     * @param userID The Discord user ID to link the account to (default is "~$name").
     * @param guildID Optional Discord guild ID to add roles to the user.
     * @throws InvalidAccountException if the Minecraft account is invalid.
     * @throws AlreadyWhitelistedException if the account is already whitelisted.
     * @throws Exception for any other errors that occur during the linking process.
     */
    fun linkAccount(
        name: String,
        userID: String = "~$name",
        guildID: String? = null,
        initiator: User? = null,
    ): WhitelistEntry {
        val player = Data.instance.server.getOfflinePlayer(name)

        if (verifyMinecraftAccount) {
            try {
                when (val response = MojangAPI().verifyMinecraftAccount(player.uniqueId)) {
                    is ErrorResponse -> {
                        Logger.error("Failed to verify Minecraft account: ${response.errorMessage}")
                        throw InvalidAccountException()
                    }
                    is SuccessfulResponse -> {
                        if (response.name != name) {
                            Logger.error("Minecraft account name mismatch: expected $name, got ${response.name}")
                            throw InvalidAccountException()
                        }
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }

        if (player.isWhitelisted) {
            throw AlreadyWhitelistedException()
        }

        val whitelistEntry = WhitelistEntry(
            minecraftName = name,
            minecraftUUID = player.uniqueId.toString(),
            discordUserID = userID
        )

        try {
            WhitelistLogic.addEntry(whitelistEntry, player)
        } catch (e: Exception) {
            throw e
        }

        try {
            if (guildID != null && !userID.startsWith("~")) {
                WhitelistLogic.addRolesToMember(userID, guildID)
            }
        } catch (e: Exception) {
            Logger.error("Failed to add roles to member with user ID $userID in guild $guildID: ${e.message}. The user was still whitelisted, but roles could not be assigned.")
        }

        Logger.info(
            LanguageHandler.getMessage(
                RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ADDED.key,
                mapOf(
                    "discordAccount" to DiscordUser.parseIDToName(userID),
                    "minecraftAccount" to name,
                    "user" to (initiator?.effectiveName ?: "Unknown")
                )
            )
        )

        return whitelistEntry
    }
}
