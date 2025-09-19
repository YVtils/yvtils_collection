package yv.tils.multiMine.logic

import org.bukkit.Material
import org.bukkit.Tag
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
        val materials = Tag.LEAVES
        return materials.isTagged(block.type)
    }
}