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

package yv.tils.essentials.commands.register

import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.World
import yv.tils.essentials.commands.handler.DimensionHandler
import yv.tils.essentials.permissions.Permissions

class DimensionCMD {
    val command = commandTree("dimension") {
        withPermission(Permissions.COMMAND_PVP.permission.name)
        withUsage("dimension <world> [state]")

        worldArgument("world", false) {
            booleanArgument("newState",false) {
                anyExecutor { sender, args ->
                    val world = args["world"] as World
                    val state = args["newState"] as Boolean?
                    DimensionHandler().setDimensionState(sender, world, state)
                }
            }

            literalArgument("state", false) {
                anyExecutor { sender, args ->
                    val world = args["world"] as World
                    DimensionHandler().checkDimensionState(sender, world)
                }
            }
        }
    }
}