package yv.tils.discord.actions.commands.handler

import coroutine.CoroutineHandler
import language.LanguageHandler
import logger.Logger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.*
import net.dv8tion.jda.api.interactions.components.ActionRow
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.whitelist.*
import yv.tils.discord.logic.whitelist.WhitelistManage.Companion.AlreadyWhitelistedException
import yv.tils.discord.logic.whitelist.WhitelistManage.Companion.InvalidAccountException
import yv.tils.discord.logic.whitelist.WhitelistManage.Companion.accountReplaceCache
import yv.tils.discord.utils.DiscordUser

class JDAWhitelist {
    companion object {
        val cmdPermission = ConfigFile.getValueAsString("command.whitelistCommand.permission") ?: "MANAGE_CHANNEL"
    }

    private fun handleForceAdd(e: SlashCommandInteractionEvent) {
        val minecraftName = e.getOption("minecraft_name")?.asString ?: return
        val discordUserID = e.getOption("discord_user")?.asUser?.id ?: "~$minecraftName"
        val guildID: String = e.guild?.id ?: return

        CoroutineHandler.launchTask(
            task = {
                if (WhitelistLogic.containsEntry(discordUserID)) {
                    val oldName = WhitelistLogic.getEntryByDiscordID(discordUserID)?.minecraftName ?: "Unknown"

                    accountReplaceCache[discordUserID] = minecraftName

                    e.replyEmbeds(
                        WhitelistEmbeds().accountChangePromptEmbed(
                            oldName = oldName,
                            newName = minecraftName
                        ).build()
                    ).addActionRow(
                        WhitelistEmbeds().accountChangeActionRow()
                    ).setEphemeral(true).queue()

                    return@launchTask
                }

                try {
                    WhitelistManage().linkAccount(minecraftName, discordUserID, guildID)

                    e.replyEmbeds(
                        WhitelistEmbeds().accountAddEmbed(minecraftName).build()
                    ).setEphemeral(true).queue()

                    Logger.info(
                        LanguageHandler.getMessage(
                            RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ADDED.key,
                            mapOf(
                                "discordAccount" to DiscordUser.parseIDToName(discordUserID),
                                "minecraftAccount" to minecraftName,
                                "user" to e.user.effectiveName,
                            )
                        )
                    )
                } catch (ex: Exception) {
                    when (ex) {
                        is AlreadyWhitelistedException -> {
                            e.replyEmbeds(
                                WhitelistEmbeds().accountAlreadyListedEmbed(minecraftName).build()
                            ).setEphemeral(true).queue()

                            Logger.info(
                                LanguageHandler.getMessage(
                                    RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ALREADY_LISTED.key,
                                    mapOf(
                                        "discordAccount" to DiscordUser.parseIDToName(discordUserID),
                                        "minecraftAccount" to minecraftName,
                                        "user" to e.user.effectiveName,
                                    )
                                )
                            )
                            return@launchTask
                        }
                        is InvalidAccountException -> {
                            e.replyEmbeds(
                                WhitelistEmbeds().invalidAccountEmbed(minecraftName).build()
                            ).setEphemeral(true).queue()

                            Logger.info(
                                LanguageHandler.getMessage(
                                    RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_INVALID.key,
                                    mapOf(
                                        "discordAccount" to DiscordUser.parseIDToName(discordUserID),
                                        "minecraftAccount" to minecraftName,
                                        "user" to e.user.effectiveName,
                                    )
                                )
                            )
                            return@launchTask
                        }
                        else -> {
                            e.replyEmbeds(
                                WhitelistEmbeds().accountErrorEmbed(ex.message ?: "-").build()
                            ).setEphemeral(true).queue()

                            Logger.warn(
                                LanguageHandler.getMessage(
                                    RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_ERROR.key,
                                    mapOf(
                                        "discordAccount" to DiscordUser.parseIDToName(discordUserID),
                                        "minecraftAccount" to minecraftName,
                                        "user" to e.user.effectiveName,
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

    private fun handleForceRemove(e: SlashCommandInteractionEvent) {
        val site = e.getOption("site")?.asInt ?: 1
        val discordUser = e.getOption("discord_user")?.asUser
        val minecraftName = e.getOption("minecraft_name")?.asString
        val guildID: String = e.guild?.id ?: return

        CoroutineHandler.launchTask(
            task = {
                if (discordUser != null || minecraftName != null) {
                    var discordEntry: WhitelistEntry? = null
                    var minecraftEntry: WhitelistEntry? = null

                    if (discordUser != null) {
                        val entry = WhitelistLogic.getEntryByDiscordID(discordUser.id)
                        if (entry == null) {
                            if (minecraftName == null) {
                                Logger.dev("No entry found for discord user: $discordUser")
                                // TODO: Add reply
                                return@launchTask
                            }
                            discordEntry = null
                        } else {
                            discordEntry = entry
                        }
                    }

                    if (minecraftName != null) {
                        val entry = WhitelistLogic.getEntryByMinecraftName(minecraftName)
                        if (entry == null) {
                            if (discordEntry == null) {
                                Logger.dev("No entry found for minecraft name: $minecraftName")
                                // TODO: Add reply
                                return@launchTask
                            }
                            minecraftEntry = null
                        } else {
                            minecraftEntry = entry
                        }
                    }

                    if (discordEntry != minecraftEntry && discordEntry != null && minecraftEntry != null) {
                        Logger.dev("Discord entry and Minecraft entry do not match: $discordEntry vs $minecraftEntry")
                        // TODO: Add reply
                        return@launchTask
                    }

                    val discordUserID = discordEntry?.discordUserID ?: minecraftEntry?.discordUserID ?: return@launchTask

                    WhitelistManage().unlinkAccount(discordUserID, guildID)

                    Logger.dev("Removed account: $discordUserID from the whitelist")

                    // TODO: Add reply

                    return@launchTask
                }

                val entries = WhitelistLogic.getEntriesBySite(site)

                val embed = WhitelistEmbeds().forceRemoveEmbed(site)
                e.replyEmbeds(embed.build())
                    .setComponents(
                        ActionRow.of(WhitelistEmbeds().forceRemoveActionRowDropdown(entries).build()),
                        ActionRow.of(WhitelistEmbeds().forceRemoveActionRowButtons(site))
                    )
                    .setEphemeral(true)
                    .queue()
            },
            isOnce = true
        )
    }

    private fun handleCheck(e: SlashCommandInteractionEvent) {
        val minecraftName = e.getOption("minecraft_name")?.asString
        val discordUser = e.getOption("discord_user")?.asUser

        CoroutineHandler.launchTask(
            task = {
                var entry: WhitelistEntry? = null

                if (minecraftName == null && discordUser == null) {
                    val selfUser = e.user
                    entry = WhitelistLogic.getEntryByDiscordID(selfUser.id)
                }

                if (minecraftName != null && discordUser != null) {
                    val minecraftEntry = WhitelistLogic.getEntryByMinecraftName(minecraftName)
                    val discordEntry = WhitelistLogic.getEntryByDiscordID(discordUser.id)

                    if (discordEntry != minecraftEntry && discordEntry != null && minecraftEntry != null) {
                        Logger.dev("Discord entry and Minecraft entry do not match: $discordEntry vs $minecraftEntry")
                        // TODO: Add reply
                        return@launchTask
                    }

                    entry = minecraftEntry ?: discordEntry
                } else if (minecraftName != null) {
                    entry = WhitelistLogic.getEntryByMinecraftName(minecraftName)
                } else if (discordUser != null) {
                    entry = WhitelistLogic.getEntryByDiscordID(discordUser.id)
                }

                if (entry == null) {
                    e.replyEmbeds(
                        WhitelistEmbeds().invalidAccountEmbed(
                            minecraftName ?: discordUser?.name ?: "Unknown"
                        ).build()
                    ).setEphemeral(true).queue()
                    return@launchTask
                }

                val embed = WhitelistEmbeds().checkEmbed(entry)
                e.replyEmbeds(embed.build())
                    .setEphemeral(true)
                    .queue()
            },
            isOnce = true
        )
    }

    /**
     * Executes the whitelist command with the provided arguments.
     *
     * @param e The SlashCommandInteractionEvent containing the command event.
     */
    fun executeCommand(e: SlashCommandInteractionEvent) {
        when (e.subcommandName) {
            "forceadd" -> handleForceAdd(e)
            "forceremove" -> handleForceRemove(e)
            "check" -> handleCheck(e)
            else -> e.reply("Unknown subcommand").setEphemeral(true).queue()
        }
    }

    /**
     * Registers the whitelist command with subcommands.
     *
     * @return SlashCommandData for the whitelist command.
     */
    fun registerCommand(): SlashCommandData {
        val subForceAdd = subForceAdd()
        val subForceRemove = subForceRemove()
        val subCheck = subCheck()

        val data = try {
            Commands.slash(
                "whitelist",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_DESCRIPTION.key)
            )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.valueOf(cmdPermission)))
                .addSubcommands(subForceAdd, subForceRemove, subCheck)
        } catch (_: Exception) {
            Commands.slash(
                "whitelist",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_DESCRIPTION.key)
            )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                .addSubcommands(subForceAdd, subForceRemove, subCheck)
        }

        return data
    }

    private fun subForceAdd(): SubcommandData {
        val subcommand = SubcommandData(
            "forceadd",
            LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_DESCRIPTION.key)
        )

        val options = mutableListOf<OptionData>()
        options.add(
            OptionData(
                OptionType.STRING,
                "minecraft_name",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_MINECRAFTNAME_DESCRIPTION.key),
                true
            )
        )

        options.add(
            OptionData(
                OptionType.USER,
                "discord_user",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_DISCORDUSER_DESCRIPTION.key),
                false
            )
        )

        subcommand.addOptions(options)

        return subcommand
    }

    private fun subForceRemove(): SubcommandData {
        val subcommand = SubcommandData(
            "forceremove",
            LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_DESCRIPTION.key)
        )

        val options = mutableListOf<OptionData>()
        options.add(
            OptionData(
                OptionType.INTEGER,
                "site",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_SITE_DESCRIPTION.key),
                false
            )
        )

        options.add(
            OptionData(
                OptionType.USER,
                "discord_user",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_DISCORDUSER_DESCRIPTION.key),
                false
            )
        )

        options.add(
            OptionData(
                OptionType.STRING,
                "minecraft_name",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_MINECRAFTNAME_DESCRIPTION.key),
                false
            )
        )

        subcommand.addOptions(options)

        return subcommand
    }

    // TODO: Add also as user context menu option
    private fun subCheck(): SubcommandData {
        val subcommand = SubcommandData(
            "check",
            LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_DESCRIPTION.key)
        )

        val options = mutableListOf<OptionData>()
        options.add(
            OptionData(
                OptionType.STRING,
                "minecraft_name",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_MINECRAFTNAME_DESCRIPTION.key),
                false
            )
        )

        options.add(
            OptionData(
                OptionType.USER,
                "discord_user",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_DISCORDUSER_DESCRIPTION.key),
                false
            )
        )

        subcommand.addOptions(options)

        return subcommand
    }
}
