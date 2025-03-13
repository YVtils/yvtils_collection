package yv.tils.essentials.commands.handler

import data.Data
import language.LanguageHandler
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HealHandler {
    /**
     * Heal player
     * @param player Player to heal
     * @param sender CommandSender to send messages
     */
    fun playerHeal(player: Player, sender: CommandSender = player) {
        player.health = player.getAttribute(Attribute.MAX_HEALTH)!!.value
        player.foodLevel = 20

        player.sendMessage(
            LanguageHandler.getMessage(
                "command.heal.self",
                player.uniqueId,
                params = mapOf("prefix" to Data.prefix)
            )
        )

        if (sender != player) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.heal.other",
                    sender,
                    params = mapOf("prefix" to Data.prefix, "player" to player.name)
                )
            )
        }
    }
}