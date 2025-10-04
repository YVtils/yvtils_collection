package yv.tils.multiMine.logic

import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.block.Block
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.utils.BlockUtils
import yv.tils.utils.logger.Logger

class LeaveDecayHandler {
    fun trigger(startBlock: Block) {
        val centerLocation: Location = startBlock.location
        Logger.dev("LeaveDecayHandler trigger() at ${centerLocation.blockX}, ${centerLocation.blockY}, ${centerLocation.blockZ}")

        val decayActive = ConfigFile.config["leaveDecay"] as Boolean
        if (!decayActive) {
            Logger.dev("Leaf decay is disabled in config")
            return
        }

        // Delegate to area-based trigger with a default margin around the single block
        triggerArea(
            world = centerLocation.world!!,
            minX = centerLocation.blockX,
            minY = centerLocation.blockY,
            minZ = centerLocation.blockZ,
            maxX = centerLocation.blockX,
            maxY = centerLocation.blockY,
            maxZ = centerLocation.blockZ,
            margin = 5,
        )
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
            Logger.dev("Leaf decay is disabled in config")
            return
        }

        val leaves = Tag.LEAVES.values

        val yMinWorld = world.minHeight
        val yMaxWorld = world.maxHeight - 1

        val fromX = minX - margin
        val fromY = (minY - margin).coerceAtLeast(yMinWorld)
        val fromZ = minZ - margin
        val toX = maxX + margin
        val toY = (maxY + margin).coerceAtMost(yMaxWorld)
        val toZ = maxZ + margin

        Logger.dev("LeaveDecayHandler scanning area: [$fromX,$fromY,$fromZ] -> [$toX,$toY,$toZ]")

        var found = 0
        for (x in fromX..toX) {
            for (y in fromY..toY) {
                for (z in fromZ..toZ) {
                    val block = world.getBlockAt(x, y, z)
                    if (block.type in leaves) {
                        found++
                        BlockUtils().registerBlocks(block.location, leaves.toList())
                    }
                }
            }
        }

        Logger.dev("LeaveDecayHandler found $found leaf blocks to process in the area")
    }
}
