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

        e.deferReply(true).queue()
        val hook = e.hook

        CoroutineHandler.launchTask(
            task = {
                if (WhitelistLogic.containsEntry(discordUserID)) {
                    val oldName = WhitelistLogic.getEntryByDiscordID(discordUserID)?.minecraftName ?: "Unknown"

                    accountReplaceCache[discordUserID] = minecraftName

                    hook.sendMessageComponents(
                        WhitelistComponents().accountChangePromptContainer(
                            oldName = oldName,
                            newName = minecraftName
                        )
                    ).useComponentsV2().setEphemeral(true).queue()

                    return@launchTask
                }

                try {
                    WhitelistManage().linkAccount(minecraftName, discordUserID, guildID, e.user)

                    hook.sendMessageComponents(
                        WhitelistComponents().accountAddContainer(minecraftName)
                    ).useComponentsV2().setEphemeral(true).queue()
                } catch (ex: Exception) {
                    when (ex) {
                        is AlreadyWhitelistedException -> {
                            hook.sendMessageComponents(
                                WhitelistComponents().accountAlreadyListedContainer(minecraftName)
                            ).useComponentsV2().setEphemeral(true).queue()

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
                            hook.sendMessageComponents(
                                WhitelistComponents().invalidAccountContainer(minecraftName)
                            ).useComponentsV2().setEphemeral(true).queue()

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
                            hook.sendMessageComponents(
                                WhitelistComponents().accountErrorContainer(ex.message ?: "-")
                            ).useComponentsV2().setEphemeral(true).queue()

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

        val user = e.user

        e.deferReply(true).queue()
        val hook = e.hook

        CoroutineHandler.launchTask(
            task = {
                WhitelistLogic.getEntriesBySite(site)

                if (discordUser != null || minecraftName != null) {
                    var discordEntry: WhitelistEntry? = null
                    var minecraftEntry: WhitelistEntry? = null

                    if (discordUser != null) {
                        val entry = WhitelistLogic.getEntryByDiscordID(discordUser.id)
                        if (entry == null) {
                            if (minecraftName == null) {
                                hook.sendMessageComponents(
                                    WhitelistComponents().accountErrorContainer(
                                        LanguageHandler.getRawMessage(
                                            RegisterStrings.LangStrings.ERROR_WHITELIST_FORCE_REMOVE_NO_ENTRY_DISCORD.key,
                                            params = mapOf(
                                                "discordUser" to discordUser.name
                                            )
                                        )
                                    )
                                ).useComponentsV2().queue()
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
                                hook.sendMessageComponents(
                                    WhitelistComponents().accountErrorContainer(
                                        LanguageHandler.getRawMessage(
                                            RegisterStrings.LangStrings.ERROR_WHITELIST_FORCE_REMOVE_NO_ENTRY_MINECRAFT.key,
                                            params = mapOf(
                                                "minecraftName" to minecraftName
                                            )
                                        )
                                    )
                                ).useComponentsV2().queue()
                                return@launchTask
                            }
                            minecraftEntry = null
                        } else {
                            minecraftEntry = entry
                        }
                    }

                    if (discordEntry != minecraftEntry && discordEntry != null && minecraftEntry != null) {
                        hook.sendMessageComponents(
                            WhitelistComponents().accountErrorContainer(
                                LanguageHandler.getRawMessage(
                                    RegisterStrings.LangStrings.ERROR_WHITELIST_FORCE_REMOVE_ENTRIES_NOT_EQUAL.key,
                                    params = mapOf(
                                        "discordEntry" to discordEntry.toString(),
                                        "minecraftEntry" to minecraftEntry.toString()
                                    )
                                )
                            )
                        ).useComponentsV2().queue()

                        return@launchTask
                    }

                    val discordUserID = discordEntry?.discordUserID ?: minecraftEntry?.discordUserID ?: return@launchTask
                    val oldEntry = WhitelistManage().unlinkAccount(discordUserID, guildID, user)

                    hook.sendMessageComponents(
                        WhitelistComponents().forceRemoveContainer(site = site, listOf(oldEntry))
                    ).useComponentsV2().setEphemeral(true).queue()

                    return@launchTask
                }

                hook.sendMessageComponents(
                    WhitelistComponents().forceRemoveContainer(site = site)
                ).useComponentsV2().setEphemeral(true).queue()
            },
            isOnce = true
        )
    }

    private fun handleCheck(e: SlashCommandInteractionEvent) {
        val minecraftName = e.getOption("minecraft_name")?.asString
        val discordUser = e.getOption("discord_user")?.asUser

        e.deferReply(true).queue()
        val hook = e.hook

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
                        hook.sendMessageComponents(
                            WhitelistComponents().accountErrorContainer(
                                LanguageHandler.getRawMessage(
                                    RegisterStrings.LangStrings.ERROR_WHITELIST_FORCE_REMOVE_ENTRIES_NOT_EQUAL.key,
                                    params = mapOf(
                                        "discordEntry" to discordEntry.toString(),
                                        "minecraftEntry" to minecraftEntry.toString()
                                    )
                                )
                            )
                        ).useComponentsV2().setEphemeral(true).queue()


                        return@launchTask
                    }

                    entry = minecraftEntry ?: discordEntry
                } else if (minecraftName != null) {
                    entry = WhitelistLogic.getEntryByMinecraftName(minecraftName)
                } else if (discordUser != null) {
                    entry = WhitelistLogic.getEntryByDiscordID(discordUser.id)
                }

                if (entry == null) {
                    hook.sendMessageComponents(
                        WhitelistComponents().invalidAccountContainer(
                            minecraftName ?: discordUser?.name ?: "Unknown"
                        )
                    ).useComponentsV2().setEphemeral(true).queue()
                    return@launchTask
                }

                hook.sendMessageComponents(
                    WhitelistComponents().checkContainer(entry)
                ).useComponentsV2().setEphemeral(true).queue()
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
