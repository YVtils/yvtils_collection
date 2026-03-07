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
import dev.jorel.commandapi.kotlindsl.multiLiteralArgument
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.GamemodeHandler
import yv.tils.essentials.permissions.Permissions
import yv.tils.essentials.utils.CheckArguments
import yv.tils.utils.data.Data
import yv.tils.common.language.LangStrings as CommonLangStrings

class GamemodeCMD {
    val command = commandTree("gm") {
        withPermission(Permissions.COMMAND_GAMEMODE.permission.name)
        withUsage("gm <gamemode> [player]")
        withAliases("gamemode")

        multiLiteralArgument("gamemode", "creative", "survival", "adventure", "spectator", "0", "1", "2", "3", optional = false) {
            playerProfileArgument("player", true) { // TODO: Fix player profile argument not working
                anyExecutor { sender, args ->
                    val gamemode = args["gamemode"].toString().lowercase()
                    val target = args["player"]

                    if (!CheckArguments.checkForTargetArg(sender, target)) return@anyExecutor

                    val gmHandler = GamemodeHandler()

                    if (target is Player) {
                        gmHandler.gamemodeSwitch(target, gamemode, sender)
                    } else {
                        gmHandler.gamemodeSwitch(sender as Player, gamemode)
                    }
                }
            }
        }
    }
}
