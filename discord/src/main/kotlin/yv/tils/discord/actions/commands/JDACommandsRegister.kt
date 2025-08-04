package yv.tils.discord.actions.commands

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import yv.tils.discord.actions.commands.handler.JDAServerInfo
import yv.tils.discord.actions.commands.handler.JDAWhitelist
import yv.tils.discord.logic.AppLogic
import yv.tils.utils.logger.Logger

class JDACommandsRegister {

    fun registerCommands() {
        val commandData: MutableList<CommandData> = mutableListOf()
        serverInfoCMD(commandData)
        whitelistCMD(commandData)
        try {
            AppLogic.getJDA().updateCommands().addCommands(commandData).queue()
        } catch (e: IllegalStateException) {
            Logger.error("Failed to register commands: ${e.message}")
        }
    }

    private fun serverInfoCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAServerInfo().registerCommand())
    }

    private fun whitelistCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAWhitelist().registerCommand())
    }
}
