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

package yv.tils.moderation.commands

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jorel.commandapi.kotlindsl.*
import yv.tils.common.other.AsyncActionAnnounce
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.TempBanLogic
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import java.util.concurrent.CompletableFuture

class TempBanCommand {
    val command = commandTree("tempban") {
        withPermission(Permissions.COMMAND_MODERATION_TEMPBAN.permission.name)
        withUsage("tempban <player> <duration> <unit> [reason]")

        asyncPlayerProfileArgument("target") {
            integerArgument("duration") {
                multiLiteralArgument("unit", "s", "m", "h", "d", "w") {
                    greedyStringArgument("reason", true) {
                        anyExecutor { sender, args ->
                            @Suppress("UNCHECKED_CAST")
                            val target = args["target"] as CompletableFuture<List<PlayerProfile>>
                            val duration = args["duration"] as Int
                            val unit = args["unit"] as String
                            val reason = (args["reason"] ?: LanguageHandler.getRawMessage("moderation.placeholder.reason.none")) as String

                            AsyncActionAnnounce.announceAction(sender)

                            target.thenAccept { offlinePlayers ->
                                TempBanLogic().triggerTempBan(offlinePlayers, reason, duration, unit, sender)
                            }.exceptionally { throwable ->
                                AsyncActionAnnounce.announceError(sender)
                                Logger.error("Failed to fetch player profiles for the command")
                                Logger.debug("Error details", throwable, DEBUGLEVEL.DETAILED)
                                null
                            }
                        }
                    }
                }
            }
        }
    }
}