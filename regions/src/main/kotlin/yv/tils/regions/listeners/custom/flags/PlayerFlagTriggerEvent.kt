package yv.tils.regions.listeners.custom.flags

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import yv.tils.regions.data.Flag
import yv.tils.regions.data.RegionManager

/**
 * This event is triggered when a flag is triggered in a region.
 *
 * @param player The player who triggered the flag.
 * @param target The target player who is affected by the flag.
 * @param playerRegion The region where the player is located.
 * @param targetRegion The region where the target player is located.
 * @param flag The type of flag that was triggered.
 */
class PlayerFlagTriggerEvent(val player: Player, val target: Player?, val playerRegion: RegionManager.RegionData?, val targetRegion: RegionManager.RegionData?, val flag: Flag): Event(), Cancellable {
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