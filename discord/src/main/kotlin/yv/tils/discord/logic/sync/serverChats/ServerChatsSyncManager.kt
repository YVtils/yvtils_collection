package yv.tils.discord.logic.sync.serverChats

import logger.Logger
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
        val ignoreBotMessages = ConfigFile.getValueAsBoolean("syncFeature.chatSync.settings.ignoreBotMessages") ?: true

        lateinit var channel: TextChannel
    }

    fun loadChannelFromID() {
        if (channelID.isEmpty()) {
            Logger.warn("Channel ID for chat sync is not set. Please check your configuration.") // TODO: Replace with actual warning message
            active = false
            return
        }

        try {
            channel = AppLogic.jda.getTextChannelById(channelID)
                ?: throw IllegalArgumentException("Channel with ID $channelID not found.")
        } catch (e: Exception) {
            Logger.error("Failed to load channel for chat sync: ${e.message}") // TODO: Replace with actual error message
            Logger.warn("Please check if the channel ID is correct and the bot has permission to access the channel.") // TODO: Replace with actual warning message
            active = false
        }
    }
}