package yv.tils.regions.listeners

import org.bukkit.event.*
import yv.tils.regions.listeners.custom.regions.PlayerEntryRegionEvent
import yv.tils.regions.logic.PlayerChecks
import yv.tils.utils.logger.Logger

class PlayerEntryRegion : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerEntryRegionEvent) {
        Logger.debug("Player ${e.player.name} entered region ${e.newRegion.name} from region ${e.oldRegion?.name}")

        PlayerChecks.addPlayerToRegion(e.newRegion, e.player)
    }
}
