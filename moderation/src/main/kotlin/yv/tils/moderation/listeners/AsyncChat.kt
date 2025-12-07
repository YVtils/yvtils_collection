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

package yv.tils.moderation.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.Logger

class AsyncChat : Listener {
    @EventHandler
    fun onEvent(e: AsyncChatEvent) {
        val player = e.player

        if (!TargetUtils.isTargetMuted(player)) return

        e.isCancelled = true

        val muteData = TargetUtils.getMuteData(player) ?: run {
            // TODO: Add some type of error message
            Logger.dev("Failed to get mute")
            return
        }

        TargetUtils().sendMutedMessage(player, muteData)
    }
}