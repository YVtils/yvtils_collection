package yv.tils.sit.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.sit.logic.DismountListener

class PlayerQuit : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: PlayerQuitEvent) {
        DismountListener().onQuit(e)
    }
}