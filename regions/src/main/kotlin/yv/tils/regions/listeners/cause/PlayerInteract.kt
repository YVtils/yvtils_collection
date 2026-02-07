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

import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.Switch
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class PlayerInteract: Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerInteractEvent) {
        val player = e.player
        val block = e.clickedBlock ?: return
        val region = RegionLogic.getRegion(block.location) ?: return

        if (
            block.blockData is Door || // Doors
            block.blockData is TrapDoor || // Trapdoors
            block.blockData is Switch || // Buttons, Levers
            block.blockData is Gate || // Fence gates
            block.blockData is Powerable || // Redstone items (a.e. Pressure Plates)
            block.blockData is Lightable || // Lightable items (a.e. candles)
            block.blockData is AnaloguePowerable // Analogue powerable items (a.e. target block)
        ) {
            val flagTrigger = BlockFlagTriggerEvent(player, block, region, Flag.INTERACT)
            flagTrigger.callEvent()
            if (flagTrigger.isCancelled) {
                e.isCancelled = true
            }
        }
    }
}