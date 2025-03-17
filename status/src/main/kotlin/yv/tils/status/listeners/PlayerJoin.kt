package yv.tils.status.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.status.logic.StatusJoinQuit

class PlayerJoin : Listener {
    @EventHandler
    fun onEvent(e: PlayerJoinEvent) {
        StatusJoinQuit().loadPlayer(e)
    }
}