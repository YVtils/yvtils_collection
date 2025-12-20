/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.moderation.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.gui.PlayerGUI

class ModGUICommand {
    val command = commandTree("modgui") {
        withPermission(Permissions.COMMAND_MODERATION_MODGUI.permission.name)
        withUsage("modgui [target]")

        playerProfileArgument("target", true) {
            anyExecutor { sender, arguments ->
                PlayerGUI().openGUI(sender)
            }
        }
    }
}