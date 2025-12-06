/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.listeners.cause

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class BlockPlace: Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEvent(e: BlockPlaceEvent) {
        val player = e.player
        val block = e.block
        val region = RegionLogic.getRegion(block.location) ?: return

        val flagTrigger = BlockFlagTriggerEvent(player, block, region, Flag.PLACE)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}