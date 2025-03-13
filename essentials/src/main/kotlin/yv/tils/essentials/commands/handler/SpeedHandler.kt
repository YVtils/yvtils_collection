package yv.tils.essentials.commands.handler

import data.Data
import language.LanguageHandler
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpeedHandler {
    /**
     * Switch speed for player
     * @param player Player to switch speed
     * @param speed String of speed to switch
     * @param sender CommandSender to send messages
     */
    fun speedSwitch(player: Player, speed: String, sender: CommandSender = player) {
        val floatSpeed = speed.toFloat()

        player.walkSpeed = floatSpeed / 10
        player.flySpeed = floatSpeed / 10

        player.sendMessage(
            LanguageHandler.getMessage(
                "command.speed.change.self",
                player.uniqueId,
                params = mapOf(
                    "prefix" to Data.prefix,
                    "speed" to speed
                )
            )
        )

        if (sender != player) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.speed.change.other",
                    sender,
                    params = mapOf(
                        "prefix" to Data.prefix,
                        "speed" to speed,
                        "player" to player.name
                    )
                )
            )
        }
    }

    /**
     * Reset speed for player
     * @param player Player to reset speed
     * @param sender CommandSender to send messages
     */
    fun speedReset(player: Player, sender: CommandSender = player) {
        player.walkSpeed = 0.2F
        player.flySpeed = 0.1F

        player.sendMessage(
            LanguageHandler.getMessage(
                "command.speed.reset.self",
                player.uniqueId,
                params = mapOf("prefix" to Data.prefix)
            )
        )

        if (sender != player) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.speed.reset.other",
                    sender,
                    params = mapOf(
                        "prefix" to Data.prefix,
                        "player" to player.name
                    )
                )
            )
        }
    }
}