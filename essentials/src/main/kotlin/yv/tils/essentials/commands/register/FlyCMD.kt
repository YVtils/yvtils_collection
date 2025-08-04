package yv.tils.essentials.commands.register

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.FlyHandler
import yv.tils.utils.data.Data


class FlyCMD {
    val command = commandTree("fly") {
        withPermission("yvtils.command.fly")
        withPermission(CommandPermission.OP)
        withUsage("fly [player]")

        playerArgument("player", true) {
            anyExecutor { sender, args ->
                if (sender !is Player && args[0] == null) {
                    sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                    return@anyExecutor
                }

                val flyHandler = FlyHandler()

                if (args[0] is Player) {
                    val target = args[0] as Player
                    flyHandler.flySwitch(target, sender)
                } else {
                    flyHandler.flySwitch(sender as Player)
                }
            }
        }
    }
}
