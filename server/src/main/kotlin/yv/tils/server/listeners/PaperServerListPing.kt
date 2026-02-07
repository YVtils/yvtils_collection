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

package yv.tils.server.listeners

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.server.motd.DisplayMOTD

class PaperServerListPing : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEvent(e: PaperServerListPingEvent) {
        DisplayMOTD().onServerPing(e)
    }
}