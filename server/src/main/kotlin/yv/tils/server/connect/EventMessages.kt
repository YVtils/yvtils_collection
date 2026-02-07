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

package yv.tils.server.connect

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.config.language.LanguageBroadcast
import yv.tils.server.configs.ConfigFile
import yv.tils.utils.logger.Logger

class EventMessages {
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val resp = messageHandler("event.player.join", e.player)

        if (resp) {
            e.joinMessage(null)
        }
    }

    fun onPlayerQuit(e: PlayerQuitEvent) {
        val resp = messageHandler("event.player.quit", e.player)

        if (resp) {
            e.quitMessage(null)
        }
    }

    fun onFakePlayerJoin(player: Player, cause: EventCause) {
        val resp = messageHandler("event.player.join", player)

        if (resp) {
            Logger.info("Broadcasting fake player join message for ${player.name} with cause: ${cause.title}")
        }
    }

    fun onFakePlayerQuit(player: Player, cause: EventCause) {
        Logger.info("Broadcasting fake player quit message for ${player.name} with cause: ${cause.title}")
        val resp = messageHandler("event.player.quit", player)

        if (resp) {
            Logger.info("Broadcasting fake player quit message for ${player.name} with cause: ${cause.title}")
        }
    }

    private fun messageHandler(messageKey: String, player: Player): Boolean {
        if (ConfigFile.config["event.joinQuit.message.enabled"] == false) {
            return false
        }

        LanguageBroadcast.broadcast(messageKey, mapOf(
            "player" to player.name,
        ))

        return true
    }

    enum class EventCause(val title: String) {
        VANISH("Vanish"),
    }
}
