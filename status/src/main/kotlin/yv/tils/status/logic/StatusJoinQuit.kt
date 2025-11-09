/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.status.logic

import yv.tils.utils.data.Data
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.message.MessageUtils
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile

class StatusJoinQuit {
    fun loadPlayer(e: PlayerJoinEvent) {
        val player = e.player
        val status = SaveFile.saves[player.uniqueId] ?: return

        if (StatusHandler().setStatusDisplay(player, status.content)) {
            val display = ConfigFile.config["display"] as String

            val displayCompo = MessageUtils.replacer(
                MessageUtils.convert(display),
                mapOf(
                    "status" to status.content,
                    "playerName" to player.name
                )
            )

            player.sendMessage(
                LanguageHandler.getMessage(
                    "status.server.join",
                    player,
                    mapOf(
                        "prefix" to Data.prefix,
                        "status" to MessageUtils.convert(displayCompo),
                    )
                )
            )
        }
    }

    fun savePlayer(e: PlayerQuitEvent) {
        val player = e.player
        val team = player.scoreboard.getTeam(player.name)

        if (team != null) {
            StatusTeamManager().removePlayer(player)
        }
    }
}
