package yv.tils.discord.listener

import org.bukkit.event.*
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.discord.logic.sync.serverChats.SyncPlayerConnectionChange
import yv.tils.discord.utils.emoji.EmojiUtils

class PlayerJoin : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerJoinEvent) {
        EmojiUtils().createPlayerEmoji(e.player)
        SyncPlayerConnectionChange().syncJoin(e)
    }
}
