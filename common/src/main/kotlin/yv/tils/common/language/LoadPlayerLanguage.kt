package yv.tils.common.language

import data.Data
import language.LanguageHandler
import logger.Logger
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLocaleChangeEvent
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