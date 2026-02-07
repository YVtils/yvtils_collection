/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.logic.sync.serverStats

import yv.tils.discord.configs.ConfigFile
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import yv.tils.utils.player.PlayerUtils
import yv.tils.utils.server.ServerUtils
import yv.tils.utils.server.VersionUtils
import yv.tils.utils.time.TimeUtils

class CollectStats {
    companion object {
        private var active = ConfigFile.getValueAsBoolean("syncFeature.serverStats.enabled") ?: false
        private val statsMode = ConfigFile.getValueAsString("syncFeature.serverStats.mode") ?: "both"
        private val channelID = ConfigFile.getValueAsString("syncFeature.serverStats.channel") ?: "CHANNEL_ID"
        
        private val layoutStatus = ConfigFile.getValueAsString("syncFeature.serverStats.design.status.text") ?: "<emoji> | SERVER <status>"
        private val layoutVersion = ConfigFile.getValueAsString("syncFeature.serverStats.design.version.text") ?: "<emoji> | <version>"
        private val layoutPlayers = ConfigFile.getValueAsString("syncFeature.serverStats.design.players.text") ?: "<emoji> | <players> / <maxPlayers> Players"
        private val layoutLastRefreshed = ConfigFile.getValueAsString("syncFeature.serverStats.design.lastRefreshed.text") ?: "<emoji> | <time>"
        
        private val statusEmojiOnline = ConfigFile.getValueAsString("syncFeature.serverStats.design.status.emoji.online") ?: "💚"
        private val statusEmojiOffline = ConfigFile.getValueAsString("syncFeature.serverStats.design.status.emoji.offline") ?: "❤️"
        private val statusEmojiMaintenance = ConfigFile.getValueAsString("syncFeature.serverStats.design.status.emoji.maintenance") ?: "💛"
        private val versionEmoji = ConfigFile.getValueAsString("syncFeature.serverStats.design.version.emoji") ?: "🛠️"
        private val playersEmoji = ConfigFile.getValueAsString("syncFeature.serverStats.design.players.emoji") ?: "👥"
        private val lastRefreshedEmoji = ConfigFile.getValueAsString("syncFeature.serverStats.design.lastRefresh.emoji") ?: "⌚"

        private var serverStatusText = ""
        private var serverVersionText = ""
        private var lastPlayerCountText = ""
        private var lastRefreshedText = ""

        private var serverStatus = "x"
        private var serverVersion = "x.x.x"
        private var lastPlayerCount = "x"
        private var lastRefreshed = "xx/xx/xxxx xx:xx:xx"

        private lateinit var taskID: String
    }
    
    fun collect() {
        if (!active) {
            return
        }

        taskID = CoroutineHandler.launchTask(
            task = {
                if (!active) {
                    CoroutineHandler.cancelTask(taskID)
                    return@launchTask
                }

                val serverStats = prepareLayout()

                when (statsMode) {
                    "channels" -> {
                        StatsAsChannels().syncStats(serverStats)
                    }

                    "description" -> {
                        StatsAsDescription().syncStats(serverStats)
                    }

                    "both" -> {
                        StatsAsChannels().syncStats(serverStats)
                        StatsAsDescription().syncStats(serverStats)
                    }

                    else -> {
                        active = false
                        Logger.error("The provided stats mode '$statsMode' is not supported. Please check your configuration.")
                        CoroutineHandler.cancelTask(taskID)
                    }
                }
            },
            taskName = "yvtils-discord-serverStats",
            afterDelay = 600L * 1000L // 10 minutes in milliseconds
        )
    }

    fun serverShutdown() {
        if (!active) {
            return
        }

        val serverStats = prepareLayout("OFFLINE", "N/A", "0")

        when (statsMode) {
            "channels" -> {
                StatsAsChannels().serverShutdown(serverStats)
            }

            "description" -> {
                StatsAsDescription().serverShutdown(serverStats)
            }

            "both" -> {
                StatsAsChannels().serverShutdown(serverStats)
                StatsAsDescription().serverShutdown(serverStats)
            }
        }
    }

    fun prepareLayout(
        newServerStatus: String = "",
        newServerVersion: String = "",
        newLastPlayerCount: String = ""
    ): ServerStats {
        if (newServerStatus.isEmpty()) {
            serverStatus = "ONLINE"

            if (ServerUtils.serverInMaintenance) {
                serverStatus = "MAINTENANCE"
            }
        } else {
            serverStatus = newServerStatus.uppercase()
        }

        if (newServerVersion.isEmpty()) {
            serverVersion = VersionUtils.serverVersion
            val viaVersion = VersionUtils.isViaVersion

            if (viaVersion) {
                serverVersion = "$serverVersion +"
            }
        } else {
            serverVersion = newServerVersion
        }

        lastPlayerCount = newLastPlayerCount.ifEmpty {
            PlayerUtils.onlinePlayersAsCount.toString()
        }

        lastRefreshed = TimeUtils().parseTimezone()

        serverStatusText = layoutStatus
            .replace("<emoji>", if (serverStatus == "ONLINE") statusEmojiOnline else if (serverStatus == "MAINTENANCE") statusEmojiMaintenance else statusEmojiOffline)
            .replace("<status>", serverStatus)

        serverVersionText = layoutVersion
            .replace("<emoji>", versionEmoji)
            .replace("<version>", serverVersion)

        lastPlayerCountText = layoutPlayers
            .replace("<emoji>", playersEmoji)
            .replace("<players>", lastPlayerCount)
            .replace("<maxPlayers>", PlayerUtils.maxOnlinePlayers.toString())

        lastRefreshedText = layoutLastRefreshed
            .replace("<emoji>", lastRefreshedEmoji)
            .replace("<time>", lastRefreshed)

        Logger.debug("Server Stats Updated: Status: $serverStatusText, Version: $serverVersionText, Players: $lastPlayerCountText, Last Refreshed: $lastRefreshedText")

        return ServerStats(
            status = serverStatusText,
            version = serverVersionText,
            players = lastPlayerCountText,
            lastRefreshed = lastRefreshedText
        )
    }

    data class ServerStats(
        val status: String,
        val version: String,
        val players: String,
        val lastRefreshed: String
    )
}
