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

import org.bukkit.entity.Player
import yv.tils.yv_smp.configs.ConfigFile
import java.io.File

/**
 * High-level API for playing custom audio to players via Simple Voice Chat.
 *
 * This provides a simple interface to the music system without needing to
 * understand the underlying implementation details.
 *
 * ## Requirements
 * - Simple Voice Chat plugin must be installed on the server
 * - Players must have Simple Voice Chat mod installed
 *
 * ## Examples
 *
 * ### Play audio from URL to a single player
 * ```kotlin
 * MusicAPI.playUrl(player, "https://example.com/audio.mp3")
 * ```
 *
 * ### Play audio from local file with custom settings
 * ```kotlin
 * val file = File("/path/to/audio.ogg")
 * MusicAPI.playFile(player, file, volume = 0.8f, loop = true)
 * ```
 *
 * ### Broadcast audio to all online players
 * ```kotlin
 * MusicAPI.broadcastUrl("https://example.com/announcement.mp3")
 * ```
 *
 * ### Stop audio for a player with fade-out
 * ```kotlin
 * MusicAPI.stop(player, fadeoutMs = 2000)
 * ```
 */
object MusicAPI {

    /**
     * Apply the master volume from config to the requested volume.
     * @param volume The requested volume (0.0 to 1.0)
     * @return The volume with master volume applied
     */
    private fun applyMasterVolume(volume: Float): Float {
        val masterVolume = ConfigFile.getMasterVolume()
        return (volume * masterVolume).coerceIn(0.0f, 1.0f)
    }

    /**
     * Play audio from a URL for a specific player.
     *
     * @param player The player to play audio for
     * @param url The URL of the audio file (supports various formats)
     * @param volume Volume level (0.0 to 1.0, default: 1.0)
     * @param pitch Pitch adjustment (0.5 to 2.0, default: 1.0)
     * @param loop Whether to loop the audio (default: false)
     * @return true if playback started successfully, false otherwise
     */
    fun playUrl(
        player: Player,
        url: String,
        volume: Float = 1.0f,
        pitch: Float = 1.0f,
        loop: Boolean = false
    ): Boolean {
        if (!MusicHandler.isVoiceChatAvailable) {
            return false
        }

        return try {
            val config = AudioConfig(
                source = AudioSource.Url(url),
                volume = applyMasterVolume(volume),
                pitch = pitch,
                loop = loop
            )
            MusicHandler.playAudio(player, config)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Play audio from a local file for a specific player.
     *
     * @param player The player to play audio for
     * @param file The audio file to play (must exist)
     * @param volume Volume level (0.0 to 1.0, default: 1.0)
     * @param pitch Pitch adjustment (0.5 to 2.0, default: 1.0)
     * @param loop Whether to loop the audio (default: false)
     * @return true if playback started successfully, false otherwise
     */
    fun playFile(
        player: Player,
        file: File,
        volume: Float = 1.0f,
        pitch: Float = 1.0f,
        loop: Boolean = false
    ): Boolean {
        if (!MusicHandler.isVoiceChatAvailable) {
            return false
        }

        return try {
            val config = AudioConfig(
                source = AudioSource.File(file),
                volume = applyMasterVolume(volume),
                pitch = pitch,
                loop = loop
            )
            MusicHandler.playAudio(player, config)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Broadcast audio from a URL to all online players.
     *
     * @param url The URL of the audio file
     * @param volume Volume level (0.0 to 1.0, default: 1.0)
     * @param pitch Pitch adjustment (0.5 to 2.0, default: 1.0)
     * @param loop Whether to loop the audio (default: false)
     * @return true if broadcast started successfully, false otherwise
     */
    fun broadcastUrl(
        url: String,
        volume: Float = 1.0f,
        pitch: Float = 1.0f,
        loop: Boolean = false
    ): Boolean {
        if (!MusicHandler.isVoiceChatAvailable) {
            return false
        }

        return try {
            val config = AudioConfig(
                source = AudioSource.Url(url),
                volume = applyMasterVolume(volume),
                pitch = pitch,
                loop = loop
            )
            MusicHandler.playAudioBroadcast(config)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Broadcast audio from a local file to all online players.
     *
     * @param file The audio file to play (must exist)
     * @param volume Volume level (0.0 to 1.0, default: 1.0)
     * @param pitch Pitch adjustment (0.5 to 2.0, default: 1.0)
     * @param loop Whether to loop the audio (default: false)
     * @return true if broadcast started successfully, false otherwise
     */
    fun broadcastFile(
        file: File,
        volume: Float = 1.0f,
        pitch: Float = 1.0f,
        loop: Boolean = false
    ): Boolean {
        if (!MusicHandler.isVoiceChatAvailable) {
            return false
        }

        return try {
            val config = AudioConfig(
                source = AudioSource.File(file),
                volume = applyMasterVolume(volume),
                pitch = pitch,
                loop = loop
            )
            MusicHandler.playAudioBroadcast(config)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Stop audio playback for a specific player.
     *
     * @param player The player to stop audio for
     * @param fadeoutMs Duration of fade-out in milliseconds (0 for instant stop, default: 0)
     */
    fun stop(player: Player, fadeoutMs: Long = 0L) {
        MusicHandler.stopAudio(player, fadeoutMs)
    }

    /**
     * Check if a player currently has audio playing.
     *
     * @param player The player to check
     * @return true if the player has active audio, false otherwise
     */
    fun isPlaying(player: Player): Boolean {
        return MusicHandler.hasActiveAudio(player)
    }

    /**
     * Check if the music system is available (Simple Voice Chat is loaded).
     *
     * @return true if the music system is available, false otherwise
     */
    fun isAvailable(): Boolean {
        return MusicHandler.isVoiceChatAvailable
    }
}
