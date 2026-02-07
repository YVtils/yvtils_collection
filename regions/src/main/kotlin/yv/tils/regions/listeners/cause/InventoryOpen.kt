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

package yv.tils.regions.listeners.cause

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class InventoryOpen : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: InventoryOpenEvent) {
        val player = e.player as? Player ?: return
        val location = e.inventory.location ?: return
        val region = RegionLogic.getRegion(location) ?: return

        val flagTrigger = BlockFlagTriggerEvent(player, location.block, region, Flag.CONTAINER)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}