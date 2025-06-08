package yv.tils.discord.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import yv.tils.discord.logic.sync.serverChats.SyncAdvancements

class PlayerAdvancementDone : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerAdvancementDoneEvent) {
        val advancementName = e.advancement.key.key

        if (advancementName.startsWith("recipes/")) {
            return
        }

        SyncAdvancements().announceOnDiscord(e)
    }
}