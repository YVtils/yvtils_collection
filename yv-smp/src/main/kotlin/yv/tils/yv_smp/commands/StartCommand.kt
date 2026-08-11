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

package yv.tils.yv_smp.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import yv.tils.yv_smp.logic.StartLogic
import yv.tils.yv_smp.permissions.Permissions

class StartCommand {
    val command = commandTree("start") {
        withPermission(Permissions.COMMAND_START.permission.name)
        withUsage("start")

        anyExecutor { sender, _ ->
            StartLogic().start(sender)
        }
    }
}