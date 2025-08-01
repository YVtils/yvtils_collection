package yv.tils.discord.listener

import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.discord.logic.sync.serverChats.SyncPlayerConnectionChange
import yv.tils.discord.utils.DiscordEmoji

class PlayerQuit : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerQuitEvent) {
        SyncPlayerConnectionChange().syncQuit(e)
        removeSkinEmoji(e.player)
    }

    private fun removeSkinEmoji(player: Player) {
        val uuid = player.uniqueId

        DiscordEmoji().removeSkinEmoji(uuid)
    }
}
