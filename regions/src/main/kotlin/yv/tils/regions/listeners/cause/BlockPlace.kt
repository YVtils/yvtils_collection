package yv.tils.regions.listeners.cause

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import yv.tils.regions.data.FlagType
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class BlockPlace: Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEvent(e: BlockPlaceEvent) {
        val player = e.player
        val block = e.block
        val region = RegionLogic.getRegion(block.location) ?: return

        val flagTrigger = BlockFlagTriggerEvent(player, block, region, FlagType.PLACE)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}