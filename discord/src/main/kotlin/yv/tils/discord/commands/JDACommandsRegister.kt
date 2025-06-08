package yv.tils.discord.commands

import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import yv.tils.discord.commands.handler.JDAServerInfo
import yv.tils.discord.commands.handler.JDAWhitelist

class JDACommandsRegister : ListenerAdapter() {
    override fun onReady(e: ReadyEvent) {
        val commandData: MutableList<CommandData> = mutableListOf()
        serverInfoCMD(commandData)
        whitelistCMD(commandData)
        e.jda.updateCommands().addCommands(commandData).queue()
    }

    private fun serverInfoCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAServerInfo().registerCommand())
    }

    private fun whitelistCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAWhitelist().registerCommand())
    }
}