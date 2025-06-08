package yv.tils.discord.logic.sync.serverChats

import logger.Logger
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.discord.data.Permissions
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.active
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.channel
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.syncJoinLeaveMessages

class SyncPlayerConnectionChange {
    fun syncJoin(e: PlayerJoinEvent) {
        if (!active) return
        if (!syncJoinLeaveMessages) return
        if (!e.player.hasPermission(Permissions.SYNC_JOIN.permission)) return

        sendDiscordMessage(e.player, "joined")
    }

    fun syncQuit(e: PlayerQuitEvent) {
        if (!active) return
        if (!syncJoinLeaveMessages) return
        if (!e.player.hasPermission(Permissions.SYNC_QUIT.permission)) return

        sendDiscordMessage(e.player, "left")
    }

    private fun sendDiscordMessage(sender: Player, action: String) {
        try {
            channel.sendMessageEmbeds(MessageEmbeds().embedForJoinLeave(sender, action).build()).queue()
        } catch (_: UninitializedPropertyAccessException) {
            Logger.warn("Discord channel for chat sync is not initialized. Please check your configuration.") // TODO: Replace with actual warning message
            active = false
        }
    }
}