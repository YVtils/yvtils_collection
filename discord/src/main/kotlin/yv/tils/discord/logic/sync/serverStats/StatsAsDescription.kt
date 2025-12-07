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

package yv.tils.discord.logic.sync.serverStats

import yv.tils.utils.logger.Logger
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
            Logger.warn("Channel ID for server stats is not set. Please check your configuration.")
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
            Logger.error("Failed to update Discord channel topic for server stats: ${e.message}")
        }
    }

    fun serverShutdown(serverStats: CollectStats.ServerStats) {
        updateDescription(serverStats)
    }
}
