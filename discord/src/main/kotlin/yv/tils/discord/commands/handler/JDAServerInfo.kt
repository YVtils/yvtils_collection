package yv.tils.discord.commands.handler

import language.LanguageHandler
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.data.Embeds
import yv.tils.discord.language.RegisterStrings
import java.io.File

class JDAServerInfo {
    companion object {
        val cmdPermission = ConfigFile.getValueAsString("command.serverInfoCommand.permission") ?: "MESSAGE_SEND"
    }

    /**
     * Executes the /mcinfo command.
     *
     * @param e The SlashCommandInteractionEvent containing the command event.
     * @param args The arguments passed to the command, if any.
     */
    fun executeCommand(e: SlashCommandInteractionEvent, args: String?) {
        val serverIcon = File("./server-icon.png")

        if (serverIcon.exists()) {
            e.reply("").setEmbeds(Embeds().serverInfoEmbed(e.user).build()).setEphemeral(true)
                .addFiles(FileUpload.fromData(serverIcon, "server-icon.png")).queue()
        } else {
            e.reply("").setEmbeds(Embeds().serverInfoEmbed(e.user).build()).setEphemeral(true).queue()
        }
    }

    /**
     * Registers the /mcinfo command for the JDA bot.
     *
     * @return SlashCommandData for the /mcinfo command.
     */
    fun registerCommand(): SlashCommandData {
        val data = try {
            Commands.slash(
                "mcinfo",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_MCINFO_DESCRIPTION.key)
            )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.valueOf(cmdPermission)))
        } catch (_: Exception) {
            Commands.slash(
                "mcinfo",
                LanguageHandler.getCleanMessage(RegisterStrings.LangStrings.SLASHCOMMANDS_MCINFO_DESCRIPTION.key)
            )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
        }

        return data
    }
}