package yv.tils.discord.actions.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.actions.commands.handler.JDAServerInfo
import yv.tils.discord.actions.commands.handler.JDAWhitelist

class JDACommandsListener : ListenerAdapter() {
    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        val command = e.name
        val args = e.subcommandName

        when (command) {
            "mcinfo" -> {
                JDAServerInfo().executeCommand(e)
            }
            "whitelist" -> {
                JDAWhitelist().executeCommand(e)
            }
        }
    }
}