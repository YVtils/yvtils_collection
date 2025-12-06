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

package yv.tils.discord.listener

import org.bukkit.event.*
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.discord.logic.sync.serverChats.SyncPlayerConnectionChange
import yv.tils.discord.utils.emoji.EmojiUtils

class PlayerQuit : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerQuitEvent) {
        SyncPlayerConnectionChange().syncQuit(e)
        EmojiUtils().removePlayerEmoji(e.player)
    }
}
