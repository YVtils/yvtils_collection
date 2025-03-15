package yv.tils.message.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.message.logic.MessageHandler

class PlayerQuit : Listener {
    @EventHandler
    fun onEvent(e: PlayerQuitEvent) {
        val uuid = e.player.uniqueId
        MessageHandler().removeSession(uuid)
    }
}