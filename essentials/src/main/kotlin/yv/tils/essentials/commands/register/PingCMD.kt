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
import yv.tils.essentials.commands.handler.PingHandler
import yv.tils.essentials.permissions.Permissions
import yv.tils.essentials.utils.CheckArguments
import yv.tils.utils.data.Data

class PingCMD {
    val command = commandTree("ping") {
        withPermission(Permissions.COMMAND_PING.permission.name)
        withUsage("ping")

        playerProfileArgument("player", true) { // TODO: Fix player profile argument not working
            withPermission(Permissions.COMMAND_PING_ARG_OTHER.permission.name)
            anyExecutor { sender, args ->
                val target = args["player"]

                if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                val pingHandler = PingHandler()

                if (target is Player) {
                    pingHandler.ping(target, sender)
                } else {
                    pingHandler.ping(sender as Player)
                }
            }
        }
    }
}
