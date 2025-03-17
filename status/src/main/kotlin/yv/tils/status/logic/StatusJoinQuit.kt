package yv.tils.status.logic

import data.Data
import language.LanguageHandler
import message.MessageUtils
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile

class StatusJoinQuit {
    fun loadPlayer(e: PlayerJoinEvent) {
        val player = e.player
        val status = SaveFile.saves[player.uniqueId] as String? ?: return

        if (StatusHandler().setStatusDisplay(player, status)) {
            val display = ConfigFile.config["display"] as String

            val displayCompo = MessageUtils.replacer(
                MessageUtils.convert(display),
                mapOf(
                    "status" to status,
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