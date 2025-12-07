/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.multiMine.logic

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.block.data.type.Leaves
import org.bukkit.entity.Player
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.utils.BlockUtils
import yv.tils.multiMine.utils.BlockUtils.Companion.processFinishedMap
import yv.tils.multiMine.utils.ToolUtils.Companion.toolBroke
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class LeaveDecayHandler {
    companion object {
        private const val DECAY_DISTANCE_THRESHOLD_EXCLUSIVE = 6
    }

    fun trigger(area: BlockUtils.BreakAreaBox, origin: Location, player: Player) {
        val playerId = player.uniqueId

        if (BlockUtils.runningProcessesMap[playerId] == null || BlockUtils.runningProcessesMap[playerId]!!.load() > 0) {
            Logger.debug("Processes still running for ${player.name}, not triggering leaf decay yet")
            return
        }

        if (processFinishedMap[playerId]!!) {
            Logger.debug("Leaf decay already triggered for ${player.name}, skipping")
            return
        }
        processFinishedMap[playerId] = true

        Logger.debug("All processes finished for ${player.name}, triggering leaf decay")

        // Reset values
        BlockUtils.runningProcessesMap[playerId] = AtomicInt(0)
        BlockUtils.brokenMap[playerId]?.store(0)
        toolBroke = false

        LeaveDecayHandler().triggerArea(
            world = origin.world!!,
            minX = area.minX,
            minY = area.minY,
            minZ = area.minZ,
            maxX = area.maxX,
            maxY = area.maxY,
            maxZ = area.maxZ,
            margin = 6,
        )
    }

    /**
     * Triggers leaf decay in the specified area.
     * @param world The world where the area is located.
     * @param minX The minimum X coordinate of the area.
     * @param minY The minimum Y coordinate of the area.
     * @param minZ The minimum Z coordinate of the area.
     * @param maxX The maximum X coordinate of the area.
     * @param maxY The maximum Y coordinate of the area.
     * @param maxZ The maximum Z coordinate of the area.
     * @param margin The margin to expand the area for leaf decay calculation (default is 5).
     */
    fun triggerArea(
        world: World,
        minX: Int,
        minY: Int,
        minZ: Int,
        maxX: Int,
        maxY: Int,
        maxZ: Int,
        margin: Int = 5,
    ) {
        val decayActive = ConfigFile.config["leaveDecay"] as Boolean
        if (!decayActive) {
            Logger.debug("Leaf decay disabled in config")
            return
        }

        val yMinWorld = world.minHeight
        val yMaxWorld = world.maxHeight - 1

        val fromX = minX - margin
        val fromY = (minY - margin).coerceAtLeast(yMinWorld)
        val fromZ = minZ - margin
        val toX = maxX + margin
        val toY = (maxY + margin).coerceAtMost(yMaxWorld)
        val toZ = maxZ + margin

        // Queue based on server-provided data.distance; do NOT modify distances ourselves
        val queue: ArrayDeque<Triple<Int, Int, Int>> = ArrayDeque()
        val queued: HashSet<Triple<Int, Int, Int>> = HashSet()

        fun seedScan(): Int {
            var added = 0
            for (x in fromX..toX) for (y in fromY..toY) for (z in fromZ..toZ) {
                val b = world.getBlockAt(x, y, z)
                val d = b.blockData
                if (Tag.LEAVES.isTagged(b.type) && d is Leaves && !d.isPersistent && d.distance > DECAY_DISTANCE_THRESHOLD_EXCLUSIVE) {
                    val key = Triple(x, y, z)
                    if (queued.add(key)) {
                        queue.addLast(key)
                        added++
                    }
                }
            }
            return added
        }

        val initialSeeded = seedScan()

        val animationTime = ConfigFile.config["animationTime"] as Int
        val stepDelay = (animationTime * 0.8).coerceAtLeast(1.0).toLong()

        Logger.debug("Sequential leaf-decay start: seeded=$initialSeeded, delay=${stepDelay}t")

        var idleScans = 0
        val maxIdleScans = 3 // allow a few rescans to let server update leaf distances after log breaks

        Bukkit.getScheduler().runTaskTimer(Data.instance, Consumer { task ->
            if (queue.isEmpty()) {
                val added = seedScan()
                if (added == 0) {
                    idleScans++
                    if (idleScans >= maxIdleScans) {
                        Logger.debug("Sequential leaf-decay finished (no candidates after $idleScans rescans)")
                        task.cancel()
                    }
                } else {
                    idleScans = 0
                }
                return@Consumer
            }

            val (x, y, z) = queue.removeFirst()
            val blk = world.getBlockAt(x, y, z)
            val data = blk.blockData
            if (!(Tag.LEAVES.isTagged(blk.type) && data is Leaves) || data.isPersistent) return@Consumer

            // Only break if current server-provided distance still exceeds threshold
            if (data.distance > DECAY_DISTANCE_THRESHOLD_EXCLUSIVE) {
                blk.breakNaturally(true, true)

                // Enqueue 6-neighborhood neighbors based on their current distance
                for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                    if (kotlin.math.abs(dx) + kotlin.math.abs(dy) + kotlin.math.abs(dz) != 1) continue
                    val nx = x + dx
                    val ny = y + dy
                    val nz = z + dz
                    if (ny !in yMinWorld..yMaxWorld) continue
                    val nb = world.getBlockAt(nx, ny, nz)
                    val nd = nb.blockData
                    if (!(Tag.LEAVES.isTagged(nb.type) && nd is Leaves) || nd.isPersistent) continue
                    if (nd.distance > DECAY_DISTANCE_THRESHOLD_EXCLUSIVE) {
                        val key = Triple(nx, ny, nz)
                        if (queued.add(key)) queue.addLast(key)
                    }
                }
            }
        }, 1L, stepDelay)
    }
}
