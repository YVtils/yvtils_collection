package yv.tils.waypoint.logic

import org.bukkit.Location
import org.bukkit.block.Block
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.round

/** A bounded direction guide over nearby walkable terrain, not obstacle-avoiding pathfinding. */
object TerrainTrail {
    const val MAX_BLOCKS = 32

    fun blocks(start: Location, target: Location, limit: Int = MAX_BLOCKS): List<Block> {
        if (start.world != target.world) return emptyList()
        val world = start.world
        val dx = target.x - start.x
        val dz = target.z - start.z
        val distance = hypot(dx, dz)
        if (distance < 0.1) return emptyList()
        val result = linkedSetOf<Block>()
        var surface = (start.blockY - 1).coerceIn(world.minHeight, world.maxHeight - 3)
        for (step in 1..min(limit.coerceIn(1, 64), distance.toInt())) {
            val x = floor(start.x + dx / distance * step).toInt()
            val z = floor(start.z + dz / distance * step).toInt()
            if (!world.isChunkLoaded(x shr 4, z shr 4)) break
            val candidates = (surface - 3..surface + 2).filter { it in world.minHeight until world.maxHeight - 2 }
            val block = candidates.sortedBy { abs(it - surface) }.firstNotNullOfOrNull { y ->
                world.getBlockAt(x, y, z).takeIf {
                    it.type.isSolid && !it.isLiquid && it.getRelative(0, 1, 0).isPassable && it.getRelative(
                        0,
                        2,
                        0
                    ).isPassable
                }
            } ?: break
            surface = block.y
            result.add(block)
        }
        return result.toList()
    }

    fun direction(yaw: Float, dx: Double, dz: Double): String {
        val bearing = Math.toDegrees(atan2(-dx, dz))
        val angle = (((bearing - yaw) % 360 + 540) % 360) - 180
        val arrows = arrayOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")
        return arrows[(round(angle / 45).toInt() + 8) % 8]
    }
}
