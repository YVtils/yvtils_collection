package yv.tils.multiMine.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.multiMine.data.Permissions
import yv.tils.multiMine.logic.BlockManage
import yv.tils.multiMine.logic.ManageGUI
import yv.tils.multiMine.logic.MultiMineHandler
import yv.tils.utils.data.Data

class MultiMineCommand {
    val blockManage = BlockManage()
    val manageGUI = ManageGUI()

    val command = commandTree("multiMine") {
        withUsage("multiMine <add/remove/addMultiple/removeMultiple> [block]")
        withAliases("mm")

        stringArgument("action") {
            withPermission(Permissions.COMMAND_MULTIMINE_MANAGE.permission.name)
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
                                manageGUI.openGUI(sender)
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
            withPermission(Permissions.COMMAND_MULTIMINE_TOGGLE.permission.name)
            playerExecutor { sender, _ ->
                MultiMineHandler().toggle(sender)
            }
        }
    }
}
