package yv.tils.essentials.commands.handler

import data.Data
import language.LanguageHandler
import logger.Logger
import net.minecraft.world.entity.ai.attributes.Attributes
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HealHandler {
    /**
     * Heal player
     * @param player Player to heal
     * @param sender CommandSender to send messages
     */
    fun playerHeal(player: Player, sender: CommandSender = player) {
        try {
            player.health = (player as CraftPlayer).handle.getAttribute(Attributes.MAX_HEALTH)?.baseValue ?: 20.0
        } catch (e: Exception) {
            Logger.debug("An error occurred while trying to heal the player. Falling back to effect method. Error: ${e.message}")

            player.addPotionEffect(
                PotionEffect(
                    PotionEffectType.INSTANT_HEALTH,
                    3,
                    20,
                    false,
                    false,
                    false
                )
            )
        }

        player.foodLevel = 20
        player.fireTicks = 0

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