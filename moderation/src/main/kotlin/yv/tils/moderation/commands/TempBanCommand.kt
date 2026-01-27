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
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.TempBanLogic
import yv.tils.utils.logger.Logger

class TempBanCommand {
    val command = commandTree("tempban") {
        withPermission(Permissions.COMMAND_MODERATION_TEMPBAN.permission.name)
        withUsage("tempban <player> <duration> <unit> [reason]")

        playerProfileArgument("target") {
            integerArgument("duration") {
                textArgument("unit") {
                    replaceSuggestions(ArgumentSuggestions.strings("s", "m", "h", "d", "w"))

                    greedyStringArgument("reason", true) {
                        anyExecutor { sender, args ->
                            val target = args["target"] as List<PlayerProfile>
                            val duration = args["duration"] as Int
                            val unit = args["unit"] as String
                            val reason = (args["reason"] ?: LanguageHandler.getRawMessage("moderation.placeholder.reason.none")) as String

                            TempBanLogic().triggerTempBan(target, reason, duration, unit, sender)
                        }
                    }
                }
            }
        }
    }
}