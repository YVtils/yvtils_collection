/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.multiMine.commands

import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.multiMine.data.Permissions
import yv.tils.multiMine.logic.BlockManage
import yv.tils.multiMine.logic.ManageGUI
import yv.tils.multiMine.logic.MultiMineHandler
import yv.tils.utils.modules.Core

class MultiMineCommand {
    val blockManage = BlockManage()
    val manageGUI = ManageGUI()

    val command = commandTree("multiMine") {
        withUsage("multiMine <add/remove/addMultiple/removeMultiple> [block]")
        withAliases("mm")

        multiLiteralArgument("action", "add", "remove", "addMultiple", "removeMultiple", "settings") {
            withPermission(Permissions.COMMAND_MULTIMINE_MANAGE.permission.name)

            itemStackArgument("block", true) {
                anyExecutor { sender, args ->
                    when (args["action"]) {
                        "add" -> {
                            blockManage.addBlock(sender, args["block"])
                        }

                        "remove" -> {
                            blockManage.removeBlock(sender, args["block"])
                        }

                        "addMultiple" -> {
                            blockManage.addMultiple(sender)
                        }

                        "removeMultiple" -> {
                            blockManage.removeMultiple(sender)
                        }

                        "settings" -> {
                            if (sender is Player) {
                                manageGUI.openGUI(sender)
                            } else {
                                sender.sendMessage(
                                    LanguageHandler.getMessage(
                                        "command.executor.notPlayer",
                                        params = mapOf("prefix" to Core.prefix)
                                    )
                                )
                            }
                        }

                        else -> {
                            sender.sendMessage(
                                LanguageHandler.getMessage(
                                    "command.usage",
                                    sender,
                                    params = mapOf(
                                        "prefix" to Core.prefix,
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
            withPermission(Permissions.COMMAND_MULTIMINE_TOGGLE_SELF.permission.name)
            playerProfileArgument(
                "target",
                true
            ) { // TODO: Test if this works with playerProfile ot needs to be migrated to entitySelectorArgumentOnePlayer
                withPermission(Permissions.COMMAND_MULTIMINE_TOGGLE_OTHERS.permission.name)
                anyExecutor { sender, args ->
                    MultiMineHandler().toggle(sender, args)
                }
            }
        }
    }
}
