package yv.tils.multiMine.utils

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.type.Leaves
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.logic.LeaveDecayHandler
import yv.tils.multiMine.utils.ToolUtils.Companion.toolBroke
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.*

// TODO: Try making the break process better for the performance

class BlockUtils {
    companion object {
        val animationTime = ConfigFile.config["animationTime"] as Int
        val breakLimit = ConfigFile.config["breakLimit"] as Int
        val blocks = ConfigFile.blockList

        val brokenMap: MutableMap<UUID, Int> = mutableMapOf()
        val processFinishedMap: MutableMap<UUID, Boolean> = mutableMapOf()
        val runningProcessesMap: MutableMap<UUID, Int> = mutableMapOf()
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
        var taskSuccess = false

        Bukkit.getScheduler().runTask(Data.instance,  Runnable {
            if (! brokenMap.containsKey(player.uniqueId)) {
                brokenMap[player.uniqueId] = 0
            }

            if (brokenMap[player.uniqueId] !! >= breakLimit) {
                return@Runnable
            }

            if (checkBlock(block.type, blockList) && ToolUtils().checkTool(block, item)) {
                if (brokenMap[player.uniqueId]!! != 0) {
                    try {
                        if (toolBroke) return@Runnable

                        if (ToolUtils().damageTool(player, 1, item)) {
                            return@Runnable
                        }
                    } catch (_: NullPointerException) {
                        return@Runnable
                    }
                }

                brokenMap[player.uniqueId] = brokenMap[player.uniqueId]!! + 1

                block.breakNaturally(item, true, true)
                taskSuccess = true
                return@Runnable
            }
            return@Runnable
        })

        return taskSuccess
    }

    /**
     * Breaks the blocks without player or item context
     * Used for recursive breaking without tool damage or limits
     * @param block The block to break
     * @param blockList The list of blocks that can be broken
     * @return true if the block was broken
     */
    private fun breakBlock(block: Block, blockList: List<Material>): Boolean {
        var taskSuccess = false

        Bukkit.getScheduler().runTask(Data.instance,  Runnable {

            if (checkBlock(block.type, blockList)) {
                block.breakNaturally(true, true)
                taskSuccess = true
                return@Runnable
            }
            return@Runnable
        })

        return taskSuccess
    }

    /**
     * Registers the blocks to be broken
     * @param loc The location of the block to start breaking from
     * @param player The player who is breaking the blocks
     * @param item The item the player is using to break the blocks
     * @param customBlockList The list of blocks that can be broken, defaults to the config list
     */
    fun registerBlocks(loc: Location, player: Player, item: ItemStack, customBlockList: List<Material> = blocks) {
        CoroutineHandler.launchTask(
            task = {
                val playerId = player.uniqueId

                if (! brokenMap.containsKey(playerId)) {
                    brokenMap[playerId] = 0
                }
                if (! runningProcessesMap.containsKey(playerId)) {
                    runningProcessesMap[playerId] = 0
                }

                if (brokenMap[playerId] !! >= breakLimit) {
                    LeaveDecayHandler().trigger(loc.block)

                    return@launchTask
                }

                if (! processFinishedMap.containsKey(playerId)) {
                    processFinishedMap[playerId] = false
                }

                var tasksScheduled = 0
                for (x in - 1 .. 1) {
                    for (y in - 1 .. 1) {
                        for (z in - 1 .. 1) {
                            if (x == 0 && y == 0 && z == 0) continue
                            val newLoc = Location(loc.world, loc.x + x, loc.y + y, loc.z + z)
                            val newBlock = newLoc.block

                            runningProcessesMap[playerId] = runningProcessesMap[playerId] !! + 1
                            tasksScheduled ++

                            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                                if (!breakBlock(newBlock, player, item, customBlockList)) {
                                    synchronized(runningProcessesMap) {
                                        runningProcessesMap[playerId] = runningProcessesMap[playerId] !! - 1

                                        if (runningProcessesMap[playerId] !! <= 0 && ! processFinishedMap[playerId] !!) {
                                            runningProcessesMap[playerId] = 0
                                            brokenMap[playerId] = 0
                                            toolBroke = false
                                            processFinishedMap[playerId] = true

                                            LeaveDecayHandler().trigger(loc.block)

                                            processFinishedMap[playerId] = false
                                        }
                                    }

                                    return@Runnable
                                } else {
                                    registerBlocks(newLoc, player, item, customBlockList)
                                }
                            }, animationTime * 1L)
                        }
                    }
                }
            }
        )
    }

    /**
     * Registers the blocks to be broken without player or item context
     * Used for recursive breaking without tool damage or limits
     * @param loc The location of the block to start breaking from
     * @param customBlockList The list of blocks that can be broken, defaults to the config list
     */
    fun registerBlocks(loc: Location, customBlockList: List<Material> = blocks) {
        CoroutineHandler.launchTask(
            task = {
                for (x in -1..1) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            if (x == 0 && y == 0 && z == 0) continue
                            val newLoc = Location(loc.world, loc.x + x, loc.y + y, loc.z + z)
                            val newBlock = newLoc.block

                            if (newBlock.type in customBlockList) {
                                if (newBlock.blockData is Leaves) {
                                    val newBlockAsLeave = newBlock.blockData as Leaves

                                    if (!newBlockAsLeave.isPersistent && newBlockAsLeave.distance >= 4) {
                                        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                                            if (breakBlock(newBlock, customBlockList)) {
                                                registerBlocks(newLoc, customBlockList)
                                            }
                                        }, (animationTime * 0.8).toLong())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
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
