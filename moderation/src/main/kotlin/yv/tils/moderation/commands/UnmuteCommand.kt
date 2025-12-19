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
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.UnbanLogic
import java.util.concurrent.CompletableFuture

class UnmuteCommand {
    val command = commandTree("unmute") {
        withPermission(Permissions.COMMAND_MODERATION_UNMUTE.permission.name)
        withUsage("unmute <player> [reason]")

        playerProfileArgument("target") {
            replaceSuggestions(ArgumentSuggestions.stringsAsync { info ->
                CompletableFuture.supplyAsync {
                    info.sender.sendMessage(LanguageHandler.getMessage(yv.tils.common.language.LangStrings.COMMAND_SUGGESTION_ASYNC_ACTION.key))

                    MuteSaveFile().getAllMutes()
                }
            })

            greedyStringArgument("reason", true) {
                anyExecutor { sender, args ->
                    val target = args["target"] as List<PlayerProfile>
                    val reason = (args["reason"] ?: LanguageHandler.getMessage("moderation.placeholder.reason.none")) as String

                    UnbanLogic().triggerUnban(target, reason, sender)
                }
            }
        }
    }
}