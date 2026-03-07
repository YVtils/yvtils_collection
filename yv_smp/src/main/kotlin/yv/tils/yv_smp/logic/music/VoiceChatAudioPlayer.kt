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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import de.maxhenkel.opus4j.OpusDecoder
import de.maxhenkel.opus4j.OpusEncoder
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel
import dev.lavalink.youtube.YoutubeAudioSourceManager
import org.bukkit.entity.Player
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Audio player that uses LavaPlayer for streaming audio via Simple Voice Chat.
 * Handles audio encoding/decoding and playback for a specific player.
 */
class VoiceChatAudioPlayer(
    private val player: Player,
    serverApi: VoicechatServerApi,
    private val config: AudioConfig
) {
    
    companion object {
        // Shared audio player manager for all instances
        private val AUDIO_PLAYER_MANAGER: AudioPlayerManager = DefaultAudioPlayerManager().apply {
            val youtubeSourceManager = YoutubeAudioSourceManager()
            registerSourceManager(youtubeSourceManager)
            AudioSourceManagers.registerRemoteSources(this, YoutubeAudioSourceManager::class.java)
            AudioSourceManagers.registerLocalSource(this)
        }
        
        // Shared executor for all fade operations and audio processing
        private val AUDIO_EXECUTOR: ScheduledExecutorService = Executors.newScheduledThreadPool(
            4,
            { runnable -> Thread(runnable, "YVtils-Audio-Thread").apply { isDaemon = true } }
        )

        private const val SAMPLE_RATE = 48000
        private const val CHANNELS = 1
        private const val FRAME_SIZE = 960
        private const val AUDIO_FRAME_INTERVAL_MS = 20L

        fun byteToShort(input: ByteArray): ShortArray {
            val iterations = input.size / 2
            val output = ShortArray(iterations)
            val bb = ByteBuffer.wrap(input)
            for (index in 0 until iterations) {
                output[index] = bb.short
            }
            return output
        }
        
        fun shortToByte(input: ShortArray): ByteArray {
            val bb = ByteBuffer.allocate(input.size * 2)
            for (value in input) {
                bb.putShort(value)
            }
            return bb.array()
        }

        /**
         * Shutdown the shared executor service.
         * Should be called during plugin shutdown.
         */
        fun shutdown() {
            AUDIO_EXECUTOR.shutdown()
            try {
                if (!AUDIO_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    AUDIO_EXECUTOR.shutdownNow()
                }
            } catch (_: InterruptedException) {
                AUDIO_EXECUTOR.shutdownNow()
            }
        }
    }
    
    private val lavaPlayer: AudioPlayer = AUDIO_PLAYER_MANAGER.createPlayer()
    private val audioQueue = ArrayDeque<ByteArray>()
    private var channel: StaticAudioChannel? = null
    private var decoder: OpusDecoder? = null
    private var encoder: OpusEncoder? = null
    private var audioTask: ScheduledFuture<*>? = null
    private val running = AtomicBoolean(false)
    private var volumeMultiplier = config.volume
    private val isFadingOut = AtomicBoolean(false)
    private var fadeTask: ScheduledFuture<*>? = null
    private var onEndCallback: (() -> Unit)? = null

    init {
        try {
            // Initialize Opus codec
            decoder = OpusDecoder(SAMPLE_RATE, CHANNELS).apply {
                frameSize = FRAME_SIZE
            }
            encoder = OpusEncoder(SAMPLE_RATE, CHANNELS, OpusEncoder.Application.AUDIO)
            
            // Get player's voice chat connection
            val connection = serverApi.getConnectionOf(player.uniqueId)
            if (connection != null) {
                // Create StaticAudioChannel for this specific player
                channel = serverApi.createStaticAudioChannel(UUID.randomUUID())
                channel?.addTarget(connection)
                channel?.category = SimpleVoiceChat.AUDIO_CATEGORY

                Logger.info("Created audio channel for ${player.name}")
                
                // Set up LavaPlayer event listener
                lavaPlayer.addListener(object : AudioEventAdapter() {
                    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
                        if (config.loop && endReason == AudioTrackEndReason.FINISHED) {
                            // Loop the track
                            player.playTrack(track.makeClone())
                            Logger.info("Looping track for ${this@VoiceChatAudioPlayer.player.name}")
                        } else {
                            // Wait for audio queue to empty before stopping
                            AUDIO_EXECUTOR.execute {
                                while (audioQueue.isNotEmpty() && running.get()) {
                                    try {
                                        Thread.sleep(100)
                                    } catch (_: InterruptedException) {
                                        break
                                    }
                                }
                                stop()
                                onEndCallback?.invoke()
                            }
                        }
                    }
                })
                
                // Apply volume and pitch from config
                lavaPlayer.volume = (config.volume * 100).toInt()

                // Start the audio processing task (20ms intervals for 50 FPS audio)
                audioTask = AUDIO_EXECUTOR.scheduleAtFixedRate(
                    { processAudioFrame() },
                    0,
                    AUDIO_FRAME_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
                )
            } else {
                Logger.warn("Player ${player.name} does not have voice chat installed or enabled")
            }
            
        } catch (e: Exception) {
            Logger.error("Failed to initialize VoiceChat audio player for ${player.name}: ${e.message}")
            cleanup()
        }
    }
    
    /**
     * Loads and plays audio from a URL.
     *
     * @param audioUrl The URL of the audio to play
     * @param onLoaded Callback invoked when audio is successfully loaded
     * @param onEnd Callback invoked when audio playback ends
     */
    fun load(audioUrl: String, onLoaded: (() -> Unit)? = null, onEnd: (() -> Unit)? = null) {
        this.onEndCallback = onEnd
        loadAudio(audioUrl, onLoaded)
    }

    /**
     * Internal method to load audio from URL.
     */
    private fun loadAudio(audioUrl: String, onLoaded: (() -> Unit)?) {
        AUDIO_PLAYER_MANAGER.loadItem(audioUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                Logger.info("Loaded track: ${track.info.title} for ${player.name}")
                lavaPlayer.playTrack(track)
                running.set(true)
                onLoaded?.invoke()
            }
            
            override fun playlistLoaded(playlist: AudioPlaylist) {
                val track = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                if (track != null) {
                    Logger.info("Loaded track from playlist: ${track.info.title} for ${player.name}")
                    lavaPlayer.playTrack(track)
                    running.set(true)
                    onLoaded?.invoke()
                } else {
                    Logger.warn("Playlist loaded but no tracks available for ${player.name}")
                }
            }
            
            override fun noMatches() {
                Logger.warn("No matches found for audio: $audioUrl for ${player.name}")
                cleanup()
            }
            
            override fun loadFailed(exception: FriendlyException) {
                Logger.warn("Failed to load audio for ${player.name}: ${exception.message}")
                cleanup()
            }
        })
    }
    
    /**
     * Processes a single audio frame - decode Opus from LavaPlayer, encode for Voice Chat.
     */
    private fun processAudioFrame() {
        if (!running.get() || channel == null) return

        try {
            // Get Opus frame from LavaPlayer
            val frame = lavaPlayer.provide()
            if (frame != null && decoder?.isClosed == false && encoder?.isClosed == false) {
                // Decode Opus → PCM
                val decoded = shortToByte(decoder!!.decode(frame.data))
                var pcmShorts = byteToShort(decoded)

                // Apply volume multiplier (for fade-out)
                if (volumeMultiplier < 1.0f) {
                    pcmShorts = pcmShorts.map { (it * volumeMultiplier).toInt().toShort() }.toShortArray()
                }

                // Encode PCM → Opus for Voice Chat
                val encoded = encoder!!.encode(pcmShorts)
                audioQueue.add(encoded)
            }

            // Send queued audio to the player's channel
            if (audioQueue.isNotEmpty()) {
                val data = audioQueue.poll()
                channel?.send(data)
            }
        } catch (e: Exception) {
            Logger.warn("Error processing audio frame for ${player.name}: ${e.message}")
        }
    }
    
    /**
     * Fades out the audio over the specified duration, then stops playback.
     * 
     * @param durationMs Duration of the fade-out in milliseconds (default: 2000ms = 2 seconds)
     */
    fun fadeOutAndStop(durationMs: Long = 2000) {
        if (!running.get() || isFadingOut.getAndSet(true)) {
            stop()
            return
        }
        
        val startVolume = volumeMultiplier
        val startTime = System.currentTimeMillis()

        Logger.info("Starting fade-out for ${player.name} over ${durationMs}ms")
        
        // Create a scheduled task for fade-out
        fadeTask = AUDIO_EXECUTOR.scheduleAtFixedRate({
            try {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                
                // Linear fade-out: reduce volume from startVolume to 0
                volumeMultiplier = startVolume * (1f - progress)
                
                if (progress >= 1f) {
                    // Fade-out complete, stop the audio
                    fadeTask?.cancel(false)
                    stop()
                    Logger.info("Fade-out complete for ${player.name}")
                }
            } catch (e: Exception) {
                Logger.warn("Error during fade-out for ${player.name}: ${e.message}")
                stop()
            }
        }, 0, 50, TimeUnit.MILLISECONDS) // Update every 50ms for smooth fade
    }
    
    /**
     * Stops audio playback immediately and cleans up resources.
     */
    fun stop() {
        if (!running.compareAndSet(true, false)) {
            return // Already stopped
        }

        cleanup()
        Logger.info("Stopped audio player for ${player.name}")
    }

    /**
     * Internal cleanup method to release all resources.
     */
    private fun cleanup() {
        isFadingOut.set(false)

        // Cancel tasks
        fadeTask?.cancel(false)
        audioTask?.cancel(false)
        fadeTask = null
        audioTask = null

        // Stop LavaPlayer
        lavaPlayer.stopTrack()
        lavaPlayer.destroy()
        audioQueue.clear()
        
        // Close codecs
        try {
            decoder?.close()
            encoder?.close()
        } catch (e: Exception) {
            Logger.warn("Error closing codec for ${player.name}: ${e.message}")
        }
        
        decoder = null
        encoder = null
        channel = null
        volumeMultiplier = config.volume
    }
    
    /**
     * Checks if audio is currently playing.
     */
    fun isPlaying(): Boolean = lavaPlayer.playingTrack != null && running.get()
}


