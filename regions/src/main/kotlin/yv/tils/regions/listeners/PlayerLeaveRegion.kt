package yv.tils.regions.listeners

import logger.Logger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.regions.listeners.custom.regions.PlayerLeaveRegionEvent
import yv.tils.regions.logic.PlayerChecks

class PlayerLeaveRegion : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerLeaveRegionEvent) {
        Logger.debug("Player ${e.player.name} left region ${e.oldRegion.name} to region ${e.newRegion?.name}")

        PlayerChecks.removePlayerFromRegion(e.oldRegion, e.player)
    }
}