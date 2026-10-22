/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.music

import com.sedmelluq.discord.lavaplayer.format.OpusAudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import de.maxhenkel.opus4j.OpusDecoder
import de.maxhenkel.opus4j.OpusEncoder
import dev.lavalink.youtube.YoutubeAudioSourceManager
import yv.tils.utils.logger.Logger
import yv.tils.yv_smp.logic.music.svc.VoicechatBridge
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** One decoder/clock/channel per source, never per listener. Playback advances even without an audience. */
internal class LocationalAudioStream(
    private val name: String,
    private val channel: Any,
    private val looping: Boolean,
    volume: Float,
    private val send: (Any, ByteArray) -> Unit = VoicechatBridge::send,
    private val flush: (Any) -> Unit = VoicechatBridge::flush,
) : AutoCloseable {
    companion object {
        private val manager = DefaultAudioPlayerManager().apply {
            configuration.outputFormat = OpusAudioDataFormat(1, 48000, 960)
            registerSourceManager(YoutubeAudioSourceManager())
            AudioSourceManagers.registerRemoteSources(this, YoutubeAudioSourceManager::class.java)
            AudioSourceManagers.registerLocalSource(this)
        }
        private val executor = Executors.newScheduledThreadPool(2) { runnable ->
            Thread(runnable, "YVtils-Positional-Audio").apply { isDaemon = true }
        }

        fun shutdown() {
            executor.shutdownNow()
            manager.shutdown()
        }
    }

    private val lock = Any()
    private val player = manager.createPlayer().apply { this.volume = (volume * 100).toInt() }
    private var decoder: OpusDecoder? = null
    private var encoder: OpusEncoder? = null
    private var task: ScheduledFuture<*>? = null
    private var closed = false

    init {
        player.addListener(object : AudioEventAdapter() {
            override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
                synchronized(lock) {
                    if (closed) return
                    when (endReason) {
                        AudioTrackEndReason.FINISHED -> if (looping) {
                            player.playTrack(track.makeClone())
                        } else close()
                        AudioTrackEndReason.LOAD_FAILED -> fail("track decoding failed")
                        else -> Unit // Explicit replacement/stop must not end the replacement stream.
                    }
                }
            }
        })
    }

    fun load(identifier: String) {
        manager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = start(track)
            override fun playlistLoaded(playlist: AudioPlaylist) {
                val track = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                if (track == null) fail("playlist is empty") else start(track)
            }
            override fun noMatches() = fail("no audio found")
            override fun loadFailed(exception: FriendlyException) = fail(exception.message ?: "loading failed")
        })
    }

    private fun start(track: AudioTrack) = synchronized(lock) {
        if (closed) return@synchronized
        try {
            decoder = OpusDecoder(48000, 1).apply { frameSize = 960 }
            encoder = OpusEncoder(48000, 1, OpusEncoder.Application.AUDIO)
            player.playTrack(track)
            task = executor.scheduleAtFixedRate({ frame() }, 0, 20, TimeUnit.MILLISECONDS)
        } catch (exception: Exception) {
            fail(exception.message ?: "initialization failed")
        }
    }

    private fun frame() = synchronized(lock) {
        if (closed) return@synchronized
        try {
            val frame = player.provide() ?: return@synchronized
            // End events may close the stream synchronously from provide().
            if (!closed) send(channel, encoder!!.encode(decoder!!.decode(frame.data)))
        } catch (exception: Exception) {
            fail(exception.message ?: "audio processing failed")
        }
    }

    private fun fail(message: String) = synchronized(lock) {
        if (!closed) {
            Logger.warn("Ambient audio '$name': $message. Use ambient play or edit the source to retry.")
            close()
        }
    }

    override fun close() = synchronized(lock) {
        if (closed) return@synchronized
        closed = true // Also rejects asynchronous load results arriving after removal/disable.
        task?.cancel(false)
        player.destroy()
        try {
            flush(channel)
        } finally {
            try {
                decoder?.close()
            } finally {
                encoder?.close()
            }
        }
    }
}
