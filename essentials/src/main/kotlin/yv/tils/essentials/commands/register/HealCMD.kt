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
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import org.bukkit.entity.Player
import yv.tils.essentials.commands.handler.HealHandler
import yv.tils.essentials.permissions.Permissions
import yv.tils.essentials.utils.CheckArguments

class HealCMD {
    val command = commandTree("heal") {
        withPermission(Permissions.COMMAND_HEAL.permission.name)
        withUsage("heal [player]")

        entitySelectorArgumentOnePlayer("player", true) {
            anyExecutor { sender, args ->
                val target = args["player"] as? Player

                if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                val healHandler = HealHandler()

                if (target is Player) {
                    val target = target as Player
                    healHandler.playerHeal(target, sender)
                } else {
                    healHandler.playerHeal(sender as Player)
                }
            }
        }
    }
}
