/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */
package yv.tils.regionsv2.logic

import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import yv.tils.utils.modules.Core
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.data.Permissions
import yv.tils.regionsv2.language.LangStrings.*
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class ClaimBounds(val world: World, val min: BlockVector3, val max: BlockVector3) {
    val sides get() = listOf(max.x() - min.x() + 1, max.y() - min.y() + 1, max.z() - min.z() + 1)
    val volume get() = sides.fold(1L) { total, side -> total * side }
}

object ClaimSelection {
    private data class Selection(var first: Location? = null, var second: Location? = null, var name: String = "")

    private val selections = mutableMapOf<UUID, Selection>()
    private val previews = mutableMapOf<UUID, BukkitTask>()
    fun name(player: Player): String = selections[player.uniqueId]?.name.orEmpty()

    fun name(player: Player, name: String) {
        check(ConfigFile.state.enabled && player.hasPermission(Permissions.CLAIM.permission.name)) { SELECTION_DENIED.key }
        check(name.isNotBlank() && name.length <= 64 && name.none { it.isISOControl() }) { INVALID_NAME.key }
        selections.getOrPut(player.uniqueId) { Selection() }.name = name
    }

    fun select(player: Player, first: Boolean) {
        Permissions.SELECT.require(player)
        check(ConfigFile.state.enabled && player.hasPermission(Permissions.CLAIM.permission.name)) { SELECTION_DENIED.key }
        check(player.location.blockY in player.world.minHeight until player.world.maxHeight) { BUILD_HEIGHT.key }
        val selection = selections.getOrPut(player.uniqueId) { Selection() }
        val other = if (first) selection.second else selection.first
        check(other == null || other.world == player.world) { SELECTION_WORLD.key }
        if (first) selection.first = player.location.block.location else selection.second =
            player.location.block.location
    }

    fun bounds(player: Player): ClaimBounds {
        val selection = selections[player.uniqueId] ?: error(SELECT_CORNERS.key)
        val a = selection.first ?: error(SELECT_FIRST.key)
        val b = selection.second ?: error(SELECT_SECOND.key)
        return ClaimBounds(
            a.world, BlockVector3.at(min(a.blockX, b.blockX), a.world.minHeight, min(a.blockZ, b.blockZ)),
            BlockVector3.at(max(a.blockX, b.blockX), a.world.maxHeight - 1, max(a.blockZ, b.blockZ))
        )
    }

    fun description(player: Player, first: Boolean): String? {
        val selection = selections[player.uniqueId]
        val position = if (first) selection?.first else selection?.second
        return position?.let { "${it.world.name}: ${it.blockX}, ${it.blockZ}" }
    }

    fun preview(player: Player, bounds: ClaimBounds = bounds(player)) {
        Permissions.PREVIEW.require(player)
        check(ConfigFile.state.enabled) { DISABLED.key }
        check(player.world == bounds.world) { PREVIEW_WORLD.key }
        previews.remove(player.uniqueId)?.cancel()
        val points = mutableListOf<Location>()
        val low = listOf(bounds.min.x().toDouble(), bounds.min.y().toDouble(), bounds.min.z().toDouble())
        val high = listOf(bounds.max.x() + 1.0, bounds.max.y() + 1.0, bounds.max.z() + 1.0)
        // Bounded work even for large existing claims: at most 12 * 41 particles per pulse.
        for (axis in 0..2) for (a in 0..1) for (b in 0..1) {
            val others = (0..2).filter { it != axis }
            val steps = ceil(high[axis] - low[axis]).toInt().coerceIn(1, 40)
            for (step in 0..steps) {
                val coordinates = low.toMutableList()
                coordinates[axis] = low[axis] + (high[axis] - low[axis]) * step / steps
                coordinates[others[0]] = if (a == 0) low[others[0]] else high[others[0]]
                coordinates[others[1]] = if (b == 0) low[others[1]] else high[others[1]]
                points += Location(bounds.world, coordinates[0], coordinates[1], coordinates[2])
            }
        }
        // Full-height top/bottom edges may be out of sight: include a horizontal outline
        // at the player's current elevation so the footprint is immediately visible.
        val y = player.location.y.coerceIn(low[1], high[1])
        for (axis in listOf(0, 2)) for (side in 0..1) {
            val other = if (axis == 0) 2 else 0
            val steps = ceil(high[axis] - low[axis]).toInt().coerceIn(1, 40)
            for (step in 0..steps) {
                val coordinates = low.toMutableList()
                coordinates[1] = y
                coordinates[axis] = low[axis] + (high[axis] - low[axis]) * step / steps
                coordinates[other] = if (side == 0) low[other] else high[other]
                points += Location(bounds.world, coordinates[0], coordinates[1], coordinates[2])
            }
        }
        var pulses = 0
        previews[player.uniqueId] = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            if (!player.isOnline || player.world != bounds.world || pulses++ >= 30) {
                previews.remove(player.uniqueId)?.cancel()
            } else points.filter { it.distanceSquared(player.location) <= 96.0 * 96.0 }.forEach {
                player.spawnParticle(Particle.DUST, it, 1, 0.0, 0.0, 0.0, 0.0, Particle.DustOptions(Color.LIME, 1f))
            }
        }, 0L, 10L)
    }

    fun clear(uuid: UUID) {
        selections.remove(uuid)
        previews.remove(uuid)?.cancel()
    }

    fun shutdown() {
        previews.values.forEach { it.cancel() }
        previews.clear()
        selections.clear()
    }
}
