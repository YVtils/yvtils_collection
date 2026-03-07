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
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.GodHandler
import yv.tils.essentials.permissions.Permissions
import yv.tils.essentials.utils.CheckArguments
import yv.tils.utils.data.Data

class GodCMD {
    val command = commandTree("god") {
        withPermission(Permissions.COMMAND_GOD.permission.name)
        withUsage("god [player]")

        playerProfileArgument("player", true) { // TODO: Fix player profile argument not working
            anyExecutor { sender, args ->
                val target = args["player"]

                if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                val godHandler = GodHandler()

                if (target is Player) {
                    godHandler.godSwitch(target, sender)
                } else {
                    godHandler.godSwitch(sender as Player)
                }
            }
        }
    }
}
