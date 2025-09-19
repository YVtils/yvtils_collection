package yv.tils.multiMine.logic

import org.bukkit.Tag
import org.bukkit.block.Block
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.utils.BlockUtils

// TODO: Add fast Leave decay

class LeaveDecayHandler {
    companion object {
        val decayActive = ConfigFile.config["leaveDecay.active"] as Boolean

        val leaveBlocks = Tag.LEAVES.values.toMutableList() + Tag.WART_BLOCKS.values.toMutableList()
    }

    fun trigger(startBlock: Block) {
        if (!decayActive) return

        val loc = startBlock.location
        BlockUtils().registerBlocks(loc, leaveBlocks)
    }
}
