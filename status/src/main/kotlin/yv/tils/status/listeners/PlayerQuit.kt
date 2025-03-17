package yv.tils.status.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.status.logic.StatusJoinQuit

class PlayerQuit : Listener {
    @EventHandler
    fun onEvent(e: PlayerQuitEvent) {
        StatusJoinQuit().savePlayer(e)
    }
}