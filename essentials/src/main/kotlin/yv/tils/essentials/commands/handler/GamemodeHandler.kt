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

package yv.tils.essentials.commands.handler

import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.Language
import yv.tils.config.language.LanguageHandler
import yv.tils.config.language.LanguageProvider
import yv.tils.essentials.language.LangStrings
import yv.tils.common.language.LangStrings as CommonLangStrings
import yv.tils.utils.data.Data

class GamemodeHandler {
    /**
     * Switch gamemode for player
     * @param player Player to switch gamemode
     * @param gamemode String of gamemode to switch
     * @param sender CommandSender to send messages
     */
    fun gamemodeSwitch(player: Player, gamemode: String, sender: CommandSender = player) {
        val gamemodeName: LanguageProvider.LangStrings

        when (gamemode) {
            "survival", "0" -> {
                gamemodeName = LangStrings.GAMEMODE_SURVIVAL
                player.gameMode = GameMode.SURVIVAL
            }

            "creative", "1" -> {
                gamemodeName = LangStrings.GAMEMODE_CREATIVE
                player.gameMode = GameMode.CREATIVE
            }

            "adventure", "2" -> {
                gamemodeName = LangStrings.GAMEMODE_ADVENTURE
                player.gameMode = GameMode.ADVENTURE
            }

            "spectator", "3" -> {
                gamemodeName = LangStrings.GAMEMODE_SPECTATOR
                player.gameMode = GameMode.SPECTATOR
            }

            else -> {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        CommonLangStrings.COMMAND_USAGE.key,
                        sender,
                        params = mapOf(
                            "prefix" to Data.prefix,
                            "command" to "/gm <survival/creative/adventure/spectator> [player]"
                        )
                    )
                )

                return
            }
        }

        player.playSound(player.location, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 15f, 15f)

        player.sendMessage(
            LanguageHandler.getMessage(
                LangStrings.COMMAND_GAMEMODE_SELF,
                player.uniqueId,
                mapOf(
                    "prefix" to Data.prefix,
                    "gamemode" to LanguageHandler.getRawMessage(gamemodeName, player.uniqueId),
                )
            ),
        )

        if (player != sender) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_GAMEMODE_OTHER,
                    sender,
                    mapOf(
                        "prefix" to Data.prefix,
                        "gamemode" to LanguageHandler.getRawMessage(gamemodeName, sender),
                        "player" to player.name
                    )
                )
            )
        }
    }
}
