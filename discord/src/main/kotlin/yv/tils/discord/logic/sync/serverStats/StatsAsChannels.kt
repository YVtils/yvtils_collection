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

import yv.tils.utils.logger.Logger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.configs.StatsSyncSaveFile
import yv.tils.discord.logic.AppLogic.Companion.jda
import java.util.*
import java.util.concurrent.CompletableFuture

class StatsAsChannels {
    companion object {
        val channelIDs = StatsSyncSaveFile.saves

        private val serverStatusActive = ConfigFile.getValueAsBoolean("syncFeature.serverStats.settings.showServerStatus") ?: true
        private val serverVersionActive = ConfigFile.getValueAsBoolean("syncFeature.serverStats.settings.showServerVersion") ?: true
        private val onlinePlayersActive = ConfigFile.getValueAsBoolean("syncFeature.serverStats.settings.showServerPlayers") ?: true
        private val lastRefreshTimeActive = ConfigFile.getValueAsBoolean("syncFeature.serverStats.settings.showLastRefreshTime") ?: true
    }

    private fun createChannels() {
        val futures = mutableListOf<CompletableFuture<Pair<String, StatsSyncSaveFile.StatsSyncSave>>>()
        
        for (guild in jda.guilds) {
            val guildId = guild.id
            val future = CompletableFuture<Pair<String, StatsSyncSaveFile.StatsSyncSave>>()
            
            val statsSyncSave = StatsSyncSaveFile.StatsSyncSave(
                guild.id,
                "", // Will be filled later
                "", // Will be filled later
                "", // Will be filled later
                ""  // Will be filled later
            )
            
            // Chain all the channel creations for this guild
            var statusFuture: CompletableFuture<VoiceChannel>? = null
            
            if (serverStatusActive) {
                statusFuture = createVoiceChannel(guild, "<serverStatus>")
                    .thenApply { channel ->
                        statsSyncSave.status = channel.id
                        channel
                    }
            }
            
            var versionFuture: CompletableFuture<VoiceChannel>? = null
            if (serverVersionActive) {
                val previousFuture = statusFuture ?: CompletableFuture.completedFuture(null)
                versionFuture = previousFuture.thenCompose {
                    createVoiceChannel(guild, "<serverVersion>")
                }.thenApply { channel ->
                    statsSyncSave.version = channel.id
                    channel
                }
            }
            
            var playersFuture: CompletableFuture<VoiceChannel>? = null
            if (onlinePlayersActive) {
                val previousFuture = versionFuture ?: statusFuture ?: CompletableFuture.completedFuture(null)
                playersFuture = previousFuture.thenCompose {
                    createVoiceChannel(guild, "<onlinePlayers>")
                }.thenApply { channel ->
                    statsSyncSave.playerCount = channel.id
                    channel
                }
            }
            
            var refreshFuture: CompletableFuture<VoiceChannel>? = null
            if (lastRefreshTimeActive) {
                val previousFuture = playersFuture ?: versionFuture ?: statusFuture ?: CompletableFuture.completedFuture(null)
                refreshFuture = previousFuture.thenCompose {
                    createVoiceChannel(guild, "<lastRefreshTime>")
                }.thenApply { channel ->
                    statsSyncSave.lastRefreshed = channel.id
                    channel
                }
            }
            
            // Complete the guild future when all channels are created
            val finalFuture = refreshFuture ?: playersFuture ?: versionFuture ?: statusFuture ?: CompletableFuture.completedFuture(null)
            finalFuture.thenAccept {
                future.complete(Pair(guildId, statsSyncSave))
            }.exceptionally { throwable ->
                Logger.error("Failed to create channels for guild ${guild.name} (${guild.id}): ${throwable.message}")
                future.completeExceptionally(throwable)
                null
            }
            
            futures.add(future)
        }
        
        // Wait for all guild futures to complete
        val results = futures.mapNotNull { 
            try {
                it.get()
            } catch (e: Exception) {
                Logger.error("Error waiting for channel creation: ${e.message}")
                null
            }
        }
        
        // Update the save file with all the channel IDs
        val channelIDList = results.toMap().toMutableMap()
        StatsSyncSaveFile().registerStrings(channelIDList.values.toMutableList())
        channelIDs.putAll(channelIDList)
    }
    
    private fun createVoiceChannel(guild: net.dv8tion.jda.api.entities.Guild, name: String): CompletableFuture<VoiceChannel> {
        val future = CompletableFuture<VoiceChannel>()
        
        guild.createVoiceChannel(name).queue({ channel ->
            channel.manager.putPermissionOverride(
                guild.publicRole,
                EnumSet.of(Permission.VIEW_CHANNEL),
                EnumSet.of(Permission.VOICE_CONNECT)
            ).queue(
                { future.complete(channel) },
                { error -> future.completeExceptionally(error) }
            )
        }, { error ->
            future.completeExceptionally(error)
        })
        
        return future
    }

    private fun updateChannels(serverStats: CollectStats.ServerStats) {
        if (channelIDs.isEmpty()) {
            createChannels()
        }

        for ((guildID, channelSave) in channelIDs) {
            val guild = jda.getGuildById(guildID) ?: continue
            val channelList = mutableListOf<String>()
            if (serverStatusActive) {
                val channel = guild.getVoiceChannelById(channelSave.status) ?: continue
                channel.manager.setName(serverStats.status).queue()
                channelList.add(channel.id)
            }

            if (serverVersionActive) {
                val channel = guild.getVoiceChannelById(channelSave.version) ?: continue
                channel.manager.setName(serverStats.version).queue()
                channelList.add(channel.id)
            }

            if (onlinePlayersActive) {
                val channel = guild.getVoiceChannelById(channelSave.playerCount) ?: continue
                channel.manager.setName(serverStats.players).queue()
                channelList.add(channel.id)
            }

            if (lastRefreshTimeActive) {
                val channel = guild.getVoiceChannelById(channelSave.lastRefreshed) ?: continue
                channel.manager.setName(serverStats.lastRefreshed).queue()
                channelList.add(channel.id)
            }
        }
    }

    fun syncStats(serverStats: CollectStats.ServerStats) {
        updateChannels(serverStats)
    }

    fun deleteChannels() {
        if (channelIDs.isEmpty()) {
            return
        }

        for ((guildID, channelSave) in channelIDs) {
            val guild = jda.getGuildById(guildID) ?: continue

            listOf(
                channelSave.status,
                channelSave.version,
                channelSave.playerCount,
                channelSave.lastRefreshed
            ).forEach { channelId ->
                val channel = guild.getVoiceChannelById(channelId)
                channel?.delete()?.queue()
            }
            channelIDs.remove(guildID)

            StatsSyncSaveFile().registerStrings(channelIDs.values.toMutableList())

            Logger.debug("Deleted channels for guild: $guildID")
            Logger.debug("Updated channel IDs: ${channelIDs.keys.joinToString(", ")}")
        }
    }

    fun serverShutdown(serverStats: CollectStats.ServerStats) {
        updateChannels(serverStats)
    }
}
