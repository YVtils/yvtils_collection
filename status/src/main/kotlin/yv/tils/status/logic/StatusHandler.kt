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

import yv.tils.utils.data.Data
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile

class StatusHandler {
    fun setStatus(player: Player, status: String) {
        val maxLength = ConfigFile.config["maxLength"] as Int

        if (MessageUtils.strip(status).length > maxLength) {
            player.sendMessage(LanguageHandler.getMessage(
                "command.status.input.tooLong",
                player,
                mapOf(
                    "prefix" to Data.prefix,
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
                    "prefix" to Data.prefix,
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
                "prefix" to Data.prefix,
            )
        ))

        if (sender != player) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.status.clear.cleared.other",
                sender,
                mapOf(
                    "prefix" to Data.prefix,
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
                    "prefix" to Data.prefix,
                    "status" to MessageUtils.convert(displayCompo)
                )
            ))
        }
    }

    fun setStatusDisplay(player: Player, status: String): Boolean {
        if (status == "") {
            player.displayName(MessageUtils.convert(player.name))
            player.playerListName(MessageUtils.convert(player.name))
            StatusTeamManager().removePlayer(player)
            SaveFile().updatePlayerSetting(player.uniqueId, "")
            return false
        }

        if (checkBlacklist(status)) {
            player.sendMessage(LanguageHandler.getMessage(
                "command.status.input.invalid",
                player,
                mapOf(
                    "prefix" to Data.prefix,
                    "status" to status,
                )
            ))

            setStatusDisplay(player, "")
            return false
        }

        val display = ConfigFile.config["display"] as String

        val displayCompo = MessageUtils.replacer(
            MessageUtils.convert(display),
            mapOf(
                "status" to status,
                "playerName" to player.name
            )
        )

        val displayCompoNameTag = MessageUtils.replacer(
            MessageUtils.convert(display),
            mapOf(
                "status" to status,
                "playerName" to ""
            )
        )

        player.displayName(displayCompo)
        player.playerListName(displayCompo)
        StatusTeamManager().addPlayer(player, displayCompoNameTag)

        SaveFile().updatePlayerSetting(player.uniqueId, status)

        return true
    }

    fun generateDefaultStatus(): Collection<String> {
        val list = ConfigFile.config["defaultStatus"] as? List<*> ?: return emptyList()

        Logger.debug("Default status list: $list")

        return list.filterIsInstance<String>()
    }

    private fun checkBlacklist(status: String): Boolean {
        val blacklist = ConfigFile.config["blacklist"] as? List<*> ?: return false

        Logger.debug("Blacklist: $blacklist")

        return blacklist.contains(status)
    }
}
