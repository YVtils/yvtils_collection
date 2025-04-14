package yv.tils.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.server.connect.EventMessages

class PlayerQuit : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEvent(e: PlayerQuitEvent) {
        EventMessages().onPlayerQuit(e)
    }
}