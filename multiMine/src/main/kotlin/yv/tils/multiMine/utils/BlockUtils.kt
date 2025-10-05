package yv.tils.multiMine.utils

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.type.Leaves
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.logic.LeaveDecayHandler
import yv.tils.multiMine.utils.ToolUtils.Companion.toolBroke
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.*

class BlockUtils {
    companion object {
        val animationTime = ConfigFile.config["animationTime"] as Int
        val breakLimit = ConfigFile.config["breakLimit"] as Int
        val blocks = ConfigFile.blockList

        val brokenMap: MutableMap<UUID, Int> = mutableMapOf()
        val processFinishedMap: MutableMap<UUID, Boolean> = mutableMapOf()
        val runningProcessesMap: MutableMap<UUID, Int> = mutableMapOf()
    }

    // Tracks the affected log area for a multimine run
    data class BreakAreaBox(
        var minX: Int,
        var minY: Int,
        var minZ: Int,
        var maxX: Int,
        var maxY: Int,
        var maxZ: Int,
    ) {
        fun include(x: Int, y: Int, z: Int) {
            if (x < minX) minX = x
            if (y < minY) minY = y
            if (z < minZ) minZ = z
            if (x > maxX) maxX = x
            if (y > maxY) maxY = y
            if (z > maxZ) maxZ = z
        }
    }

    /**
     * Breaks the blocks
     * @param block The block to break
     * @param player The player who is breaking the block
     * @param item The item the player is using to break the block
     * @param blockList The list of blocks that can be broken
     * @return true if the block was broken
     */
    private fun breakBlock(block: Block, player: Player, item: ItemStack, blockList: List<Material>): Boolean {
        // Initialize player's broken count if not exists
        if (!brokenMap.containsKey(player.uniqueId)) {
            brokenMap[player.uniqueId] = 0
        }

        // Check break limit BEFORE attempting to break
        if (brokenMap[player.uniqueId]!! >= breakLimit) {
            Logger.dev("Break limit reached for player ${player.name}: ${brokenMap[player.uniqueId]}/$breakLimit")
            return false
        }

        if (checkBlock(block.type, blockList) && ToolUtils().checkTool(block, item)) {
            if (brokenMap[player.uniqueId]!! != 0) {
                try {
                    if (toolBroke) return false

                    if (ToolUtils().damageTool(player, 1, item)) {
                        return false
                    }
                } catch (_: NullPointerException) {
                    return false
                }
            }

            brokenMap[player.uniqueId] = brokenMap[player.uniqueId]!! + 1
            Logger.dev("Player ${player.name} broke block. Count: ${brokenMap[player.uniqueId]}/$breakLimit")

            block.breakNaturally(item, true, true)
            return true
        }
        return false
    }

    /**
     * Breaks the blocks without player or item context
     * Used for recursive breaking without tool damage or limits
     * @param block The block to break
     * @param blockList The list of blocks that can be broken
     * @return true if the block was broken
     */
    private fun breakBlock(block: Block, blockList: List<Material>): Boolean {
        if (checkBlock(block.type, blockList)) {
            block.breakNaturally(true, true)
            return true
        }
        return false
    }

    /**
     * Registers the blocks to be broken
     * @param loc The location of the block to start breaking from
     * @param player The player who is breaking the blocks
     * @param item The item the player is using to break the blocks
     * @param customBlockList The list of blocks that can be broken, defaults to the config list
     * @param topLevel Whether this is the initial (top-level) invocation
     * @param root The root location for the breaking process, used for recursive calls
     * @param box The bounding box of the area being broken, used for leaf decay
     */
    fun registerBlocks(
        loc: Location,
        player: Player,
        item: ItemStack,
        customBlockList: List<Material> = blocks,
        topLevel: Boolean = true,
        root: Location? = null,
        box: BreakAreaBox? = null,
    ) {
        val playerId = player.uniqueId
        val origin = root ?: loc
        val area = box ?: BreakAreaBox(loc.blockX, loc.blockY, loc.blockZ, loc.blockX, loc.blockY, loc.blockZ)

        // Initialize player's counters if not exists
        if (!brokenMap.containsKey(playerId)) {
            brokenMap[playerId] = 0
            Logger.dev("BREAK_LIMIT: Initialized counter for ${player.name} to 0")
        }
        if (!runningProcessesMap.containsKey(playerId)) {
            runningProcessesMap[playerId] = 0
        }
        if (topLevel) {
            processFinishedMap[playerId] = false
        } else if (!processFinishedMap.containsKey(playerId)) {
            processFinishedMap[playerId] = false
        }

        Logger.dev("BREAK_LIMIT: Player ${player.name} starting multimine. Current count: ${brokenMap[playerId]}/$breakLimit")

        // Check break limit BEFORE starting any processes
        if (brokenMap[playerId]!! >= breakLimit) {
            Logger.dev("BREAK_LIMIT: STOPPING - Break limit already reached for player ${player.name}: ${brokenMap[playerId]}/$breakLimit")
            return
        }

        var tasksScheduled = 0
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    val newLoc = Location(loc.world, loc.x + x, loc.y + y, loc.z + z)
                    val newBlock = newLoc.block

                    // Only schedule tasks for eligible neighbor blocks (logs), skip others
                    if (!checkBlock(newBlock.type, customBlockList)) continue
                    if (!ToolUtils().checkTool(newBlock, item)) continue

                    synchronized(runningProcessesMap) {
                        runningProcessesMap[playerId] = runningProcessesMap[playerId]!! + 1
                    }
                    tasksScheduled++

                    Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                        try {
                            if (breakBlock(newBlock, player, item, customBlockList)) {
                                // Update area with the successfully broken log
                                area.include(newLoc.blockX, newLoc.blockY, newLoc.blockZ)
                                // Schedule neighbors recursively (not top-level)
                                registerBlocks(newLoc, player, item, customBlockList, topLevel = false, root = origin, box = area)
                            }
                        } finally {
                            // Always decrement when this scheduled task completes
                            synchronized(runningProcessesMap) {
                                runningProcessesMap[playerId] = runningProcessesMap[playerId]!! - 1

                                Logger.dev("BREAK_LIMIT: Task finished for ${player.name}. Remaining: ${runningProcessesMap[playerId]}")

                                if (runningProcessesMap[playerId]!! <= 0 && !processFinishedMap[playerId]!!) {
                                    processFinishedMap[playerId] = true

                                    Logger.dev("BREAK_LIMIT: All processes finished for ${player.name}, resetting values and triggering leaf decay")

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
                        }
                    }, animationTime * 1L)
                }
            }
        }

        Logger.dev("BREAK_LIMIT: Scheduled $tasksScheduled tasks for ${player.name}")

        // If nothing was scheduled at top-level, finalize immediately if no tasks are running
        if (tasksScheduled == 0 && topLevel) {
            synchronized(runningProcessesMap) {
                if (runningProcessesMap[playerId]!! <= 0 && !processFinishedMap[playerId]!!) {
                    processFinishedMap[playerId] = true

                    Logger.dev("BREAK_LIMIT: No tasks scheduled for ${player.name}. Finalizing and triggering leaf decay immediately")

                    runningProcessesMap[playerId] = 0
                    brokenMap[playerId] = 0
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
            }
        }
    }

    /**
     * Registers the blocks to be broken without player or item context
     * Used for recursive breaking without tool damage or limits
     * @param loc The location of the block to start breaking from
     * @param customBlockList The list of blocks that can be broken, defaults to the config list
     */
    fun registerBlocks(loc: Location, customBlockList: List<Material> = blocks) {
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    val newLoc = Location(loc.world, loc.x + x, loc.y + y, loc.z + z)
                    val newBlock = newLoc.block

                    if (newBlock.type in customBlockList) {
                        // Handle leaf blocks (have distance and persistence properties)
                        if (newBlock.blockData is Leaves) {
                            val newBlockAsLeave = newBlock.blockData as Leaves

                            // Check if the leaf should decay (not persistent and far from logs)
                            if (!newBlockAsLeave.isPersistent && newBlockAsLeave.distance >= 4) {
                                // TEMP: Make leaf decay delay much bigger to verify delay behavior
                                val tempDelayTicks = ((animationTime.toDouble() * 5.0).coerceAtLeast(40.0)).toLong()
                                Logger.dev("Fast leaf-decay TEMP delay: $tempDelayTicks ticks")
                                Bukkit.getScheduler().runTaskLater(
                                    Data.instance,
                                    Runnable {
                                        if (breakBlock(newBlock, customBlockList)) {
                                            // Continue decay process
                                            registerBlocks(newLoc, customBlockList)
                                        }
                                    },
                                    tempDelayTicks
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the block is in the list of blocks
     * @param material The material to check
     * @param blocks The list of blocks to check against
     * @return true if the block is in the list
     */
    fun checkBlock(material: Material, blocks: List<Material>): Boolean {
        return blocks.contains(material)
    }
}