package yv.tils.claim.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import yv.tils.claim.listeners.custom.PlayerEntryRegion
import yv.tils.claim.listeners.custom.PlayerLeaveRegion
import yv.tils.claim.logic.RegionLogic

class PlayerMove : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerMoveEvent) {
        val from = e.from
        val to = e.to
        val player = e.player

        val oldRegion = RegionLogic.getRegion(from)
        val region = RegionLogic.getRegion(to)
        if (oldRegion == null && region != null) {
            PlayerEntryRegion(player).callEvent()
            return
        }

        if (oldRegion != null && region == null) {
            PlayerLeaveRegion(player).callEvent()
            return
        }

        if (oldRegion != null && region != null) {
            if (oldRegion != region) {
                PlayerLeaveRegion(player).callEvent()
                PlayerEntryRegion(player).callEvent()
            }
        }
    }
}