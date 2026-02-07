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

package yv.tils.sit.logic

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerQuitEvent

class DismountListener {
    fun onDismount(e: EntityDismountEvent) {
        val sitManager = SitManager()

        if (e.entity is Player) {
            val player = e.entity as Player
            if (e.dismounted is ArmorStand) {
                val sit = e.dismounted as ArmorStand
                sitManager.standUp(player, sit, 0.0, 1.975, 0.0)
            }
        }
    }

    fun onQuit(e: PlayerQuitEvent) {
        val sitManager = SitManager()
        if (sitManager.isSitting(e.player.uniqueId)) {
            sitManager.sitGetter(e.player)
        }
    }
}