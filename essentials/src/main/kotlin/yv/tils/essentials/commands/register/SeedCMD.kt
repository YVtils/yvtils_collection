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

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import yv.tils.essentials.commands.handler.SeedHandler
import yv.tils.essentials.permissions.Permissions

class SeedCMD {
    val command = commandTree("seed") {
        withPermission(Permissions.COMMAND_SEED.permission.name)
        withUsage("seed show")

        literalArgument("show", false) {
            anyExecutor { sender, _ ->
                val seedHandler = SeedHandler()
                seedHandler.seed(sender)
            }
        }
    }
}