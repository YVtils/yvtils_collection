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

package yv.tils.yv_smp.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.yv_smp.logic.music.MusicHandler

/**
 * Listener to automatically clean up audio resources when players disconnect.
 */
class PlayerQuitListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        // Stop any active audio for this player
        if (MusicHandler.hasActiveAudio(player)) {
            MusicHandler.stopAudio(player, fadeoutTime = 0L)
        }
    }
}

