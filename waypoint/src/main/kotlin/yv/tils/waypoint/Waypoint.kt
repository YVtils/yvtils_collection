package yv.tils.waypoint

import java.util.UUID

enum class Visibility { PUBLIC, PRIVATE, UNLISTED, SHARED }

data class Waypoint(
    val name: String,
    val owner: UUID,
    val world: UUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val visibility: Visibility,
    val sharedWith: Set<UUID> = emptySet(),
)

data class WaypointState(val waypoints: List<Waypoint> = emptyList())
