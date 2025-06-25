package yv.tils.discord.actions.commands

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import yv.tils.discord.actions.commands.handler.JDAServerInfo
import yv.tils.discord.actions.commands.handler.JDAWhitelist
import yv.tils.discord.logic.AppLogic

class JDACommandsRegister {

    fun registerCommands() {
        val commandData: MutableList<CommandData> = mutableListOf()
        serverInfoCMD(commandData)
        whitelistCMD(commandData)
        AppLogic.jda.updateCommands().addCommands(commandData).queue()
    }

    private fun serverInfoCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAServerInfo().registerCommand())
    }

    private fun whitelistCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAWhitelist().registerCommand())
    }
}