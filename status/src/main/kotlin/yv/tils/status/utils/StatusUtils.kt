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

package yv.tils.status.utils

import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile
import yv.tils.status.logic.StatusTeamManager
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core

class StatusUtils {
    companion object {
        fun currentStatus(player: Player): SaveFile.StatusSave? {
            return SaveFile.saves[player.uniqueId]
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
                player.sendMessage(
                    LanguageHandler.getMessage(
                        "command.status.input.invalid",
                        player,
                        mapOf(
                            "prefix" to Core.prefix,
                            "status" to status,
                        )
                    )
                )

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
}