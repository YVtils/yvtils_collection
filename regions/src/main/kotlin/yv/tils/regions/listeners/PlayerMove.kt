package yv.tils.regions.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import yv.tils.regions.listeners.custom.PlayerEntryRegionEvent
import yv.tils.regions.listeners.custom.PlayerLeaveRegionEvent
import yv.tils.regions.logic.RegionLogic

class PlayerMove : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerMoveEvent) {
        val from = e.from
        val to = e.to
        val player = e.player

        val oldRegion = RegionLogic.getRegion(from)
        val region = RegionLogic.getRegion(to)
        if (oldRegion == null && region != null) {
            val playerEntryRegion = PlayerEntryRegionEvent(player, null, region)
            playerEntryRegion.callEvent()
            if (playerEntryRegion.isCancelled) {
                e.isCancelled = true
            }
            return
        }

        if (oldRegion != null && region == null) {
            val playerLeaveRegion = PlayerLeaveRegionEvent(player, oldRegion, null)
            playerLeaveRegion.callEvent()
            if (playerLeaveRegion.isCancelled) {
                e.isCancelled = true
            }
            return
        }

        if (oldRegion != null && region != null) {
            if (oldRegion != region) {
                val playerLeaveRegion = PlayerLeaveRegionEvent(player, oldRegion, region)
                playerLeaveRegion.callEvent()
                if (playerLeaveRegion.isCancelled) {
                    e.isCancelled = true
                    return
                }
                val playerEntryRegion = PlayerEntryRegionEvent(player, oldRegion, region)
                playerEntryRegion.callEvent()
                if (playerEntryRegion.isCancelled) {
                    e.isCancelled = true
                    return
                }
            }
        }
    }
}