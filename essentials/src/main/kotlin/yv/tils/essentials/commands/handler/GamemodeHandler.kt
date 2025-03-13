package yv.tils.essentials.commands.handler

import data.Data
import language.LanguageHandler
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GamemodeHandler {
    /**
     * Switch gamemode for player
     * @param player Player to switch gamemode
     * @param gamemode String of gamemode to switch
     * @param sender CommandSender to send messages
     */
    fun gamemodeSwitch(player: Player, gamemode: String, sender: CommandSender = player) {
        val gamemodeName: String

        when (gamemode) {
            "survival", "0" -> {
                gamemodeName = "gamemode.survival"

                player.gameMode = GameMode.SURVIVAL
            }

            "creative", "1" -> {
                gamemodeName = "gamemode.creative"

                player.gameMode = GameMode.CREATIVE
            }

            "adventure", "2" -> {
                gamemodeName = "gamemode.adventure"

                player.gameMode = GameMode.ADVENTURE
            }

            "spectator", "3" -> {
                gamemodeName = "gamemode.spectator"

                player.gameMode = GameMode.SPECTATOR
            }

            else -> {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        "command.usage",
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
                "command.gamemode.self",
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
                    "command.gamemode.other",
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