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
        if (! e.player.hasPermission(Permissions.SYNC_JOIN.permission.name)) return

        sendDiscordMessage(e.player, "joined")
    }

    fun syncQuit(e: PlayerQuitEvent) {
        if (!active) return
        if (!syncJoinLeaveMessages) return
        if (! e.player.hasPermission(Permissions.SYNC_QUIT.permission.name)) return

        sendDiscordMessage(e.player, "left")
    }

    private fun sendDiscordMessage(sender: Player, action: String) {
        try {
            channel.sendMessageComponents(MessageComponents().componentForJoinLeave(sender, action)).useComponentsV2()
                .queue()
        } catch (_: UninitializedPropertyAccessException) {
            Logger.warn("Discord app was not able to establish chat sync bridge between minecraft and discord. Please check your channel configuration.")
            active = false
        }
    }
}
