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

package yv.tils.multiMine.utils

import org.bukkit.*
import org.bukkit.block.Block
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
        val matchBlockTypeOnly = ConfigFile.config["matchBlockTypeOnly"] as Boolean
        val blocks = ConfigFile.blockList

        val brokenMap: MutableMap<UUID, Int> = mutableMapOf()
        val processFinishedMap: MutableMap<UUID, Boolean> = mutableMapOf()
        val runningProcessesMap: MutableMap<UUID, Int> = mutableMapOf()
        val playerBlockTypeMap: MutableMap<UUID, Material> = mutableMapOf()
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
            Logger.debug("Break limit reached for ${player.name}: ${brokenMap[player.uniqueId]}/$breakLimit")
            return false
        }

        if (checkBlock(block.type, blockList, player) && ToolUtils().checkTool(block, item)) {
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
            Logger.debug("Block broken by ${player.name}. Count: ${brokenMap[player.uniqueId]}/$breakLimit")

            block.breakNaturally(item, true, true)
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
            Logger.debug("Init counter for ${player.name}")
        }
        if (!runningProcessesMap.containsKey(playerId)) {
            runningProcessesMap[playerId] = 0
        }
        if (topLevel) {
            processFinishedMap[playerId] = false
        } else if (!processFinishedMap.containsKey(playerId)) {
            processFinishedMap[playerId] = false
        }

        Logger.debug("Start multimine for ${player.name}: ${brokenMap[playerId]}/$breakLimit")

        // Check break limit BEFORE starting any processes
        if (brokenMap[playerId]!! >= breakLimit) {
            Logger.debug("Stop multimine (limit) for ${player.name}: ${brokenMap[playerId]}/$breakLimit")
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
                                area.include(newLoc.blockX, newLoc.blockY, newLoc.blockZ)
                                registerBlocks(
                                    newLoc,
                                    player,
                                    item,
                                    customBlockList,
                                    topLevel = false,
                                    root = origin,
                                    box = area
                                )
                            }
                        } finally {
                            synchronized(runningProcessesMap) {
                                runningProcessesMap[playerId] = runningProcessesMap[playerId]!! - 1

                                Logger.debug("Task finished for ${player.name}. Remaining: ${runningProcessesMap[playerId]}")

                                LeaveDecayHandler().trigger(
                                    area = area,
                                    origin = origin,
                                    player = player
                                )
                            }
                        }
                    }, animationTime * 1L)
                }
            }
        }

        Logger.debug("Scheduled $tasksScheduled tasks for ${player.name}")

        // If nothing was scheduled at top-level, finalize immediately if no tasks are running
        if (tasksScheduled == 0 && topLevel) {
            synchronized(runningProcessesMap) {
                LeaveDecayHandler().trigger(
                    area = area,
                    origin = origin,
                    player = player
                )
            }
        }
    }

    /**
     * Checks if the block is in the provided list of blocks
     * @param material The material to check
     * @param blocks The list of blocks to check against
     * @return true if the block is in the provided list
     */
    fun checkBlock(material: Material, blocks: List<Material>, player: Player? = null): Boolean {
        if (player == null) {
            return blocks.contains(material)
        }

        if (matchBlockTypeOnly) {
            val playerId = player.uniqueId
            if (!playerBlockTypeMap.containsKey(playerId) && blocks.contains(material)) {
                playerBlockTypeMap[playerId] = material
                Logger.debug("Set block type for ${player.name} to $material")
            }

            return material == playerBlockTypeMap[playerId]
        }

        return blocks.contains(material)
    }
}