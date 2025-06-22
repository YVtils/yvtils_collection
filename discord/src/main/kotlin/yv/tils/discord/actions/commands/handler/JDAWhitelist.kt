package yv.tils.discord.actions.commands.handler

import coroutine.CoroutineHandler
import language.LanguageHandler
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.whitelist.WhitelistEmbeds
import yv.tils.discord.logic.whitelist.WhitelistEntry
import yv.tils.discord.logic.whitelist.WhitelistLogic
import yv.tils.discord.logic.whitelist.WhitelistManage
import yv.tils.discord.logic.whitelist.WhitelistManage.Companion.AlreadyWhitelistedException
import yv.tils.discord.logic.whitelist.WhitelistManage.Companion.InvalidAccountException
import yv.tils.discord.logic.whitelist.WhitelistManage.Companion.accountReplaceCache

// TODO: Fix command descriptions showing as keys
class JDAWhitelist {
    companion object {
        val cmdPermission = ConfigFile.getValueAsString("command.whitelistCommand.permission") ?: "MANAGE_CHANNEL"
    }

    fun handleForceAdd(e: SlashCommandInteractionEvent) {
        val minecraftName = e.getOption("minecraft_name")?.asString ?: return
        val discordUserID = e.getOption("discord_user")?.asUser?.id ?: "~$minecraftName"
        val guildID: String = e.guild?.id ?: return


        CoroutineHandler.launchTask(
            task = {
                if (WhitelistLogic.containsEntry(discordUserID)) {
                    val oldName = WhitelistLogic.getEntryByDiscordID(discordUserID)?.minecraftName ?: "Unknown"

                    accountReplaceCache[discordUserID] = minecraftName

                    e.replyEmbeds(
                        WhitelistEmbeds().accountChangeEmbed(
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
                } catch (ex: Exception) {
                    when (ex) {
                        is AlreadyWhitelistedException -> {
                            e.replyEmbeds(
                                WhitelistEmbeds().accountAlreadyListedEmbed(minecraftName).build()
                            ).setEphemeral(true).queue()

                            // TODO: Add console message for "xxx tried to whitelist their account yyy, but it is already whitelisted"
                            return@launchTask
                        }
                        is InvalidAccountException -> {
                            e.replyEmbeds(
                                WhitelistEmbeds().invalidAccountEmbed(minecraftName).build()
                            ).setEphemeral(true).queue()

                            // TODO: Add console message for "xxx tried to whitelist their account yyy, but it is invalid"
                            return@launchTask
                        }
                        else -> {
                            e.replyEmbeds(
                                WhitelistEmbeds().accountErrorEmbed(ex.message ?: "-").build()
                            ).setEphemeral(true).queue()

                            // TODO: Add console message for "xxx tried to whitelist their account yyy, but an error occurred"
                            return@launchTask
                        }
                    }
                }
            },
            isOnce = true
        )
    }

    fun handleForceRemove(e: SlashCommandInteractionEvent) {
        // Get all options from the command
        // If dc_user or minecraft_name is provided, remove the entry directly
        // If site is provided send an embed with a list of entries, each site has 25, of the specified site
        // If no options are provided, send an embed with a list of all entries splited in pages starting from 0

        val site = e.getOption("site")?.asLong ?: 0L
        val discordUser = e.getOption("discord_user")?.asUser
        val minecraftName = e.getOption("minecraft_name")?.asString
        val guildID: String = e.guild?.id ?: return

        CoroutineHandler.launchTask(
            task = {
                if (discordUser != null || minecraftName != null) {

                    return@launchTask
                }

                val entries = WhitelistLogic.getEntriesBySite(site) ?: WhitelistLogic.getEntriesBySite(0L)
                if (entries.isEmpty()) {
                    // Send an embed with deactivates dropdown and a message that no entries were found
                }

                val embed = WhitelistEmbeds().forceRemoveEmbed(entries, site)
                e.replyEmbeds(embed.build())
                    .addActionRow(WhitelistEmbeds().forceRemoveActionRow(site, entries.size))
                    .setEphemeral(true)
                    .queue()

            },
            isOnce = true
        )
    }

    fun handleCheck(e: SlashCommandInteractionEvent) {
        // Handle the check subcommand logic here
    }

    /**
     * Executes the whitelist command with the provided arguments.
     *
     * @param e The SlashCommandInteractionEvent containing the command event.
     * @param args The arguments passed to the command, if any.
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