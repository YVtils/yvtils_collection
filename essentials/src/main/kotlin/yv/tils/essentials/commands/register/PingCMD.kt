package yv.tils.essentials.commands.register

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.PingHandler
import yv.tils.utils.data.Data

class PingCMD {
    val command = commandTree("ping") {
        withPermission("yvtils.command.ping")
        withPermission(CommandPermission.NONE)
        withUsage("ping")

        playerArgument("player", true) {
            anyExecutor { sender, args ->
                if (sender !is Player && args[0] == null) {
                    sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                    return@anyExecutor
                }

                val pingHandler = PingHandler()

                if (args[0] is Player) {
                    val target = args[0] as Player
                    pingHandler.ping(target, sender)
                } else {
                    pingHandler.ping(sender as Player)
                }
            }
        }
    }
}
