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

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.status.configs.ConfigFile
import yv.tils.status.utils.StatusUtils.Companion.generateDefaultStatus
import yv.tils.status.utils.StatusUtils.Companion.setStatusDisplay
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core

class StatusHandler {
    fun setStatus(player: Player, status: String) {
        val maxLength = ConfigFile.config["maxLength"] as Int

        if (MessageUtils.strip(status).length > maxLength) {
            player.sendMessage(LanguageHandler.getMessage(
                "command.status.input.tooLong",
                player,
                mapOf(
                    "prefix" to Core.prefix,
                    "maxLength" to maxLength.toString()
                )
            ))
            return
        }

        setStatusDisplayHandler(player, status)
    }

    fun setDefaultStatus(player: Player, status: String) {
        val suggestions = generateDefaultStatus()
        if (!suggestions.contains(status)) {
            player.sendMessage(LanguageHandler.getMessage(
                "command.status.default.notFound",
                player,
                mapOf(
                    "prefix" to Core.prefix,
                    "status" to status,
                )
            ))
            return
        }

        setStatusDisplayHandler(player, status)
    }

    fun clearStatus(player: Player, sender: CommandSender = player) {
        setStatusDisplay(player, "")
        player.sendMessage(LanguageHandler.getMessage(
            "command.status.clear.cleared.self",
            sender,
            mapOf(
                "prefix" to Core.prefix,
            )
        ))

        if (sender != player) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.status.clear.cleared.other",
                sender,
                mapOf(
                    "prefix" to Core.prefix,
                    "yv/tils/player" to player.name
                )
            ))
        }
    }

    private fun setStatusDisplayHandler(player: Player, status: String) {
        if (setStatusDisplay(player, status)) {
            val display = ConfigFile.config["display"] as String

            val displayCompo = MessageUtils.replacer(
                MessageUtils.convert(display),
                mapOf(
                    "status" to status,
                    "playerName" to player.name
                )
            )

            player.sendMessage(LanguageHandler.getMessage(
                "command.status.set",
                player,
                mapOf(
                    "prefix" to Core.prefix,
                    "status" to MessageUtils.convert(displayCompo)
                )
            ))
        }
    }
}
