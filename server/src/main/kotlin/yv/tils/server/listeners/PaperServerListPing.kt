package yv.tils.server.listeners

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.server.motd.DisplayMOTD

class PaperServerListPing : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEvent(e: PaperServerListPingEvent) {
        DisplayMOTD().onServerPing(e)
    }
}