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

package yv.tils.discord.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import yv.tils.discord.logic.sync.serverChats.SyncAdvancements

class PlayerAdvancementDone : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerAdvancementDoneEvent) {
        val advancementName = e.advancement.key.key

        if (advancementName.startsWith("recipes/")) {
            return
        }

        SyncAdvancements().announceOnDiscord(e)
    }
}