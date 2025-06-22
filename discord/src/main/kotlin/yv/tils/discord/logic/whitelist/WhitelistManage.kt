package yv.tils.discord.logic.whitelist

import apis.MojangAPI
import apis.MojangAPI.ErrorResponse
import apis.MojangAPI.SuccessfulResponse
import coroutine.CoroutineHandler
import data.Data
import logger.Logger
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.truncate

class WhitelistManage: ListenerAdapter() {
    companion object {
        val whitelistChannelID = ConfigFile.getValueAsString("whitelistFeature.channel")
        val ignoreBotMessages = ConfigFile.getValueAsBoolean("general.settings.ignoreBotMessages") ?: true
        val verifyMinecraftAccount = ConfigFile.getValueAsBoolean("whitelistFeature.settings.checkMinecraftAccount") ?: true

        class InvalidAccountException : Exception("Invalid Minecraft account")
        class AlreadyWhitelistedException : Exception("Account is already whitelisted")

        /**
         * Cache for account replacements.
         * Maps Discord user IDs to new Minecraft usernames.
         */
        val accountReplaceCache = mutableMapOf<String, String>()
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

                    accountReplaceCache[userID] = name

                    channel.sendMessageEmbeds(
                        WhitelistEmbeds().accountChangeEmbed(
                            oldName = oldName,
                            newName = name
                        ).build()
                    ).addActionRow(
                        WhitelistEmbeds().accountChangeActionRow()
                    ).complete().delete().queueAfter(1, TimeUnit.MINUTES, {
                        accountReplaceCache.remove(userID)
                    }, { /* ignore if already deleted */ })

                    return@launchTask
                }

                try {
                    linkAccount(name, userID, e.guild.id)
                    channel.sendMessageEmbeds(
                        WhitelistEmbeds().accountAddEmbed(name).build()
                    ).complete().delete().queueAfter(5, TimeUnit.SECONDS)

                    // TODO: Add console message for "xxx has whitelisted their account yyy"
                } catch (e: Exception) {
                    when (e) {
                        is AlreadyWhitelistedException -> {
                            channel.sendMessageEmbeds(
                                WhitelistEmbeds().accountAlreadyListedEmbed(name).build()
                            ).complete().delete().queueAfter(15, TimeUnit.SECONDS)

                            // TODO: Add console message for "xxx tried to whitelist their account yyy, but it is already whitelisted"
                            return@launchTask
                        }
                        is InvalidAccountException -> {
                            channel.sendMessageEmbeds(
                                WhitelistEmbeds().invalidAccountEmbed(name).build()
                            ).complete().delete().queueAfter(15, TimeUnit.SECONDS)

                            // TODO: Add console message for "xxx tried to whitelist their account yyy, but it is invalid"
                            return@launchTask
                        }
                        else -> {
                            channel.sendMessageEmbeds(
                                WhitelistEmbeds().accountErrorEmbed(e.message ?: "-").build()
                            ).complete().delete().queueAfter(15, TimeUnit.SECONDS)

                            // TODO: Add console message for "xxx tried to whitelist their account yyy, but an error occurred"
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
     * @throws InvalidAccountException if the account is not found or cannot be unlinked.
     */
    fun unlinkAccount(userID: String, guildID: String? = null) {
        try {
            val entry = WhitelistLogic.getEntryByDiscordID(userID) ?: throw InvalidAccountException()
            val player = MojangAPI().nameToOfflinePlayer(entry.minecraftName)

            WhitelistLogic.removeEntry(userID, player)
        } catch (e: Exception) {
            Logger.error("Failed to unlink account for user ID $userID: ${e.message}") // TODO: error handling...
            throw InvalidAccountException()
        }

        try {
            if (guildID != null && !userID.startsWith("~")) {
                WhitelistLogic.removeRolesFromMember(userID, guildID)
            }
        } catch (e: Exception) {
            Logger.error("Failed to remove roles from member for user ID $userID in guild $guildID: ${e.message}. The user was still unlinked, but roles could not be removed.") // TODO: error handling...
        }
    }

    /**
     * Links a Minecraft account to a Discord user.
     * @param name The Minecraft username to link.
     * @param userID The Discord user ID to link the account to (default is "~$name").
     * @throws InvalidAccountException if the Minecraft account is invalid.
     * @throws AlreadyWhitelistedException if the account is already whitelisted.
     * @throws Exception for any other errors that occur during the linking process.
     */
    fun linkAccount(name: String, userID: String = "~$name", guildID: String? = null) {
        val player = Data.instance.server.getOfflinePlayer(name)

        if (verifyMinecraftAccount) {
            try {
                val response = MojangAPI().verifyMinecraftAccount(player.uniqueId)
                when (response) {
                    is ErrorResponse -> {
                        Logger.error("Failed to verify Minecraft account: ${response.errorMessage}") // TODO: error handling...
                        throw InvalidAccountException()
                    }
                    is SuccessfulResponse -> {
                        if (response.name != name) {
                            Logger.error("Minecraft account name mismatch: expected $name, got ${response.name}") // TODO: error handling...
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
            Logger.error("Failed to add roles to member for user ID $userID in guild $guildID: ${e.message}. The user was still whitelisted, but roles could not be assigned.") // TODO: error handling...
        }
    }
}