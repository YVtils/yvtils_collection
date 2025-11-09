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
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic

class ServerChatsSyncManager {
    companion object {
        var active = ConfigFile.getValueAsBoolean("syncFeature.chatSync.enabled") ?: false
        val channelID = ConfigFile.getValueAsString("syncFeature.chatSync.channel") ?: "CHANNEL_ID"
        val permission = ConfigFile.getValueAsString("syncFeature.chatSync.permission") ?: Permission.MESSAGE_SEND.toString()
        val syncMinecraftMessages = ConfigFile.getValueAsBoolean("syncFeature.chatSync.settings.syncMinecraftMessages") ?: true
        val syncDiscordMessages = ConfigFile.getValueAsBoolean("syncFeature.chatSync.settings.syncDiscordMessages") ?: true
        val syncAdvancements = ConfigFile.getValueAsBoolean("syncFeature.chatSync.settings.syncAdvancements") ?: true
        val syncJoinLeaveMessages = ConfigFile.getValueAsBoolean("syncFeature.chatSync.settings.syncJoinLeaveMessages") ?: true
        val syncDeaths = ConfigFile.getValueAsBoolean("syncFeature.chatSync.settings.syncDeaths") ?: true
        val ignoreBotMessages = ConfigFile.getValueAsBoolean("general.settings.ignoreBotMessages") ?: true

        lateinit var channel: TextChannel
    }

    fun loadChannelFromID() {
        if (channelID.isEmpty()) {
            Logger.warn("Channel ID for chat sync is not set. Please check your configuration.")
            active = false
            return
        }

        try {
            channel = AppLogic.getJDA().getTextChannelById(channelID)
                ?: throw IllegalArgumentException("Channel with ID $channelID not found.")
        } catch (e: Exception) {
            Logger.error("Failed to load channel for chat sync: ${e.message}")
            active = false
        }
    }
}
