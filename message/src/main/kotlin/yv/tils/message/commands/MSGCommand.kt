package yv.tils.message.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.message.logic.MessageHandler

class MSGCommand {
    val command = commandTree("msg") {
        withPermission("yvtils.command.msg")
        withPermission(CommandPermission.NONE)
        withUsage("msg <player> <message>")
        withAliases("yv/tils/message", "tell", "whisper", "dm", "w")

        playerArgument("yv/tils/player") {
            greedyStringArgument("yv/tils/message") {
                anyExecutor { sender, args ->
                    val target = args[0] as Player
                    val message = args[1] as String

                    MessageHandler().sendMessage(sender, target, message)
                }
            }
        }
    }
}
