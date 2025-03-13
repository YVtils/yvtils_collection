package yv.tils.essentials.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import yv.tils.essentials.commands.handler.FlyHandler

class PlayerChangedWorld : Listener {
    @EventHandler
    fun onEvent(e: PlayerChangedWorldEvent) {
        FlyHandler().onWorldChange(e)
    }
}