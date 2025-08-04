package yv.tils.essentials.commands.register

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.GodHandler
import yv.tils.utils.data.Data

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
