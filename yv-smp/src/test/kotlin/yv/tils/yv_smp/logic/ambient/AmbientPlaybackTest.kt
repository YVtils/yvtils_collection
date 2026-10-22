/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.ambient

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AmbientPlaybackTest {
    private val first = UUID.randomUUID()
    private val second = UUID.randomUUID()

    @Test
    fun `entry starts immediately and each listener repeats a full interval later`() {
        val playback = AmbientPlayback()
        assertEquals(setOf(first), playback.update(1, setOf(first), true, 100).play)
        assertEquals(setOf(second), playback.update(50, setOf(first, second), true, 100).play)
        assertEquals(setOf(first), playback.update(101, setOf(first, second), true, 100).play)
        assertEquals(setOf(second), playback.update(150, setOf(first, second), true, 100).play)
        assertEquals(setOf(first), playback.update(201, setOf(first, second), true, 100).play)
    }

    @Test
    fun `empty sources remain ready and reentry restarts playback`() {
        val playback = AmbientPlayback()
        assertTrue(playback.update(1000, emptySet(), true, 100).play.isEmpty())
        assertEquals(setOf(first), playback.update(1050, setOf(first), true, 100).play)
        assertEquals(setOf(first), playback.update(1060, emptySet(), true, 100).stop)
        assertEquals(setOf(first), playback.update(1061, setOf(first), true, 100).play)
        assertTrue(playback.update(1150, setOf(first), true, 100).play.isEmpty())
        assertEquals(setOf(first), playback.update(1161, setOf(first), true, 100).play)
    }

    @Test
    fun `non-looping sources only track explicit playback for exit cleanup`() {
        val playback = AmbientPlayback()
        assertTrue(playback.update(1, setOf(first), false, 100).play.isEmpty())
        playback.record(2, setOf(first), 100)
        assertTrue(playback.update(102, setOf(first, second), false, 100).play.isEmpty())
        assertEquals(setOf(first), playback.update(103, emptySet(), false, 100).stop)
        assertTrue(playback.update(104, setOf(first), false, 100).play.isEmpty())
    }

    @Test
    fun `manual play resets deadlines and clearing releases all listeners`() {
        val playback = AmbientPlayback()
        playback.update(1, setOf(first, second), true, 100)
        playback.record(50, setOf(first, second), 100)
        assertTrue(playback.update(101, setOf(first, second), true, 100).play.isEmpty())
        assertEquals(setOf(first, second), playback.update(150, setOf(first, second), true, 100).play)
        assertEquals(setOf(first, second), playback.clear())
        assertTrue(playback.clear().isEmpty())
        assertEquals(setOf(first), playback.update(151, setOf(first), true, 100).play)
    }
}
