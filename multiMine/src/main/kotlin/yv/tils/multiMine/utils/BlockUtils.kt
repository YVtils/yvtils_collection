package yv.tils.multiMine.utils

import coroutine.CoroutineHandler
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import yv.tils.multiMine.logic.MultiMineHandler.Companion.animationTime
import yv.tils.multiMine.logic.MultiMineHandler.Companion.blocks
import yv.tils.multiMine.logic.MultiMineHandler.Companion.breakLimit
import yv.tils.multiMine.logic.MultiMineHandler.Companion.brokenMap
import yv.tils.multiMine.utils.ToolUtils.Companion.toolBroke

class BlockUtils {
    /**
     * Breaks the blocks
     * @param block The block to break
     * @param player The player who is breaking the block
     * @param item The item the player is using to break the block
     * @return true if the block was broken
     */
    private fun breakBlock(block: Block, player: Player, item: ItemStack): Boolean {
        if (checkBlock(block.type, blocks) && checkTool(block, item)) {
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
     */
    fun registerBlocks(loc: Location, player: Player, item: ItemStack) {
        if (brokenMap[player.uniqueId]!! >= breakLimit) {
            return
        }

        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    val newLoc = Location(loc.world, loc.x + x, loc.y + y, loc.z + z)
                    val newBlock = newLoc.block

//                    Bukkit.getScheduler().runTaskLater(Data.instance, Runnable { // TODO: Test if switchable to coroutine
//                        if (!breakBlock(newBlock, player, item)) {
//                            return@Runnable
//                        } else {
//                            registerBlocks(newLoc, player, item)
//                        }
//                    }, animationTime * 1L)

                    CoroutineHandler.launchTask({
                        if (!breakBlock(newBlock, player, item)) {
                            return@launchTask
                        } else {
                            registerBlocks(newLoc, player, item)
                        }
                    }, "yvtils-multiMine-breakBlock", animationTime * 1L)
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