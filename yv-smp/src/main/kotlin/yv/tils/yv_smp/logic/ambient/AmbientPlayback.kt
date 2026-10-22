/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.ambient

import java.util.UUID

/** Per-listener deadlines: entering an empty source never depends on a previous audience. */
internal class AmbientPlayback {
    private val nextPlay = mutableMapOf<UUID, Long>()

    data class Changes(val play: Set<UUID>, val stop: Set<UUID>)

    fun update(tick: Long, eligible: Set<UUID>, looping: Boolean, intervalTicks: Long): Changes {
        val departed = nextPlay.keys.minus(eligible)
        departed.forEach { nextPlay.remove(it) }
        val due = if (looping) eligible.filterTo(mutableSetOf()) {
            val deadline = nextPlay[it]
            deadline == null || tick >= deadline
        } else emptySet()
        record(tick, due, intervalTicks)
        return Changes(due, departed)
    }

    fun record(tick: Long, recipients: Set<UUID>, intervalTicks: Long) {
        recipients.forEach { nextPlay[it] = tick + intervalTicks }
    }

    fun clear(): Set<UUID> = nextPlay.keys.toSet().also { nextPlay.clear() }
}
