package yv.tils.essentials.commands.register

import data.Data
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerArgument
import language.LanguageHandler
import org.bukkit.entity.Player
import yv.tils.essentials.commands.handler.GodHandler

class GodCMD {
    val command = commandTree("god") {
        withPermission("yvtils.command.god")
        withPermission(CommandPermission.OP)
        withUsage("god")

        playerArgument("player", true) {
            anyExecutor { sender, args ->
                if (sender !is Player && args[0] == null) {
                    sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                    return@anyExecutor
                }

                val godHandler = GodHandler()

                if (args[0] is Player) {
                    val target = args[0] as Player
                    godHandler.godSwitch(target, sender)
                } else {
                    godHandler.godSwitch(sender as Player)
                }
            }
        }
    }
}