package yv.tils.discord.logic.sync.serverStats

import logger.Logger
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic.Companion.jda

class StatsAsDescription {
    companion object {
        val channelID = ConfigFile.getValueAsString("syncFeature.serverStats.channel")
    }

    fun syncStats(serverStats: CollectStats.ServerStats) {
        updateDescription(serverStats)
    }

    private fun updateDescription(serverStats: CollectStats.ServerStats) {
        if (channelID.isNullOrEmpty()) {
            Logger.warn("Channel ID for server stats is not set. Please check your configuration.") // TODO: Replace with actual warning message
            return
        }

        try {
            jda.getTextChannelById(channelID)?.manager?.setTopic(
                "${serverStats.status}\n" +
                        "${serverStats.version}\n" +
                        "${serverStats.players}\n" +
                        serverStats.lastRefreshed
            )?.queue()
        } catch (e: Exception) {
            Logger.error("Failed to update Discord channel topic: ${e.message}") // TODO: Replace with actual error message
            Logger.warn("Please check if the channel ID is correct and the bot has permission to manage the channel.") // TODO: Replace with actual warning message
        }
    }

    fun serverShutdown(serverStats: CollectStats.ServerStats) {
        updateDescription(serverStats)
    }
}