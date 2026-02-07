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

package yv.tils.multiMine.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.multiMine.configs.MultiMineConfig

class PlayerJoin : Listener {
    @EventHandler
    fun onEvent(e: PlayerJoinEvent) {
        val uuid = e.player.uniqueId
        MultiMineConfig().addPlayer(uuid)
    }
}