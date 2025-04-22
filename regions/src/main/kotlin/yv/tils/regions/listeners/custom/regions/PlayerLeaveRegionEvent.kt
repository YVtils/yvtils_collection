package yv.tils.regions.listeners.custom.regions

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import yv.tils.regions.data.RegionManager

/**
 * This event is triggered when a player leaves a region.
 *
 * @param player The player who left the region.
 * @param oldRegion The region the player was in before leaving.
 * @param newRegion The region the player has entered after leaving the old region, or null if there is no new region.
 */
class PlayerLeaveRegionEvent(val player: Player, val oldRegion: RegionManager.RegionData, val newRegion: RegionManager.RegionData?) : Event(), Cancellable {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    private var cancelled: Boolean = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}