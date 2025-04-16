package yv.tils.regions.listeners

import logger.Logger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.regions.listeners.custom.PlayerEntryRegionEvent
import yv.tils.regions.logic.PlayerChecks

class PlayerEntryRegion : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerEntryRegionEvent) {
        Logger.debug("Player ${e.player.name} entered region ${e.newRegion.name} from region ${e.oldRegion?.name}")
        Logger.dev("Player ${e.player.name} entered region ${e.newRegion.name} from region ${e.oldRegion?.name}")

        PlayerChecks.addPlayerToRegion(e.newRegion, e.player)
    }
}