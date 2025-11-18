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

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.discord.data.Permissions
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.active
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.channel
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.syncJoinLeaveMessages
import yv.tils.utils.logger.Logger

class SyncPlayerConnectionChange {
    fun syncJoin(e: PlayerJoinEvent) {
        if (!active) return
        if (!syncJoinLeaveMessages) return
        if (! e.player.hasPermission(Permissions.SYNC_JOIN.permission.name)) return

        sendDiscordMessage(e.player, "join")
    }

    fun syncQuit(e: PlayerQuitEvent) {
        if (!active) return
        if (!syncJoinLeaveMessages) return
        if (! e.player.hasPermission(Permissions.SYNC_QUIT.permission.name)) return

        sendDiscordMessage(e.player, "leave")
    }

    private fun sendDiscordMessage(sender: Player, action: String) {
        try {
            channel.sendMessageComponents(MessageComponents().componentForJoinLeave(sender, action)).useComponentsV2()
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
