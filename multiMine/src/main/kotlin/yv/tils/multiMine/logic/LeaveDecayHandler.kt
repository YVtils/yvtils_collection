package yv.tils.multiMine.logic

import org.bukkit.Bukkit
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.block.data.type.Leaves
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.ArrayDeque
import java.util.HashMap
import java.util.HashSet
import java.util.function.Consumer

class LeaveDecayHandler {
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
            Logger.dev("Leaf decay is disabled in config")
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

        // Directions for 6-neighborhood
        val dirs = arrayOf(
            intArrayOf(1, 0, 0), intArrayOf(-1, 0, 0),
            intArrayOf(0, 1, 0), intArrayOf(0, -1, 0),
            intArrayOf(0, 0, 1), intArrayOf(0, 0, -1),
        )

        // 1) Compute and apply initial distances using BFS from logs (distance 0)
        computeAndApplyLeafDistances(world, fromX, fromY, fromZ, toX, toY, toZ, dirs)

        // 2) Build initial queue of leaves that should decay now (distance >= 7 and not persistent)
        val queue: ArrayDeque<Triple<Int, Int, Int>> = ArrayDeque()
        val queued: HashSet<Triple<Int, Int, Int>> = HashSet()

        var seeded = 0
        for (x in fromX..toX) {
            for (y in fromY..toY) {
                for (z in fromZ..toZ) {
                    val block = world.getBlockAt(x, y, z)
                    val data = block.blockData
                    if (Tag.LEAVES.isTagged(block.type) && data is Leaves && !data.isPersistent) {
                        val dist = data.distance
                        if (dist >= 7) {
                            val key = Triple(x, y, z)
                            queue.addLast(key)
                            queued.add(key)
                            seeded++
                        }
                    }
                }
            }
        }

        if (seeded == 0) {
            Logger.dev("LeaveDecayHandler: no initial decaying leaves (distance>=7) found; nothing to do")
            return
        }

        val animationTime = ConfigFile.config["animationTime"] as Int
        val stepDelay = (animationTime * 0.8).coerceAtLeast(1.0).toLong()

        Logger.dev("LeaveDecayHandler sequential decay starting. Seeded=$seeded, stepDelay=${stepDelay}t")

        // 3) Process one leaf per step; after breaking, update neighbor distances locally and enqueue if they reach 7
        Bukkit.getScheduler().runTaskTimer(Data.instance, Consumer { task ->
            if (queue.isEmpty()) {
                Logger.dev("LeaveDecayHandler sequential decay finished")
                task.cancel()
                return@Consumer
            }

            val (x, y, z) = queue.removeFirst()
            val blk = world.getBlockAt(x, y, z)
            val data = blk.blockData
            if (!(Tag.LEAVES.isTagged(blk.type) && data is Leaves)) return@Consumer

            if (data.isPersistent) return@Consumer

            // Break this leaf
            blk.breakNaturally(true, true)

            // For each 6-neighbor leaf, recompute its distance locally and update
            for (d in dirs) {
                val nx = x + d[0]
                val ny = y + d[1]
                val nz = z + d[2]
                if (ny !in yMinWorld..yMaxWorld) continue
                val nb = world.getBlockAt(nx, ny, nz)
                val nd = nb.blockData
                if (!(Tag.LEAVES.isTagged(nb.type) && nd is Leaves)) continue

                if (nd.isPersistent) continue

                val newDist = computeLocalLeafDistance(world, nx, ny, nz, dirs)
                if (newDist != nd.distance) {
                    nd.distance = newDist
                    nb.setBlockData(nd, false)
                }

                if (newDist >= 7) {
                    val key = Triple(nx, ny, nz)
                    if (!queued.contains(key)) {
                        queue.addLast(key)
                        queued.add(key)
                    }
                }
            }
        }, 1L, stepDelay)
    }

    private fun computeAndApplyLeafDistances(
        world: World,
        fromX: Int,
        fromY: Int,
        fromZ: Int,
        toX: Int,
        toY: Int,
        toZ: Int,
        dirs: Array<IntArray>,
    ) {
        // BFS from logs (distance 0); propagate through leaves up to distance 7
        val q: ArrayDeque<Triple<Int, Int, Int>> = ArrayDeque()
        val distMap: HashMap<Triple<Int, Int, Int>, Int> = HashMap()

        // Seed logs as distance 0
        for (x in fromX..toX) {
            for (y in fromY..toY) {
                for (z in fromZ..toZ) {
                    val b = world.getBlockAt(x, y, z)
                    if (Tag.LOGS.isTagged(b.type)) {
                        val key = Triple(x, y, z)
                        distMap[key] = 0
                        q.addLast(key)
                    }
                }
            }
        }

        // Propagate to leaves
        while (q.isNotEmpty()) {
            val (x, y, z) = q.removeFirst()
            val currDist = distMap[Triple(x, y, z)] ?: continue
            val nextDist = currDist + 1
            if (nextDist > 7) continue

            for (d in dirs) {
                val nx = x + d[0]
                val ny = y + d[1]
                val nz = z + d[2]
                if (nx !in fromX..toX || ny < fromY || ny > toY || nz < fromZ || nz > toZ) continue
                val nb = world.getBlockAt(nx, ny, nz)

                // Only traverse into leaves
                val nbd = nb.blockData
                if (!(Tag.LEAVES.isTagged(nb.type) && nbd is Leaves)) continue

                val key = Triple(nx, ny, nz)
                val old = distMap[key]
                if (old == null || nextDist < old) {
                    distMap[key] = nextDist
                    q.addLast(key)
                }
            }
        }

        // Apply distances to leaves (default 7 if unreachable)
        var applied = 0
        for (x in fromX..toX) {
            for (y in fromY..toY) {
                for (z in fromZ..toZ) {
                    val b = world.getBlockAt(x, y, z)
                    val bd = b.blockData
                    if (!(Tag.LEAVES.isTagged(b.type) && bd is Leaves)) continue
                    val key = Triple(x, y, z)
                    val d = distMap[key] ?: 7
                    if (bd.distance != d) {
                        bd.distance = d
                        b.setBlockData(bd, false)
                        applied++
                    }
                }
            }
        }
        Logger.dev("LeaveDecayHandler: applied initial leaf distances to $applied blocks")
    }

    private fun computeLocalLeafDistance(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        dirs: Array<IntArray>,
    ): Int {
        var best = 7
        for (d in dirs) {
            val nx = x + d[0]
            val ny = y + d[1]
            val nz = z + d[2]
            val nb = world.getBlockAt(nx, ny, nz)
            if (Tag.LOGS.isTagged(nb.type)) {
                return 1 // Adjacent to a log
            }
            val nd = nb.blockData
            if (Tag.LEAVES.isTagged(nb.type) && nd is Leaves) {
                val cand = (nd.distance + 1).coerceAtMost(7)
                if (cand < best) best = cand
            }
        }
        return best
    }
}
