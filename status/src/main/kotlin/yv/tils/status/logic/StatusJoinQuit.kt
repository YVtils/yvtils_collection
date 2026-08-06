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

import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.configv2.language.LanguageHandler
import yv.tils.status.configs.ConfigFile
import yv.tils.status.utils.StatusUtils
import yv.tils.status.utils.StatusUtils.Companion.setStatusDisplay
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core

class StatusJoinQuit {
    fun loadPlayer(e: PlayerJoinEvent) {
        val player = e.player
        val status = StatusUtils.currentStatus(player) ?: return

        if (setStatusDisplay(player, status.content)) {
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
                        "prefix" to Core.prefix,
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
