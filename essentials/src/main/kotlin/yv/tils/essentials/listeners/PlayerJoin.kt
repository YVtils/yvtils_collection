package yv.tils.essentials.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.essentials.commands.handler.FlyHandler

class PlayerJoin : Listener {
    @EventHandler
    fun onEvent(e: PlayerJoinEvent) {
        FlyHandler().onRejoin(e)
    }
}