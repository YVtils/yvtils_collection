package yv.tils.regions.listeners

import org.bukkit.event.*
import yv.tils.regions.listeners.custom.regions.PlayerLeaveRegionEvent
import yv.tils.regions.logic.PlayerChecks
import yv.tils.utils.logger.Logger

class PlayerLeaveRegion : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerLeaveRegionEvent) {
        Logger.debug("Player ${e.player.name} left region ${e.oldRegion.name} to region ${e.newRegion?.name}")

        PlayerChecks.removePlayerFromRegion(e.oldRegion, e.player)
    }
}
