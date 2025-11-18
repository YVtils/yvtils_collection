/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.logic.sync.serverChats

import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
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

        val cause = MessageUtils.strip(e.deathMessage())

        sendDiscordMessage(e.player, cause)
    }

    private fun sendDiscordMessage(sender: Player, cause: String) {
        try {
            channel.sendMessageComponents(MessageComponents().componentForDeath(sender, cause)).useComponentsV2()
                .queue()
        } catch (_: UninitializedPropertyAccessException) {
            Logger.warn("Discord app was not able to establish chat sync bridge between minecraft and discord. Please check your channel configuration.")
            active = false
        } catch (e: Exception) {
            Logger.warn("An error occurred while sending a synced chat message to Discord")
            Logger.debug("Error details: ${e.message}")
        }
    }
}
