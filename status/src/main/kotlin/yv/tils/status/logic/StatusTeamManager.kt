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

package yv.tils.status.logic

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

class StatusTeamManager {
    private fun checkTeam(name: String, player: Player): Team {
        val team = player.scoreboard.getTeam(name)
        if (team != null) {
            return team
        }
        return player.scoreboard.registerNewTeam(name)
    }

    fun addPlayer(player: Player, prefix: Component = Component.empty()) {
        val team = checkTeam(player.uniqueId.toString() + "UUID", player)
        team.prefix(prefix)
        team.addEntry(player.name)
    }

    fun removePlayer(player: Player) {
        val team = player.scoreboard.getEntryTeam(player.name)
        team?.removeEntry(player.name)
        team?.size?.let {
            if (it == 0) {
                team.unregister()
            }
        }
    }
}