/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.scheduler.BukkitTask
import yv.tils.utils.modules.Core
import yv.tils.utils.player.PlayerUtils.Companion.isSupported
import java.util.UUID

/** Global, continuous carrier visuals, independent of encounter and egg-protection settings. */
class DragonEggCarrierEffects : Listener {
    private class Carrier(val player: Player, val wasGlowing: Boolean, var previous: Location? = null)
    private val carriers = mutableMapOf<UUID, Carrier>()
    private var task: BukkitTask? = null
    private var trailTask: BukkitTask? = null

    fun enable() {
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            sync(Bukkit.getOnlinePlayers().toList())
        }, 1L, 5L)
        trailTask = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable { tickTrails() }, 2L, 2L)
    }

    internal fun sync(players: List<Player>) {
        val holders = players.filter { player ->
            !player.isDead && (player.inventory.contents.any(DragonEggRules::containsEgg) ||
                    DragonEggRules.containsEgg(player.itemOnCursor))
        }.associateBy { it.uniqueId }
        carriers.keys.filter { it !in holders }.forEach(::remove)
        holders.forEach { (id, player) ->
            carriers.getOrPut(id) { Carrier(player, player.isGlowing, player.location.clone()) }
            if (!player.isGlowing) player.isGlowing = true
        }
        // Minecraft uses the same team color for outlines and nametags. Leave teams untouched:
        // the outline follows the existing team color (white without a team), not forced purple.
    }

    internal fun tickTrails() {
        carriers.values.forEach { carrier ->
            if (carrier.player.isOnline && !carrier.player.isDead) trail(carrier)
        }
    }

    private fun trail(carrier: Carrier) {
        val player = carrier.player
        val at = player.location
        val previous = carrier.previous
        carrier.previous = at.clone()
        if (previous == null || previous.world != at.world ||
            player.isInsideVehicle || player.isFlying || player.isGliding) return
        val movement = at.toVector().subtract(previous.toVector()).setY(0.0)
        val distance = movement.lengthSquared()
        // Ignore standing/turning and large discontinuities; accepted teleports reset the sample too.
        if (distance < 1.0e-6 || distance > 16.0 || !isSupported(player)) return
        val behind = at.clone().subtract(movement.normalize().multiply(0.6)).add(0.0, 0.45, 0.0)
        // Force rendering even with reduced vanilla particle settings; source preserves visibility checks.
        Particle.PORTAL.builder().location(behind).source(player)
            .count(8).offset(0.18, 0.15, 0.18).extra(0.025).force(true).spawn()
        Particle.DRAGON_BREATH.builder().location(behind).source(player)
            .count(4).offset(0.15, 0.12, 0.15).extra(0.015).data(1.0f).force(true).spawn()
    }

    private fun remove(id: UUID) {
        carriers.remove(id)?.let { it.player.isGlowing = it.wasGlowing }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun quit(event: PlayerQuitEvent) { remove(event.player.uniqueId) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun death(event: PlayerDeathEvent) {
        if (!event.isCancelled) remove(event.entity.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun teleport(event: PlayerTeleportEvent) {
        if (!event.isCancelled) carriers[event.player.uniqueId]?.previous = null
    }

    fun close() {
        task?.cancel()
        task = null
        trailTask?.cancel()
        trailTask = null
        carriers.keys.toList().forEach(::remove)
    }
}
