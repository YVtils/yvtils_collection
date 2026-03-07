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

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.essentials.commands.handler.SpeedHandler
import yv.tils.essentials.permissions.Permissions
import yv.tils.essentials.utils.CheckArguments

class SpeedCMD {
    private val speedHandler = SpeedHandler()

    val command = commandTree("speed") {
        withPermission(Permissions.COMMAND_SPEED.permission.name)
        withUsage("speed <speed> [player]")

        integerArgument("speed", -10, 10, false) {
            replaceSuggestions(
                ArgumentSuggestions.strings(
                    "10",
                    "9",
                    "8",
                    "7",
                    "6",
                    "5",
                    "4",
                    "3",
                    "2",
                    "1",
                    "0",
                    "-1",
                    "-2",
                    "-3",
                    "-4",
                    "-5",
                    "-6",
                    "-7",
                    "-8",
                    "-9",
                    "-10"
                )
            )
            playerProfileArgument("player", true) { // TODO: Fix player profile argument not working
                anyExecutor { sender, args ->
                    val speed = args["speed"].toString()
                    val target = args["player"]

                    if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                    if (target is Player) {
                        speedHandler.speedSwitch(target, speed, sender)
                    } else {
                        speedHandler.speedSwitch(sender as Player, speed)
                    }
                }
            }
        }

        literalArgument("reset", false) {
            playerProfileArgument("player", true) { // TODO: Fix player profile argument not working
                anyExecutor { sender, args ->
                    val target = args["player"]

                    if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                    if (args[1] is Player) {
                        val target = args[1] as Player
                        speedHandler.speedReset(target, sender)
                    } else {
                        speedHandler.speedReset(sender as Player)
                    }
                }
            }
        }
    }
}
