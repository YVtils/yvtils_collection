package yv.tils.discord.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.discord.logic.sync.serverChats.SyncPlayerConnectionChange

class PlayerQuit : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerQuitEvent) {
        SyncPlayerConnectionChange().syncQuit(e)
    }
}