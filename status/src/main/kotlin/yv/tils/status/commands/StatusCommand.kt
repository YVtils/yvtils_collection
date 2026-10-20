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

package yv.tils.status.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.status.logic.StatusManager
import yv.tils.status.data.Permissions
import yv.tils.status.utils.StatusUtils.Companion.generateDefaultStatus
import yv.tils.status.logic.StatusHandler as handler

class StatusCommand {
    val command = commandTree("status") {
        withPermission(Permissions.COMMAND_STATUS.permission.name)
        withUsage("status <set/default/clear> [status/player]")
        withAliases("prefix", "role")

        playerExecutor { sender, _ ->
            StatusManager().manageStatus(sender)
        }

        entitySelectorArgumentOnePlayer("player", false) {
            withPermission(Permissions.COMMAND_STATUS_MANAGE_OTHERS.permission.name)
            playerExecutor { sender, args ->
                StatusManager().manageStatus(sender, args["player"] as Player)
            }
        }

        literalArgument("set", false) {
            withPermission(Permissions.COMMAND_STATUS_SET.permission.name)
            greedyStringArgument("status", false) {
                playerExecutor { player, args ->
                    handler().setStatus(player, args[0] as String)
                }
            }
        }

        literalArgument("default", false) {
            withPermission(Permissions.COMMAND_STATUS_DEFAULT.permission.name)
            greedyStringArgument("status", false) {
                replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    val suggestions = generateDefaultStatus()
                    suggestions.toTypedArray()
                })

                playerExecutor { player, args ->
                    handler().setDefaultStatus(player, args[0] as String)
                }
            }
        }

        literalArgument("clear", false) {
            withPermission(Permissions.COMMAND_STATUS_CLEAR.permission.name)
            playerExecutor { player, _ -> handler().clearStatus(player) }
            entitySelectorArgumentOnePlayer("player", false) {
                withPermission(Permissions.COMMAND_STATUS_CLEAR_OTHERS.permission.name)
                anyExecutor { sender, args ->
                    handler().clearStatus(args["player"] as Player, sender)
                }
            }
        }
    }
}
