package yv.tils.essentials.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import yv.tils.essentials.commands.handler.FlyHandler

class PlayerGameModeChange : Listener {
    @EventHandler
    fun onEvent(e: PlayerGameModeChangeEvent) {
        FlyHandler().onGamemodeSwitch(e)
    }
}