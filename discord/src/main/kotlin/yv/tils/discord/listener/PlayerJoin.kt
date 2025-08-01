package yv.tils.discord.listener

import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.discord.data.Components
import yv.tils.discord.logic.sync.serverChats.SyncPlayerConnectionChange
import yv.tils.discord.utils.DiscordEmoji

class PlayerJoin : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEvent(e: PlayerJoinEvent) {
        createSkinEmoji(e.player)
        SyncPlayerConnectionChange().syncJoin(e)
    }

    private fun createSkinEmoji(player: Player) {
        val uuid = player.uniqueId
        val skinUrl = Components.ICON_URL.replace("<uuid>", uuid.toString())

        DiscordEmoji().createSkinEmoji(uuid, skinUrl, player.name)
    }
}
