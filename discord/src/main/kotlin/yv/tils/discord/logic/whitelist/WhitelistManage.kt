package yv.tils.discord.logic.whitelist

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.AccountText
import yv.tils.utils.coroutine.CoroutineHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class WhitelistManage : ListenerAdapter() {
    companion object {
        class InvalidAccountException : Exception()
        class AlreadyWhitelistedException : Exception()
        class AlreadyInCacheException : Exception()
        class NoCacheException : Exception()

        val accountReplaceCache = ConcurrentHashMap<String, String>()
        val replacementSnapshots = ConcurrentHashMap<String, WhitelistEntry>()
        @Synchronized
        fun addToCache(userID: String, minecraftName: String, canStartWith: Boolean = true) {
            if (checkInCache(userID, canStartWith)) throw AlreadyInCacheException()
            accountReplaceCache[userID] = minecraftName
            val target = if ('_' in userID) userID.substringAfter('_') else userID
            WhitelistLogic.getEntryByDiscordID(target)?.let { replacementSnapshots[userID] = it }
        }

        fun checkInCache(userID: String, canStartWith: Boolean = false) =
            accountReplaceCache.keys.any { if (canStartWith) it.startsWith(userID) else it == userID }

        fun getCacheEntryAsMap(userID: String, canStartWith: Boolean = false) =
            accountReplaceCache.filterKeys { if (canStartWith) it.startsWith(userID) else it == userID }
                .ifEmpty { throw NoCacheException() }

        fun getCacheEntry(userID: String, canStartWith: Boolean = false) =
            getCacheEntryAsMap(userID, canStartWith).values.first()

        fun removeFromCache(userID: String, canStartWith: Boolean = false) {
            getCacheEntryAsMap(
                userID,
                canStartWith
            ).keys.forEach { accountReplaceCache.remove(it); replacementSnapshots.remove(it) }
        }
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (!e.isFromGuild || e.guild.id != ConfigFile.getValueAsString("mainGuild") || e.author.isBot ||
            e.channel.id != ConfigFile.getValueAsString("whitelistFeature.channel") ||
            ConfigFile.getValueAsBoolean("whitelistFeature.settings.legacyMessageRegistration") == false
        ) return
        CoroutineHandler.launchTask(task = {
            e.message.delete().queue()
            val name = e.message.contentRaw.trim()
            val old = WhitelistLogic.getEntryByDiscordID(e.author.id)
            if (old != null && !old.minecraftName.equals(name, true)) {
                try {
                    addToCache(e.author.id, name)
                    e.channel.sendMessageComponents(
                        WhitelistComponents().accountChangePromptContainer(
                            old.minecraftName,
                            name
                        )
                    )
                        .useComponentsV2().complete().delete().queueAfter(
                            1,
                            TimeUnit.MINUTES,
                            { accountReplaceCache.remove(e.author.id); replacementSnapshots.remove(e.author.id) },
                            {})
                } catch (error: Exception) {
                    e.author.openPrivateChannel().complete().sendMessage(AccountText.error(error)).queue()
                }
                return@launchTask
            }
            val result = try {
                linkAccount(name, e.author.id, e.guild.id, e.author)
                AccountText.raw("success")
            } catch (error: Exception) {
                AccountText.error(error)
            }
            e.author.openPrivateChannel().queue({ it.sendMessage(result).queue() }, {})
        }, isOnce = true)
    }

    suspend fun linkAccount(name: String, userID: String = "~$name", guildID: String? = null, initiator: User? = null) =
        AccountService.register(name, userID, guildID, "Discord:${initiator?.id ?: "system"}")

    suspend fun unlinkAccount(userID: String, guildID: String? = null, initiator: User? = null) =
        AccountService.remove(userID, guildID, "Discord:${initiator?.id ?: "system"}")
}
