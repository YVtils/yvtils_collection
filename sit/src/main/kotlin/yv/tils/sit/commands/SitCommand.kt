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

package yv.tils.sit.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import yv.tils.sit.logic.SitManager

class SitCommand {
    val command = commandTree("sit") {
        withPermission("yvtils.smp.command.sit")
        withPermission(CommandPermission.NONE)
        withUsage("sit")
        withAliases("chair")

        playerExecutor { player, _ ->
            if (SitManager().isSitting(player.uniqueId)) {
                SitManager().sitGetter(player)
            } else {
                SitManager().sit(player)
            }
        }
    }
}