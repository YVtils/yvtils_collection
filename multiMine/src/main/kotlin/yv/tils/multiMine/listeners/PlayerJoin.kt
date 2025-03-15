package yv.tils.multiMine.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.multiMine.configs.MultiMineConfig

class PlayerJoin : Listener {
    @EventHandler
    fun onEvent(e: PlayerJoinEvent) {
        val uuid = e.player.uniqueId
        MultiMineConfig().addPlayer(uuid)
    }
}