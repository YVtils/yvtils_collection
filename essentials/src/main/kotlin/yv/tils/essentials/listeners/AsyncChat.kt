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

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.*
import yv.tils.essentials.commands.handler.GlobalMuteHandler
import yv.tils.essentials.config.ConfigFile
import yv.tils.utils.message.MessageUtils

class AsyncChat : Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEvent(e: AsyncChatEvent) {
        colorizeChatMessage(e)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onEventHIGHEST(e: AsyncChatEvent) {
        GlobalMuteHandler().playerChatEvent(e)
    }

    private fun colorizeChatMessage(e: AsyncChatEvent) {
        if (!ConfigFile.state.allowChatColors) return

        val message = MessageUtils.convertChatMessage(e.originalMessage())
        // Keep Paper's audience and cancellation pipeline intact (especially moderation mutes).
        e.renderer { _, displayName, _, _ ->
            displayName.append(MessageUtils.convert("<white>: ")).append(message)
        }
    }
}
