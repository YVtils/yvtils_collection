package yv.tils.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.server.connect.EventMessages

class PlayerJoin : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEvent(e: PlayerJoinEvent) {
        EventMessages().onPlayerJoin(e)
    }
}