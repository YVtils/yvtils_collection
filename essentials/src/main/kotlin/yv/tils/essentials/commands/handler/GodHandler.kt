package yv.tils.essentials.commands.handler

import data.Data
import language.LanguageHandler
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

class GodHandler {
    companion object {
        var god: MutableMap<UUID, Boolean> = HashMap()
    }

    /**
     * Toggle godmode for player
     * @param player Player to toggle godmode
     * @param sender CommandSender to send messages
     */
    fun godSwitch(player: Player, sender: CommandSender = player) {
        val uuid = player.uniqueId

        if (god[uuid] == null || god[uuid] == false) {
            god[uuid] = true
            FlyHandler().flySwitch(player, state = true, silent = true)
            player.sendMessage(
                LanguageHandler.getMessage(
                    "command.god.enable.self",
                    player.uniqueId,
                    params = mapOf("prefix" to Data.prefix)
                )
            )
        } else {
            god[uuid] = false
            FlyHandler().flySwitch(player, state = false, silent = true)
            player.sendMessage(
                LanguageHandler.getMessage(
                    "command.god.disable.self",
                    player.uniqueId,
                    params = mapOf("prefix" to Data.prefix)
                )
            )
        }

        if (player != sender) {
            if (god[uuid] == null || god[uuid] == false) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        "command.god.disable.other",
                        sender,
                        params = mapOf("prefix" to Data.prefix, "player" to player.name)
                    )
                )
            } else {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        "command.god.enable.other",
                        sender,
                        params = mapOf("prefix" to Data.prefix, "player" to player.name)
                    )
                )
            }
        }
    }

    /**
     * Cancel damage event if player is in godmode
     * @param e EntityDamageEvent to cancel
     */
    fun onDamage(e: EntityDamageEvent) {
        if (e.entity is Player) {
            val player = e.entity as Player
            val uuid = player.uniqueId

            if (god[uuid] == true) {
                e.isCancelled = true
            }
        }
    }
}