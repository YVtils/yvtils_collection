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
import yv.tils.utils.logger.Logger
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
        // TODO: Reimplement, when config module is ready
        // if (!(Config.config["allowChatColors"] as Boolean)) return

        val message = MessageUtils.convertChatMessage(e.originalMessage())
        val sender = e.player

        e.isCancelled = true

        Logger.info(
            sender.displayName().append(MessageUtils.convert("<white>: ").append(message))
        )

        e.player.server.onlinePlayers.forEach { player ->
            player.sendMessage(sender.displayName().append(MessageUtils.convert("<white>: ").append(message)))
        }
    }
}
