/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.logic

/** Monotonic retry deadline, independent of the last successful request or wall-clock changes. */
internal class PushBackoff(private val nanoTime: () -> Long = System::nanoTime) {
    var failures: Long = 0
        private set
    private var retryAt: Long? = null

    fun isWaiting(): Boolean = retryAt?.let { nanoTime() - it < 0 } ?: false

    fun remainingMillis(): Long = retryAt?.let {
        val remaining = it - nanoTime()
        if (remaining <= 0) 0 else (remaining + 999_999) / 1_000_000
    } ?: 0

    fun failed() {
        failures++
        val seconds = (60L shl (failures - 1).coerceAtMost(6).toInt()).coerceAtMost(3600)
        retryAt = nanoTime() + seconds * 1_000_000_000L
    }

    fun reset() {
        failures = 0
        retryAt = null
    }
}
