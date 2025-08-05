package yv.tils.discord.logic.sync.serverChats

import yv.tils.utils.logger.Logger
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import yv.tils.discord.data.Permissions
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.active
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.channel
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.syncAdvancements

class SyncAdvancements {
    fun announceOnDiscord(e: PlayerAdvancementDoneEvent) {
        val advancement = e.advancement

        if (!active) return
        if (!syncAdvancements) return // Check if advancement sync is enabled
        if (advancement.display == null) return // Skip if no display information is available
        if (! e.player.hasPermission(Permissions.SYNC_ADVANCEMENTS.permission.name)) return // Check for permission

        sendDiscordMessage(e.player, advancement)
    }

    private fun sendDiscordMessage(sender: Player, advancement: Advancement) {
        try {
            channel.sendMessageComponents(MessageComponents().componentForAdvancement(sender, advancement))
                .useComponentsV2().queue()
        } catch (_: UninitializedPropertyAccessException) {
            Logger.warn("Discord app was not able to establish chat sync bridge between minecraft and discord. Please check your channel configuration.")
            active = false
        }
    }
}
