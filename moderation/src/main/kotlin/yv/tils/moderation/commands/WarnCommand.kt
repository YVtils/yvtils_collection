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
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.asyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import yv.tils.common.other.AsyncActionAnnounce
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.WarnLogic
import yv.tils.utils.logger.Logger
import java.util.concurrent.CompletableFuture

class WarnCommand {
    val command = commandTree("warn") {
        withPermission(Permissions.COMMAND_MODERATION_WARN.permission.name)
        withUsage("warn <player> [reason]")

        asyncPlayerProfileArgument("target") {
            greedyStringArgument("reason", true) {
                anyExecutor { sender, args ->
                    @Suppress("UNCHECKED_CAST")
                    val target = args["target"] as CompletableFuture<List<PlayerProfile>>
                    val reason = (args["reason"] ?: LanguageHandler.getRawMessage("moderation.placeholder.reason.none")) as String

                    AsyncActionAnnounce.announceAction(sender)

                    target.thenAccept { offlinePlayers ->
                        WarnLogic().triggerWarn(offlinePlayers, reason, sender)
                    }.exceptionally { throwable ->
                        AsyncActionAnnounce.announceError(sender)
                        Logger.error("Failed to fetch player profiles for ban command", throwable)
                        null
                    }
                }
            }
        }
    }
}