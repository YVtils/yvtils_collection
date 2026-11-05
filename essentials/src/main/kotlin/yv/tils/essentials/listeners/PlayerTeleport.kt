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

package yv.tils.essentials.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import yv.tils.essentials.commands.handler.DimensionHandler

class PlayerTeleport : Listener {
    // Portal events have their own handler list, so PlayerPortal remains registered too.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEvent(e: PlayerTeleportEvent) {
        DimensionHandler().onTravelAttempt(e)
    }
}
