package yv.tils.discord.listener

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.discord.logic.sync.serverChats.SyncChats

class AsyncChat : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: AsyncChatEvent) {
        SyncChats().minecraftToDiscord(e)
    }
}