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
import yv.tils.status.data.Permissions
import yv.tils.status.utils.StatusDisplay
import yv.tils.status.utils.StatusUtils.Companion.generateDefaultStatus
import yv.tils.status.utils.StatusUtils.Companion.setStatusDisplay
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core
import yv.tils.utils.stats.ModuleTelemetry

class StatusHandler {
    fun setStatus(player: Player, status: String, sender: CommandSender = player) {
        if (!checkPermission(sender, player, Permissions.COMMAND_STATUS_SET)) return
        val maxLength = ConfigFile.state.maxLength

        if (MessageUtils.strip(status).length > maxLength) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.status.input.tooLong",
                    sender,
                    mapOf(
                        "prefix" to Core.prefix,
                        "maxLength" to maxLength.toString()
                    )
                )
            )
            return
        }

        setStatusDisplayHandler(player, status, sender)
    }

    fun setDefaultStatus(player: Player, status: String, sender: CommandSender = player) {
        if (!checkPermission(sender, player, Permissions.COMMAND_STATUS_DEFAULT)) return
        val suggestions = generateDefaultStatus()
        if (!suggestions.contains(status)) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.status.default.notFound",
                    sender,
                    mapOf(
                        "prefix" to Core.prefix,
                        "status" to status,
                    )
                )
            )
            return
        }

        setStatusDisplayHandler(player, status, sender)
    }

    fun clearStatus(player: Player, sender: CommandSender = player) {
        if (!checkPermission(sender, player, Permissions.COMMAND_STATUS_CLEAR)) return
        setStatusDisplay(player, "")
        player.sendMessage(
            LanguageHandler.getMessage(
                "command.status.clear.cleared.self",
                player,
                mapOf(
                    "prefix" to Core.prefix,
                )
            )
        )

        if (sender != player) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.status.clear.cleared.other",
                    sender,
                    mapOf(
                        "prefix" to Core.prefix,
                        "player" to player.name
                    )
                )
            )
        }
    }

    private fun checkPermission(sender: CommandSender, target: Player, action: Permissions): Boolean {
        if (StatusAuthorization.canChange(sender, target, action)) return true
        sender.sendMessage(LanguageHandler.getMessage(
            "status.permission.denied", sender, mapOf("prefix" to Core.prefix)
        ))
        return false
    }

    private fun setStatusDisplayHandler(player: Player, status: String, sender: CommandSender) {
        if (status.isEmpty()) {
            clearStatus(player, sender)
            return
        }
        if (setStatusDisplay(player, status, sender)) {
            ModuleTelemetry.record(ModuleTelemetry.Event.STATUS_UPDATES)
            player.sendMessage(
                StatusDisplay.message(
                    LanguageHandler.getRawMessage("command.status.set", player),
                    player.displayName(),
                    Core.prefix,
                )
            )
            if (sender != player) {
                sender.sendMessage(StatusDisplay.message(
                    LanguageHandler.getRawMessage("command.status.setOther", sender),
                    player.displayName(), Core.prefix, player.name,
                ))
            }
        }
    }
}
