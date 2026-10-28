/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.logic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PushBackoffTest {
    @Test
    fun `first failure backs off even before any successful request`() {
        var now = 0L
        val backoff = PushBackoff { now }
        assertFalse(backoff.isWaiting())
        backoff.failed()
        assertTrue(backoff.isWaiting())
        assertEquals(60000L, backoff.remainingMillis())
        now = 60_000_000_000L
        assertFalse(backoff.isWaiting())
        assertEquals(0L, backoff.remainingMillis())
    }

    @Test
    fun `exponential delay is capped at one hour and reset by success`() {
        val backoff = PushBackoff { 0L }
        backoff.failed()
        backoff.failed()
        assertEquals(120000L, backoff.remainingMillis())
        repeat(20) { backoff.failed() }
        assertEquals(3600000L, backoff.remainingMillis())
        backoff.reset()
        assertEquals(0, backoff.failures)
        assertFalse(backoff.isWaiting())
    }

    @Test
    fun `monotonic deadline survives nanoTime overflow`() {
        var now = Long.MAX_VALUE - 10_000_000_000L
        val backoff = PushBackoff { now }
        backoff.failed()
        assertEquals(60000L, backoff.remainingMillis())
        now += 60_000_000_000L
        assertFalse(backoff.isWaiting())
    }
}
