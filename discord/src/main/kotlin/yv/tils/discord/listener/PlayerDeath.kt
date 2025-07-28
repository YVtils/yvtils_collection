package yv.tils.discord.listener

import org.bukkit.event.*
import org.bukkit.event.entity.PlayerDeathEvent
import yv.tils.discord.logic.sync.serverChats.SyncDeaths

class PlayerDeath: Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerDeathEvent) {
        SyncDeaths().announceOnDiscord(e)
    }
}
