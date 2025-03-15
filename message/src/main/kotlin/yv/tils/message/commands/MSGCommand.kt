package yv.tils.message.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerArgument
import org.bukkit.entity.Player
import yv.tils.message.logic.MessageHandler

class MSGCommand {
    val command = commandTree("msg") {
        withPermission("yvtils.command.msg")
        withPermission(CommandPermission.NONE)
        withUsage("msg <player> <message>")
        withAliases("message", "tell", "whisper", "dm", "w")

        playerArgument("player") {
            greedyStringArgument("message") {
                anyExecutor { sender, args ->
                    val target = args[0] as Player
                    val message = args[1] as String

                    MessageHandler().sendMessage(sender, target, message)
                }
            }
        }
    }
}