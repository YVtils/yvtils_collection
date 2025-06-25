package yv.tils.discord.logic.whitelist

import apis.MojangAPI
import apis.MojangAPI.ErrorResponse
import apis.MojangAPI.SuccessfulResponse
import coroutine.CoroutineHandler
import data.Data
import language.LanguageHandler
import logger.Logger
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.AppLogic
import yv.tils.discord.utils.DiscordUser
import java.util.concurrent.TimeUnit

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
                        WhitelistEmbeds().accountChangePromptEmbed(
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

                    Logger.info(
                        LanguageHandler.getMessage(
                            RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ADDED.key,
                            mapOf(
                                "discordAccount" to DiscordUser.parseIDToName(userID),
                                "minecraftAccount" to name,
                                "user" to e.author.effectiveName,
                            )
                        )
                    )
                } catch (ex: Exception) {
                    when (ex) {
                        is AlreadyWhitelistedException -> {
                            channel.sendMessageEmbeds(
                                WhitelistEmbeds().accountAlreadyListedEmbed(name).build()
                            ).complete().delete().queueAfter(15, TimeUnit.SECONDS)

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
                            channel.sendMessageEmbeds(
                                WhitelistEmbeds().invalidAccountEmbed(name).build()
                            ).complete().delete().queueAfter(15, TimeUnit.SECONDS)

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
                            channel.sendMessageEmbeds(
                                WhitelistEmbeds().accountErrorEmbed(e.message ?: "-").build()
                            ).complete().delete().queueAfter(15, TimeUnit.SECONDS)

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
     * @throws InvalidAccountException if the account is not found or cannot be unlinked.
     * @throws Exception for any other errors that occur during the unlinking process.
     * @return The WhitelistEntry that was unlinked, or null if not found.
     */
    fun unlinkAccount(userID: String, guildID: String? = null): WhitelistEntry {
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

        return removedEntry
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
    }
}
