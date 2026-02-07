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
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.commands.handler.GamemodeHandler
import yv.tils.utils.data.Data

// TODO: Switch to multiLiteralArgument
class GamemodeCMD {
    val command = commandTree("gm") {
        withPermission("yvtils.command.gamemode")
        withPermission(CommandPermission.OP)
        withUsage("gm <gamemode> [player]")
        withAliases("gamemode")

        stringArgument("gamemode", false) {
            replaceSuggestions(
                ArgumentSuggestions.strings(
                    "survival",
                    "creative",
                    "adventure",
                    "spectator",
                    "0",
                    "1",
                    "2",
                    "3"
                )
            )
            playerProfileArgument("player", true) {
                anyExecutor { sender, args ->

                    if (sender !is Player && args[1] == null) {
                        sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                        return@anyExecutor
                    }

                    val gmHandler = GamemodeHandler()

                    if (args[1] is Player) {
                        val target = args[1] as Player
                        gmHandler.gamemodeSwitch(target, args[0].toString(), sender)
                    } else {
                        gmHandler.gamemodeSwitch(sender as Player, args[0].toString())
                    }
                }
            }
        }
    }
}
