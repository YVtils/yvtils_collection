package yv.tils.multiMine.logic

import org.bukkit.Material
import org.bukkit.block.Block
import yv.tils.multiMine.configs.ConfigFile
import java.util.UUID

// TODO: Add fast Leave decay

class LeaveDecayHandler {
    companion object {
        val decayActive = ConfigFile.config["leaveDecay.active"] as Boolean
        val decayTime = ConfigFile.config["leaveDecay.time"] as Int
        val decayInterval = ConfigFile.config["leaveDecay.interval"] as Int
    }

    fun trigger(startBlock: Block) {
        if (!decayActive) return


    }

    fun blockIsLeave(block: Block): Boolean {
        return when (block.type) {
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES,
            Material.CHERRY_LEAVES -> true
            else -> false
        }
    }
}