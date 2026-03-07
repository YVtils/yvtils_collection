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

package yv.tils.yv_smp.logic

import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.utils.player.PlayerUtils
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.logic.music.MusicAPI
import yv.tils.yv_smp.logic.start.PhaseHandler
import yv.tils.yv_smp.logic.start.PreparationPhase.Companion.savedGameModes

class StartLogic {
    fun start(sender: CommandSender) {
        val players = PlayerUtils.onlinePlayersAsPlayers

        players.forEach { player ->
            player.stopAllSounds()
            savedGameModes[player.uniqueId] = player.gameMode
            player.gameMode = GameMode.SPECTATOR
            players.forEach { other -> if (other != player) player.hidePlayer(Data.instance, other) }
        }

        MusicAPI.broadcastUrl(
            "https://soundcloud.com/tunetankcom/victor-wayne-battle-phase-dark-epic-tension-music-copyright-free",
        )

        PhaseHandler.startPhases(players)

        sender.sendMessage(LanguageHandler.getMessage(LangStrings.START_COMMAND_SUCCESS, sender))
    }
}