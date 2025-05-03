package yv.tils.regions.listeners.cause

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class BlockBreak: Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        val region = RegionLogic.getRegion(e.block.location) ?: return

        val flagTrigger = BlockFlagTriggerEvent(player, block, region, Flag.DESTROY)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}