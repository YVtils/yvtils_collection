package yv.tils.essentials.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import logger.Logger
import message.MessageUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.essentials.commands.handler.GlobalMuteHandler

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