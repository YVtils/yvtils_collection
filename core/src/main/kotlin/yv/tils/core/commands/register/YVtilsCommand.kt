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

package yv.tils.core.commands.register

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import yv.tils.common.permissions.Permissions
import yv.tils.core.commands.gui.YVtilsConfigGui
import yv.tils.core.commands.gui.YVtilsModulesGui
import yv.tils.core.commands.handler.YVtilsHandler
import yv.tils.utils.modules.Module

class YVtilsCommand {
    val command = commandTree("yvtils") {
        withPermission(Permissions.YVTILS_MANAGE_COMMAND.permission.name)
        withUsage("yvtils <modules/config/info>")

        literalArgument("info", false) {
            anyExecutor { sender, _ ->
                YVtilsHandler().pluginInformation(sender)
            }
        }

        literalArgument("modules", false) {
            playerExecutor { player, _ ->
                YVtilsModulesGui.open(player)
            }
        }

        literalArgument("config", false) {
            playerExecutor { player, _ ->
                YVtilsConfigGui.open(player)
            }
            stringArgument("module") {
                replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    Module.getModules(sort = true).filter { it.configGuiOpener != null }.map { it.name }.toTypedArray()
                })
                playerExecutor { player, args ->
                    YVtilsConfigGui.openModule(player, args["module"] as String)
                }
            }
        }
    }
}
