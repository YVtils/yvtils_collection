package yv.tils.discord.listener

import org.bukkit.event.*
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.discord.logic.sync.serverChats.SyncPlayerConnectionChange
import yv.tils.discord.utils.emoji.EmojiUtils

class PlayerQuit : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerQuitEvent) {
        SyncPlayerConnectionChange().syncQuit(e)
        EmojiUtils().removePlayerEmoji(e.player)
    }
}
