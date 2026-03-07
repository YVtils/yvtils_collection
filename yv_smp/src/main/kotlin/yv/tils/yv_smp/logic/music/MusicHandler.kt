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

package yv.tils.yv_smp.logic.music

import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatServerApi
import org.bukkit.entity.Player
import yv.tils.utils.logger.Logger
import yv.tils.utils.player.PlayerUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Central music handler for managing audio playback across players.
 * This is a singleton that manages all active audio players.
 */
object MusicHandler {
    private val activePlayers = ConcurrentHashMap<UUID, VoiceChatAudioPlayer>()
    private var voicechatApi: VoicechatApi? = null

    /**
     * Initialize the music handler with the VoiceChat API.
     * Should be called once during plugin initialization.
     */
    fun initialize(api: VoicechatApi?) {
        voicechatApi = api
        if (api != null) {
            Logger.info("MusicHandler initialized with VoiceChat API")
        } else {
            Logger.warn("MusicHandler initialized without VoiceChat API - audio features disabled")
        }
    }

    /**
     * Whether Simple Voice Chat is available and initialized.
     */
    val isVoiceChatAvailable: Boolean
        get() = voicechatApi != null

    /**
     * Check if a player has active audio playing.
     */
    fun hasActiveAudio(player: Player): Boolean {
        return activePlayers.containsKey(player.uniqueId)
    }

    /**
     * Get the active audio player for a specific player, if any.
     */
    fun getAudioPlayer(player: Player): VoiceChatAudioPlayer? {
        return activePlayers[player.uniqueId]
    }

    /**
     * Play audio for a specific player.
     */
    fun playAudio(player: Player, config: AudioConfig) {
        val serverApi = voicechatApi as? VoicechatServerApi
        if (serverApi == null) {
            Logger.warn("Cannot play audio for ${player.name}: VoiceChat API not available")
            return
        }

        // Check if player has Simple Voice Chat installed
        if (!SVCManager.hasPlayerSVC(player.uniqueId, serverApi)) {
            Logger.warn("Cannot play audio for ${player.name}: Player does not have Simple Voice Chat installed")
            return
        }

        try {
            Logger.info("Playing audio for ${player.name}: ${config.source}")
            playViaVoiceChat(player, serverApi, config)
        } catch (e: Exception) {
            Logger.error("Failed to play audio for ${player.name}: ${e.message}")
        }
    }

    /**
     * Broadcast audio to all online players who have Simple Voice Chat.
     */
    fun playAudioBroadcast(config: AudioConfig) {
        val onlinePlayers = PlayerUtils.onlinePlayersAsPlayers
        var successCount = 0

        Logger.info("Broadcasting audio to ${onlinePlayers.size} players: ${config.source}")

        onlinePlayers.forEach { player ->
            try {
                playAudio(player, config)
                successCount++
            } catch (e: Exception) {
                Logger.warn("Failed to play audio for ${player.name}: ${e.message}")
            }
        }

        Logger.info("Successfully started audio broadcast for $successCount/${onlinePlayers.size} players")
    }

    /**
     * Stop audio playback for a specific player.
     *
     * @param player The player to stop audio for
     * @param fadeoutTime Duration of fade-out in milliseconds (0 for instant stop)
     */
    fun stopAudio(player: Player, fadeoutTime: Long = 0L) {
        val audioPlayer = activePlayers[player.uniqueId] ?: return

        if (fadeoutTime > 0) {
            audioPlayer.fadeOutAndStop(fadeoutTime)
        } else {
            audioPlayer.stop()
        }

        activePlayers.remove(player.uniqueId)
        Logger.info("Stopped audio playback for ${player.name}")
    }

    /**
     * Stop audio for all active players.
     * Should be called during plugin shutdown.
     */
    fun stopAll(fadeoutTime: Long = 0L) {
        Logger.info("Stopping all active audio players (${activePlayers.size} active)")

        activePlayers.values.forEach { audioPlayer ->
            try {
                if (fadeoutTime > 0) {
                    audioPlayer.fadeOutAndStop(fadeoutTime)
                } else {
                    audioPlayer.stop()
                }
            } catch (e: Exception) {
                Logger.warn("Error stopping audio player: ${e.message}")
            }
        }

        activePlayers.clear()
    }

    /**
     * Internal method to play audio via Simple Voice Chat API using LavaPlayer.
     */
    private fun playViaVoiceChat(player: Player, serverApi: VoicechatServerApi, config: AudioConfig) {
        // Stop any existing player for this player
        activePlayers[player.uniqueId]?.stop()

        // Create new audio player
        val audioPlayer = VoiceChatAudioPlayer(player, serverApi, config)
        activePlayers[player.uniqueId] = audioPlayer

        // Get audio URL based on source type
        val audioUrl = when (val source = config.source) {
            is AudioSource.File -> "file:///${source.file.absolutePath.replace("\\", "/")}"
            is AudioSource.Url -> source.url.toString()
            is AudioSource.BuiltIn -> {
                Logger.warn("BuiltIn audio source cannot be played via VoiceChat")
                activePlayers.remove(player.uniqueId)
                return
            }
        }

        audioPlayer.load(audioUrl,
            onLoaded = {
                Logger.info("Audio loaded successfully for ${player.name}")
            },
            onEnd = {
                // Auto-cleanup when playback ends
                activePlayers.remove(player.uniqueId)
            }
        )
    }
}