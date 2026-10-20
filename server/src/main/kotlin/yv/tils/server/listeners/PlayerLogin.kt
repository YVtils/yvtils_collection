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

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import yv.tils.config.language.LanguageHandler
import yv.tils.server.maintenance.MaintenanceHandler
import yv.tils.utils.modules.Core

class PlayerLogin : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEvent(e: PlayerLoginEvent) {
        if (e.result != PlayerLoginEvent.Result.ALLOWED || !MaintenanceHandler.maintenance ||
            e.player.hasPermission(MaintenanceHandler.BYPASS_PERMISSION)) return

        e.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            LanguageHandler.getMessage(
                "maintenance.player.join.unallowed",
                e.player,
                mapOf("prefix" to Core.prefix)
            )
        )
    }
}
