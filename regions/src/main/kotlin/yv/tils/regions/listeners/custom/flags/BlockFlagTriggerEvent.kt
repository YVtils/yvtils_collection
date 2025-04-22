package yv.tils.regions.listeners.custom.flags

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import yv.tils.regions.data.FlagType
import yv.tils.regions.data.RegionManager

/**
 * This event is triggered when a flag is triggered in a region.
 *
 * @param player The player who triggered the flag.
 * @param block The block that triggered the flag.
 * @param region The region where the flag was triggered.
 * @param flagType The type of flag that was triggered.
 */
class BlockFlagTriggerEvent(val player: Player, val block: Block, val region: RegionManager.RegionData, val flagType: FlagType): Event(), Cancellable {
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