package yv.tils.message.commands

import data.Data
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import language.LanguageHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import yv.tils.message.logic.MessageHandler

class ReplyCommand {
    val command = commandTree("reply") {
        withPermission("yvtils.command.reply")
        withPermission(CommandPermission.NONE)
        withUsage("reply <message>")
        withAliases("r")

        greedyStringArgument("message") {
            playerExecutor { sender, args ->
                reply(sender, args)
            }
        }
    }

    private fun reply(sender: Player, args: CommandArguments) {
        val target = MessageHandler.chatSession[sender.uniqueId] ?: run {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.reply.noSession",
                    sender.uniqueId,
                    mapOf(
                        "prefix" to Data.prefix
                    )
                )
            )
            return
        }

        val message = args[0] as String
        MessageHandler().sendMessage(sender, Bukkit.getPlayer(target)!!, message)
    }
}