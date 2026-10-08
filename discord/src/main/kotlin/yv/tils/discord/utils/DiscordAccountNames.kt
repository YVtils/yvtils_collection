package yv.tils.discord.utils

import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic
import yv.tils.utils.logger.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/** REST requests are asynchronous and shared between inventory entries and command inspection. */
object DiscordAccountNames {
    private data class Cached(val future: CompletableFuture<String?>, val created: Long)
    private val names = ConcurrentHashMap<String, Cached>()

    fun cached(id: String): String? {
        if (!id.matches(Regex("[0-9]{17,20}"))) return null
        return runCatching {
            val jda = AppLogic.getJDA()
            val guild = ConfigFile.getValueAsString("mainGuild").orEmpty()
            jda.getGuildById(guild)?.getMemberById(id)?.effectiveName
                ?: names["$guild:$id"]?.future?.getNow(null)
                ?: jda.getUserById(id)?.effectiveName
        }.getOrNull()
    }

    fun retrieve(id: String): CompletableFuture<String?> {
        if (!AppLogic.started || !id.matches(Regex("[0-9]{17,20}"))) return CompletableFuture.completedFuture(null)
        val guildID = ConfigFile.getValueAsString("mainGuild").orEmpty()
        val key = "$guildID:$id"
        val now = System.currentTimeMillis()
        names.entries.removeIf { now - it.value.created > 300000 }
        val cached = names.computeIfAbsent(key) {
            val future = CompletableFuture<String?>().completeOnTimeout(null, 15, java.util.concurrent.TimeUnit.SECONDS)
            try {
                val jda = AppLogic.getJDA()
                fun userFallback() {
                    try {
                        jda.retrieveUserById(id).queue({ future.complete(it.effectiveName) }, {
                            Logger.debug("Discord display name unavailable for $id: ${it.message}")
                            future.complete(null)
                        })
                    } catch (error: Exception) {
                        Logger.debug("Discord user lookup failed for $id: ${error.message}")
                        future.complete(null)
                    }
                }
                val guild = jda.getGuildById(guildID)
                if (guild == null) userFallback()
                else guild.retrieveMemberById(id).queue({ future.complete(it.effectiveName) }, { userFallback() })
            } catch (error: Exception) {
                Logger.debug("Discord display name lookup failed for $id: ${error.message}")
                future.complete(null)
            }
            Cached(future, now)
        }
        return cached.future
    }
}
