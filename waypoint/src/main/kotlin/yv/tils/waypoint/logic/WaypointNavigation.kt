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

package yv.tils.waypoint.logic

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import yv.tils.utils.colors.Colors
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.stats.ModuleTelemetry
import yv.tils.waypoint.Waypoint
import yv.tils.waypoint.config.ConfigFile
import yv.tils.waypoint.language.WaypointText
import yv.tils.waypoint.permissions.Permissions
import java.util.*

class WaypointNavigation {
    private data class Run(val point: Waypoint, val trail: BlockTrailDisplay = BlockTrailDisplay())

    private val runs = mutableMapOf<UUID, Run>()
    private var ticker: BukkitTask? = null

    fun start() {
        ticker = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable { tick() }, 1L, 10L)
    }

    fun toggle(player: Player, point: Waypoint) {
        val same = runs[player.uniqueId]?.point == point
        stop(player)
        if (same) WaypointText.send(player, "stopped")
        else {
            runs[player.uniqueId] = Run(point)
            ModuleTelemetry.record(ModuleTelemetry.Event.WAYPOINT_NAVIGATIONS_STARTED)
            WaypointText.send(player, "started", mapOf("waypoint" to point.name))
            tick()
        }
    }

    fun stop(player: Player) {
        runs.remove(player.uniqueId)?.trail?.clear()
        player.sendActionBar(Component.empty())
    }

    fun remove(point: Waypoint) {
        runs.filterValues { it.point == point }.keys.toList().forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let(::stop) ?: runs.remove(uuid)?.trail?.clear()
        }
    }

    private fun tick() {
        runs.toMap().forEach { (uuid, run) ->
            val player = Bukkit.getPlayer(uuid)
            if (player == null) {
                runs.remove(uuid)?.trail?.clear()
                return@forEach
            }
            if (player.world.uid != run.point.world) {
                stop(player)
                return@forEach
            }
            if (!player.hasPermission(Permissions.USE.permission.name)) {
                stop(player)
                return@forEach
            }
            val target = Location(player.world, run.point.x, run.point.y, run.point.z)
            val distance = player.location.distance(target)
            if (distance <= ConfigFile.state.arrivalDistance) {
                stop(player)
                WaypointText.send(player, "arrived")
            } else {
                try {
                    run.trail.update(player, target)
                } catch (error: Exception) {
                    stop(player)
                    Logger.error("Waypoint trail failed for $uuid", error)
                    return@forEach
                }
                val arrow = TerrainTrail.direction(
                    player.location.yaw,
                    target.x - player.location.x,
                    target.z - player.location.z
                )
                player.sendActionBar(
                    Component.text(
                        "$arrow ${run.point.name}: ${distance.toInt()} m",
                        TextColor.fromHexString(Colors.SECONDARY.color)
                    )
                )
            }
        }
    }

    fun close() {
        ticker?.cancel()
        runs.values.forEach { it.trail.clear() }
        runs.keys.forEach { Bukkit.getPlayer(it)?.sendActionBar(Component.empty()) }
        runs.clear()
    }
}
