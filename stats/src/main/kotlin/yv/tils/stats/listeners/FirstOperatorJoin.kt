/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.stats.StatsYVtils

/** One non-repeating prompt per boot, only while the administrator has not decided. */
internal class FirstOperatorPrompt {
    private var prompted = false

    fun claim(operator: Boolean, undecided: Boolean): Boolean {
        if (prompted || !operator || !undecided) return false
        prompted = true
        return true
    }
}

internal class FirstOperatorJoin : Listener {
    private val prompt = FirstOperatorPrompt()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        if (prompt.claim(event.player.isOp, StatsYVtils.needsOptInPrompt())) {
            StatsYVtils.requestOptInPrompt(event.player)
        }
    }
}
