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

package yv.tils.common.language

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLocaleChangeEvent
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.*

class LoadPlayerLanguage {
    fun localChange(e: PlayerLocaleChangeEvent) {
        val player = e.player
        languageLogic(player, e.locale())
    }

    private fun languageLogic(player: Player, lang: Locale) {
        Logger.debug("Player ${player.name} joined the server with language $lang")

        LanguageHandler().setPlayerLanguage(player.uniqueId, lang)
    }

    fun asyncCleanup() {
        val iterator = LanguageHandler.playerLang.keys.iterator()

        val onlinePlayers = Data.instance.server.onlinePlayers
        val onlinePlayerUUIDs = onlinePlayers.map { it.uniqueId }.toSet()

        while (iterator.hasNext()) {
            val uuid = iterator.next()

            if (!onlinePlayerUUIDs.contains(uuid)) {
                LanguageHandler().removePlayerLanguage(uuid)
                Logger.debug("Removed language data for player $uuid")
            }
        }
    }
}
