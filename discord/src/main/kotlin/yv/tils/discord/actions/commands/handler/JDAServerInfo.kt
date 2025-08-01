package yv.tils.discord.actions.commands.handler

import language.LanguageHandler
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.data.Components
import yv.tils.discord.language.RegisterStrings

class JDAServerInfo {
    companion object {
        val cmdPermission = ConfigFile.getValueAsString("command.serverInfoCommand.permission") ?: "MESSAGE_SEND"
    }

    /**
     * Executes the /mcinfo command.
     *
     * @param e The SlashCommandInteractionEvent containing the command event.
     */
    fun executeCommand(e: SlashCommandInteractionEvent) {
        e.deferReply(true).queue()
        val hook = e.hook

        hook.sendMessageComponents(
            Components().serverInfoComponent(e.user)
        ).setEphemeral(true).useComponentsV2().queue()
    }

    /**
     * Registers the /mcinfo command for the JDA App.
     *
     * @return SlashCommandData for the /mcinfo command.
     */
    fun registerCommand(): SlashCommandData {
        val description = LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_MCINFO_DESCRIPTION.key)

        val data = try {
            Commands.slash(
                "mcinfo",
                description
            )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.valueOf(cmdPermission)))
        } catch (_: Exception) {
            Commands.slash(
                "mcinfo",
                description
            )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        }

        return data
    }
}
