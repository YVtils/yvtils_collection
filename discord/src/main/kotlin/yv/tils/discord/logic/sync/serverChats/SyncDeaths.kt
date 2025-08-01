package yv.tils.discord.logic.sync.serverChats

import logger.Logger
import message.MessageUtils
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import yv.tils.discord.data.Permissions
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.active
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.channel
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.syncDeaths

class SyncDeaths {
    fun announceOnDiscord(e: PlayerDeathEvent) {
        if (! active) return
        if (! syncDeaths) return // Check if death sync is enabled
        if (! e.player.hasPermission(Permissions.SYNC_DEATHS.permission.name)) return // Check for permission

        val cause = MessageUtils.strip(e.deathMessage()).ifBlank { "Unknown cause" } // TODO: Localization

        Logger.dev("Syncing death message to Discord: ${e.player.name} died due to $cause")

        sendDiscordMessage(e.player, cause)
    }

    private fun sendDiscordMessage(sender: Player, cause: String) {
        try {
            channel.sendMessageComponents(MessageComponents().componentForDeath(sender, cause)).useComponentsV2()
                .queue()
        } catch (_: UninitializedPropertyAccessException) {
            Logger.warn("Discord app was not able to establish chat sync bridge between minecraft and discord. Please check your channel configuration.")
            active = false
        }
    }
}
