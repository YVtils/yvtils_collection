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
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.FlyHandler
import yv.tils.utils.modules.Core
import yv.tils.essentials.permissions.Permissions
import yv.tils.essentials.utils.CheckArguments

class FlyCMD {
    val command = commandTree("fly") {
        withPermission(Permissions.COMMAND_FLY.permission.name)
        withUsage("fly [player]")

        playerProfileArgument("player", true) { // TODO: Fix player profile argument not working
            anyExecutor { sender, args ->
                val target = args["player"]

                if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                val flyHandler = FlyHandler()

                if (target is Player) {
                    flyHandler.flySwitch(target, sender)
                } else {
                    flyHandler.flySwitch(sender as Player)
                }
            }
        }
    }
}
