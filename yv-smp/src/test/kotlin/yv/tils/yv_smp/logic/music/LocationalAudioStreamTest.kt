/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.music

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.sin

class LocationalAudioStreamTest {
    @TempDir
    lateinit var directory: Path

    private fun audioFile(): String {
        // A real 200 ms mono WAV exercises LavaPlayer, Opus conversion and end events.
        val samples = 9600
        val bytes = ByteBuffer.allocate(samples * 2).order(ByteOrder.LITTLE_ENDIAN)
        repeat(samples) { bytes.putShort((sin(it * 2 * Math.PI * 440 / 48000) * 10000).toInt().toShort()) }
        val file = directory.resolve("tone.wav").toFile()
        AudioInputStream(ByteArrayInputStream(bytes.array()), AudioFormat(48000f, 16, 1, true, false), samples.toLong()).use {
            AudioSystem.write(it, AudioFileFormat.Type.WAVE, file)
        }
        return file.absolutePath
    }

    @Test
    fun `shared stream loops across multiple track ends without any listeners and closes once`() {
        val frames = CountDownLatch(35) // More than three complete plays of the input.
        val flushes = AtomicInteger()
        val stream = LocationalAudioStream(
            "test", Any(), true, 1f,
            send = { _, packet -> assertTrue(packet.isNotEmpty()); frames.countDown() },
            flush = { flushes.incrementAndGet() },
        )
        try {
            stream.load(audioFile())
            assertTrue(frames.await(10, TimeUnit.SECONDS), "Audio must keep advancing and looping with no audience")
        } finally {
            stream.close()
        }
        stream.close()
        assertEquals(1, flushes.get())
    }

    @Test
    fun `non-looping stream finishes and flushes its channel`() {
        val finished = CountDownLatch(1)
        val frames = AtomicInteger()
        val stream = LocationalAudioStream(
            "once", Any(), false, 1f,
            send = { _, _ -> frames.incrementAndGet() }, flush = { finished.countDown() },
        )
        try {
            stream.load(audioFile())
            assertTrue(finished.await(10, TimeUnit.SECONDS))
            assertTrue(frames.get() in 1..12, "One short track must play exactly once")
        } finally {
            stream.close()
        }
    }

    @Test
    fun `closed stream rejects a late asynchronous load`() {
        val frames = AtomicInteger()
        val stream = LocationalAudioStream(
            "closed", Any(), true, 1f, send = { _, _ -> frames.incrementAndGet() }, flush = { },
        )
        stream.close()
        stream.load(audioFile())
        assertEquals(0, frames.get())
    }
}
