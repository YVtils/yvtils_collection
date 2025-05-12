package yv.tils.regions.listeners.cause

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent

class PlayerItemFrameChange : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerItemFrameChangeEvent) {
        val player = e.player
        val location = e.itemFrame.location
        val block = location.block
        val region = yv.tils.regions.logic.RegionLogic.getRegion(location) ?: return

        val flagTrigger = BlockFlagTriggerEvent(player, block, region, Flag.INTERACT)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}