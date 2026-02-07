/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.listeners.custom.regions

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import yv.tils.regions.data.RegionManager

/**
 * This event is triggered when a player enters a region.
 *
 * @param player The player who entered the region.
 * @param oldRegion The region the player was in before entering the new region, or null if there was no previous region.
 * @param newRegion The new region the player has entered.
 */
class PlayerEntryRegionEvent(val player: Player, val oldRegion: RegionManager.RegionData?, val newRegion: RegionManager.RegionData) : Event(), Cancellable {
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