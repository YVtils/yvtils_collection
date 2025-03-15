package yv.tils.multiMine.commands

import data.Data
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import language.LanguageHandler
import org.bukkit.entity.Player
import yv.tils.multiMine.logic.BlockManage
import yv.tils.multiMine.logic.MultiMineHandler

class MultiMineCommand {
    val blockManage = BlockManage()

    val command = commandTree("multiMine") {
        withPermission("yvtils.command.multiMine")
        withPermission(CommandPermission.NONE)
        withUsage("multiMine <add/remove/addMultiple/removeMultiple> [block]")
        withAliases("mm")

        stringArgument("action") {
            withPermission("yvtils.command.multiMine.manage")
            withPermission(CommandPermission.OP)
            replaceSuggestions(
                ArgumentSuggestions.strings(
                    "add",
                    "remove",
                    "addMultiple",
                    "removeMultiple",
                    "gui",
                )
            )

            itemStackArgument("block", true) {
                anyExecutor { sender, args ->
                    when (args[0]) {
                        "add" -> {
                            blockManage.addBlock(sender, args[1])
                        }

                        "remove" -> {
                            blockManage.removeBlock(sender, args[1])
                        }

                        "addMultiple" -> {
                            blockManage.addMultiple(sender)
                        }

                        "removeMultiple" -> {
                            blockManage.removeMultiple(sender)
                        }

                        "gui" -> {
                            if (sender is Player) {
                                blockManage.openGUI(sender)
                            } else {
                                sender.sendMessage(LanguageHandler.getMessage(
                                    "command.executor.notPlayer",
                                    params = mapOf("prefix" to Data.prefix)
                                ))
                            }
                        }

                        else -> {
                            sender.sendMessage(
                                LanguageHandler.getMessage(
                                    "command.usage",
                                    sender,
                                    params = mapOf(
                                        "prefix" to Data.prefix,
                                        "command" to "/mm <add/remove/addMultiple/removeMultiple> [block]"
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }

        literalArgument("toggle", true) {
            withPermission("yvtils.command.multiMine.toggle")
            withPermission(CommandPermission.NONE)
            playerExecutor { sender, _ ->
                MultiMineHandler().toggle(sender)
            }
        }
    }
}