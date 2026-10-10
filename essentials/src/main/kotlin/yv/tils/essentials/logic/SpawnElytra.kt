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

package yv.tils.essentials.logic

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.scheduler.BukkitTask
import yv.tils.configv2.language.LanguageHandler
import yv.tils.essentials.commands.handler.FlyHandler
import yv.tils.essentials.config.ConfigFile
import yv.tils.essentials.language.LangStrings
import yv.tils.utils.modules.Core
import yv.tils.utils.player.PlayerUtils.Companion.isSupported
import java.util.*

/** Temporary spawn gliding, coordinated with essentials' normal /fly ownership. */
object SpawnElytra : Listener {
    private data class Flight(var boosted: Boolean = false, var landingTicks: Int = 0)

    private val grantedFlight = mutableSetOf<UUID>()
    private val flights = mutableMapOf<UUID, Flight>()
    private var task: BukkitTask? = null

    fun start() {
        stop()
        Core.instance.server.pluginManager.registerEvents(this, Core.instance)
        task = Core.instance.server.scheduler.runTaskTimer(Core.instance, Runnable {
            Core.instance.server.onlinePlayers.forEach { update(it) }
        }, 1L, 3L)
    }

    fun stop() {
        task?.cancel()
        task = null
        Core.instance.server.onlinePlayers.forEach { release(it) }
        grantedFlight.clear()
        flights.clear()
        org.bukkit.event.HandlerList.unregisterAll(this)
    }

    private fun eligible(player: Player): Boolean =
        ConfigFile.state.spawnElytra.enabled && player.gameMode == GameMode.SURVIVAL &&
                !player.isDead && FlyHandler.fly[player.uniqueId] != true

    private fun inSpawnRadius(player: Player): Boolean {
        val config = ConfigFile.state.spawnElytra
        return player.world.name in config.worlds && config.radius.isFinite() && config.radius > 0 &&
                player.location.distanceSquared(player.world.spawnLocation) <= config.radius * config.radius
    }

    private fun update(player: Player) {
        if (!eligible(player)) {
            release(player)
            return
        }

        val flight = flights[player.uniqueId]
        if (flight != null) {
            if (isSupported(player) || player.isInWater) {
                // Retain damage protection briefly while the landing event is processed.
                flight.landingTicks += 3
                stopGliding(player)
                if (flight.landingTicks >= 6) release(player)
            } else {
                flight.landingTicks = 0
            }
            return
        }

        if (inSpawnRadius(player)) {
            // Do not take ownership of flight granted by another plugin.
            if (!player.allowFlight) {
                grantedFlight.add(player.uniqueId)
                player.allowFlight = true
            }
        } else {
            release(player)
        }
    }

    private fun stopGliding(player: Player) {
        if (player.inventory.chestplate.type != Material.ELYTRA) player.isGliding = false
    }

    /** Called before /fly changes state so the two mechanics cannot own the same flight. */
    fun release(player: Player, preserveFlight: Boolean = false) {
        val hadFlight = flights.remove(player.uniqueId) != null
        val hadGrant = grantedFlight.remove(player.uniqueId)
        if (hadFlight) stopGliding(player)
        if ((hadGrant || hadFlight) && !preserveFlight &&
            FlyHandler.fly[player.uniqueId] != true &&
            player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR
        ) {
            player.isFlying = false
            player.allowFlight = false
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onDoubleJump(event: PlayerToggleFlightEvent) {
        val player = event.player
        if (!event.isFlying || player.uniqueId !in grantedFlight || !eligible(player)) return
        if (!inSpawnRadius(player) || player.uniqueId in flights) {
            event.isCancelled = true
            if (player.uniqueId !in flights) release(player)
            return
        }
        event.isCancelled = true
        player.isFlying = false
        flights[player.uniqueId] = Flight()
        player.isGliding = true
        player.sendActionBar(LanguageHandler.getMessage(LangStrings.SPAWN_ELYTRA_BOOST, player))
    }

    @EventHandler(ignoreCancelled = true)
    fun onHandSwap(event: PlayerSwapHandItemsEvent) {
        val flight = flights[event.player.uniqueId] ?: return
        if (flight.boosted || flight.landingTicks > 0) return
        val strength = ConfigFile.state.spawnElytra.boostStrength
        if (!strength.isFinite() || strength <= 0) return
        event.isCancelled = true
        flight.boosted = true
        event.player.velocity = event.player.location.direction.multiply(strength)
    }

    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity is Player && event.entity.uniqueId in flights &&
            (event.cause == EntityDamageEvent.DamageCause.FALL ||
                    event.cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL)
        ) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onToggleGlide(event: EntityToggleGlideEvent) {
        val player = event.entity as? Player ?: return
        if (!event.isGliding && player.uniqueId in flights && !isSupported(player) && !player.isInWater &&
            player.inventory.chestplate.type != Material.ELYTRA
        ) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) = release(event.player)

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDeath(event: PlayerDeathEvent) = release(event.entity)

    @EventHandler(priority = EventPriority.MONITOR)
    fun onWorldChange(event: PlayerChangedWorldEvent) = release(event.player)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) = release(event.player)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        release(event.player, event.newGameMode == GameMode.CREATIVE || event.newGameMode == GameMode.SPECTATOR)
    }
}
