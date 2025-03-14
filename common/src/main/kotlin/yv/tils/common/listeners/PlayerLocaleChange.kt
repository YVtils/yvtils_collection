package yv.tils.common.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLocaleChangeEvent
import yv.tils.common.language.LoadPlayerLanguage

class PlayerLocaleChange : Listener {
    @EventHandler
    fun onEvent(e: PlayerLocaleChangeEvent) {
        LoadPlayerLanguage().localChange(e)
    }
}