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

package yv.tils.waypoint.config

import org.bukkit.entity.Player
import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.waypoint.Visibility
import yv.tils.waypoint.Waypoint
import yv.tils.waypoint.WaypointPolicy
import yv.tils.waypoint.WaypointState
import yv.tils.waypoint.permissions.Permissions
import java.util.*

class WaypointStore {
    private val path = "/waypoint/waypoints.json"
    var state = WaypointState()
        private set

    fun load() {
        val loaded = ObjectMapperFileUtils.load(path, WaypointState())
        require(loaded.waypoints.map { it.name.lowercase(Locale.ROOT) }.distinct().size == loaded.waypoints.size) {
            "Duplicate waypoint names"
        }
        require(loaded.waypoints.all {
            validName(it.name) && it.x.isFinite() && it.y.isFinite() && it.z.isFinite()
        }) { "Invalid waypoint data" }
        state = loaded
    }

    fun save(points: List<Waypoint>) {
        require(points.all { it.sharedWith.size <= 100 }) { "Maximum 100 shared players per waypoint" }
        val next = WaypointState(points)
        ObjectMapperFileUtils.save(path, next)
        state = next
    }

    fun find(player: Player, name: String) = state.waypoints.firstOrNull {
        it.name.equals(name, true) && WaypointPolicy.accessible(it.owner, player.uniqueId, it.visibility, it.sharedWith)
    }

    fun listed(player: Player) = state.waypoints.filter {
        WaypointPolicy.listed(it.owner, player.uniqueId, it.visibility, it.sharedWith)
    }

    fun available(name: String) = validName(name) && state.waypoints.none { it.name.equals(name, true) }
    fun create(player: Player, name: String, visibility: Visibility): Boolean {
        if (!player.hasPermission(Permissions.CREATE.permission.name)) return false
        if (!available(name) || state.waypoints.count { it.owner == player.uniqueId } >= ConfigFile.state.maxPerPlayer) return false
        val loc = player.location
        save(state.waypoints + Waypoint(name, player.uniqueId, player.world.uid, loc.x, loc.y, loc.z, visibility))
        return true
    }

    private fun validName(name: String) = name.matches(Regex("[A-Za-z0-9_-]{1,64}"))
}
