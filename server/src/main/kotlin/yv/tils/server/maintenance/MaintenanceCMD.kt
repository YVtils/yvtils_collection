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

package yv.tils.server.maintenance

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.multiLiteralArgument

class MaintenanceCMD {
    private val maintenanceHandler = MaintenanceHandler()

    val command = commandTree("maintenance") {
        withPermission("yvtils.command.maintenance")
        withUsage("maintenance [state]")

        multiLiteralArgument("state", "true", "false", "toggle") {
            anyExecutor { sender, args ->
                maintenanceHandler.maintenance(sender, args)
            }
        }

        anyExecutor { sender, _ ->
            maintenanceHandler.maintenance(sender)
        }
    }
}
