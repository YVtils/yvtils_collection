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

package yv.tils.moderation.commands

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jorel.commandapi.kotlindsl.*
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.BanLogic

class BanCommand {
    val command = commandTree("ban") {
        withPermission(Permissions.COMMAND_MODERATION_BAN.permission.name)
        withUsage("ban <player> [reason]")

        playerProfileArgument("target") {
            greedyStringArgument("reason", true) {
                anyExecutor { sender, args ->
                    val target = args["target"] as List<PlayerProfile>
                    val reason = (args["reason"] ?: LanguageHandler.getMessage("moderation.placeholder.reason.none")) as String

                    BanLogic().triggerBan(target, reason, sender)
                }
            }
        }
    }
}