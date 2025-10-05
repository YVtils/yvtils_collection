package yv.tils.multiMine.logic

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.block.data.type.Leaves
import org.bukkit.entity.Player
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.utils.BlockUtils
import yv.tils.multiMine.utils.BlockUtils.Companion.brokenMap
import yv.tils.multiMine.utils.BlockUtils.Companion.processFinishedMap
import yv.tils.multiMine.utils.BlockUtils.Companion.runningProcessesMap
import yv.tils.multiMine.utils.ToolUtils.Companion.toolBroke
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.ArrayDeque
import java.util.HashMap
import java.util.HashSet
import java.util.function.Consumer
import kotlin.collections.set

class LeaveDecayHandler {
    fun trigger(area: BlockUtils.BreakAreaBox, origin: Location, player: Player) {
        val playerId = player.uniqueId
        if (runningProcessesMap[playerId]!! <= 0 && !processFinishedMap[playerId]!!) {
            processFinishedMap[playerId] = true

            Logger.debug("All processes finished for ${player.name}, triggering leaf decay")

            // Reset values
            runningProcessesMap[playerId] = 0
            brokenMap[playerId] = 0
            toolBroke = false

            // Trigger leaf decay across the whole affected area (with margin)
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
    }

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

        val dirs = arrayOf(
            intArrayOf(1, 0, 0), intArrayOf(-1, 0, 0),
            intArrayOf(0, 1, 0), intArrayOf(0, -1, 0),
            intArrayOf(0, 0, 1), intArrayOf(0, 0, -1),
        )

        computeAndApplyLeafDistances(world, fromX, fromY, fromZ, toX, toY, toZ, dirs)

        val queue: ArrayDeque<Triple<Int, Int, Int>> = ArrayDeque()
        val queued: HashSet<Triple<Int, Int, Int>> = HashSet()

        var seeded = 0
        for (x in fromX..toX) for (y in fromY..toY) for (z in fromZ..toZ) {
            val block = world.getBlockAt(x, y, z)
            val data = block.blockData
            if (Tag.LEAVES.isTagged(block.type) && data is Leaves && !data.isPersistent && data.distance >= 7) {
                val key = Triple(x, y, z)
                if (queued.add(key)) {
                    queue.addLast(key)
                    seeded++
                }
            }
        }

        if (seeded == 0) {
            Logger.debug("Sequential leaf-decay: none to process (distance>=7)")
            return
        }

        val animationTime = ConfigFile.config["animationTime"] as Int
        val stepDelay = (animationTime * 0.8).coerceAtLeast(1.0).toLong()

        Logger.debug("Sequential leaf-decay start: seeded=$seeded, delay=${stepDelay}t")

        Bukkit.getScheduler().runTaskTimer(Data.instance, Consumer { task ->
            if (queue.isEmpty()) {
                Logger.debug("Sequential leaf-decay finished")
                task.cancel()
                return@Consumer
            }

            val (x, y, z) = queue.removeFirst()
            val blk = world.getBlockAt(x, y, z)
            val data = blk.blockData
            if (!(Tag.LEAVES.isTagged(blk.type) && data is Leaves) || data.isPersistent) return@Consumer

            blk.breakNaturally(true, true)

            for (d in dirs) {
                val nx = x + d[0]
                val ny = y + d[1]
                val nz = z + d[2]
                if (ny !in yMinWorld..yMaxWorld) continue
                val nb = world.getBlockAt(nx, ny, nz)
                val nd = nb.blockData
                if (!(Tag.LEAVES.isTagged(nb.type) && nd is Leaves) || nd.isPersistent) continue

                val newDist = computeLocalLeafDistance(world, nx, ny, nz, dirs)
                if (newDist != nd.distance) {
                    nd.distance = newDist
                    nb.setBlockData(nd, false)
                }
                if (newDist >= 7) {
                    val key = Triple(nx, ny, nz)
                    if (queued.add(key)) queue.addLast(key)
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
        val q: ArrayDeque<Triple<Int, Int, Int>> = ArrayDeque()
        val distMap: HashMap<Triple<Int, Int, Int>, Int> = HashMap()

        for (x in fromX..toX) for (y in fromY..toY) for (z in fromZ..toZ) {
            val b = world.getBlockAt(x, y, z)
            if (Tag.LOGS.isTagged(b.type)) {
                val key = Triple(x, y, z)
                distMap[key] = 0
                q.addLast(key)
            }
        }

        while (q.isNotEmpty()) {
            val (x, y, z) = q.removeFirst()
            val currDist = distMap[Triple(x, y, z)] ?: continue
            val nextDist = currDist + 1
            if (nextDist > 7) continue

            for (d in dirs) {
                val nx = x + d[0]
                val ny = y + d[1]
                val nz = z + d[2]
                if (nx !in fromX..toX || ny !in fromY..toY || nz !in fromZ..toZ) continue
                val nb = world.getBlockAt(nx, ny, nz)
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

        var applied = 0
        for (x in fromX..toX) for (y in fromY..toY) for (z in fromZ..toZ) {
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
        Logger.debug("Leaf distances applied: $applied blocks")
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
            if (Tag.LOGS.isTagged(nb.type)) return 1
            val nd = nb.blockData
            if (Tag.LEAVES.isTagged(nb.type) && nd is Leaves) {
                val cand = (nd.distance + 1).coerceAtMost(7)
                if (cand < best) best = cand
            }
        }
        return best
    }
}
