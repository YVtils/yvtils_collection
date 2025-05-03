package yv.tils.regions.listeners.cause

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class PlayerInteract: Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerInteractEvent) {
        val player = e.player
        val block = e.clickedBlock ?: return
        val region = RegionLogic.getRegion(block.location) ?: return

        val flagTrigger = BlockFlagTriggerEvent(player, block, region, Flag.INTERACT)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}