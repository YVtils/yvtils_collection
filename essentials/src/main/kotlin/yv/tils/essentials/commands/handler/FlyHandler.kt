package yv.tils.essentials.commands.handler

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.block.BlockFace
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import java.util.*

class FlyHandler {
    companion object {
        var fly: MutableMap<UUID, Boolean> = HashMap()
        var airAfter: MutableMap<UUID, Boolean> = HashMap()
    }

    /**
     * Toggle fly for player
     * @param player Player to toggle fly
     * @param sender CommandSender to send messages
     * @param silent Boolean to toggle silent mode
     */
    fun flySwitch(player: Player, sender: CommandSender = player, state: Boolean? = null, silent: Boolean = false) {
        val uuid = player.uniqueId

        if ((fly[uuid] == null || fly[uuid] == false) || state == true) {
            fly[uuid] = true
            player.allowFlight = true
            player.isFlying = true
            if (!silent) {
                player.sendMessage(
                    LanguageHandler.getMessage("command.fly.enable.self", player.uniqueId, params = mapOf("prefix" to Data.prefix))
                )

                if (player != sender) {
                    sender.sendMessage(
                        LanguageHandler.getMessage("command.fly.enable.other", sender, params = mapOf("prefix" to Data.prefix, "player" to player.name))
                    )
                }
            }
        } else {
            if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) {
                fly[uuid] = false
            } else {
                airAfter[uuid] = player.location.block.getRelative(BlockFace.DOWN).type.isAir

                fly[uuid] = false
                player.allowFlight = false
                player.isFlying = false
            }

            if (!silent) {
                player.sendMessage(
                    LanguageHandler.getMessage("command.fly.disable.self", player.uniqueId, params = mapOf("prefix" to Data.prefix))
                )

                if (player != sender) {
                    sender.sendMessage(
                        LanguageHandler.getMessage("command.fly.disable.other", sender, params = mapOf("prefix" to Data.prefix, "player" to player.name))
                    )
                }
            }
        }
    }

    /**
     * Cancel fall damage for flying players
     * @param e EntityDamageEvent
     */
    fun onLandingDamage(e: EntityDamageEvent) {
        if (e.entity is Player) {
            val player = e.entity as Player
            val uuid = player.uniqueId

            if (fly[uuid] == true) {
                if (e.cause == EntityDamageEvent.DamageCause.FALL) {
                    e.isCancelled = true
                }
            } else if (airAfter[uuid] == true) {
                if (e.cause == EntityDamageEvent.DamageCause.FALL) {
                    e.isCancelled = true
                    airAfter[uuid] = false
                }
            }
        }
    }

    /**
     * Reenable fly for player on world change
     * @param e PlayerChangedWorldEvent
     */
    fun onWorldChange(e: PlayerChangedWorldEvent) {
        val player = e.player
        val uuid = player.uniqueId

        if (fly[uuid] == true) {
            player.allowFlight = true
            player.isFlying = true
        }
    }

    /**
     * Reenable fly for player on rejoin
     * @param e PlayerJoinEvent
     */
    fun onRejoin(e: PlayerJoinEvent) {
        val player = e.player
        val uuid = player.uniqueId

        if (fly[uuid] == true) {
            player.allowFlight = true
            player.isFlying = true
        }
    }

    /**
     * Reenable fly for player on gamemode switch
     * @param e PlayerGameModeChangeEvent
     */
    fun onGamemodeSwitch(e: PlayerGameModeChangeEvent) {
        val player = e.player
        val uuid = player.uniqueId

        Bukkit.getScheduler().runTaskLater(
            Data.instance,
            Runnable {
                if (fly[uuid] == true) {
                    player.allowFlight = true
                    player.isFlying = true
                }
            },
            1
        )
    }
}
