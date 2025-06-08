package yv.tils.discord.commands.handler

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

class JDAWhitelist {
    companion object {
        val cmdPermission = ConfigFile.getValueAsString("command.whitelistCommand.permission") ?: "MANAGE_CHANNEL"
    }

    /**
     * Executes the whitelist command with the provided arguments.
     *
     * @param e The SlashCommandInteractionEvent containing the command event.
     * @param args The arguments passed to the command, if any.
     */
    fun executeCommand(e: SlashCommandInteractionEvent, args: String?) {

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