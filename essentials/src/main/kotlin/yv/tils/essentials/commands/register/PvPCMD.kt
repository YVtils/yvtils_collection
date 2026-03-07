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

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.booleanArgument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import net.minecraft.world.level.levelgen.SurfaceRules.state
import yv.tils.essentials.commands.handler.PvPHandler
import yv.tils.essentials.permissions.Permissions

class PvPCMD {
    val command = commandTree("pvp") {
        withPermission(Permissions.COMMAND_PVP.permission.name)
        withUsage("pvp [state]")

        val pvpHandler = PvPHandler()

        booleanArgument("newState", false) {
            anyExecutor { sender, args ->
                val state = args["newState"] as Boolean
                pvpHandler.handler(sender, state)
            }
        }

        literalArgument("state", false) {
            anyExecutor { sender, _ ->
                pvpHandler.checkStateHandler(sender)
            }
        }

        anyExecutor { sender, _ ->
            pvpHandler.handler(sender)
        }
    }
}