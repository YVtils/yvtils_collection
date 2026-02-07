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

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.asyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandTree
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.gui.PlayerGUI

class ModGUICommand {
    val command = commandTree("modgui") {
        withPermission(Permissions.COMMAND_MODERATION_MODGUI.permission.name)
        withUsage("modgui [target]")

        asyncPlayerProfileArgument("target", true) {
            anyExecutor { sender, args ->
                PlayerGUI().openGUI(sender)

//                val target = args["target"] as CompletableFuture<List<PlayerProfile>>
//                val reason =
//                    (args["reason"] ?: LanguageHandler.getRawMessage("moderation.placeholder.reason.none")) as String
//
//                AsyncActionAnnounce.announceAction(sender)
//
//                target.thenAccept { offlinePlayers ->
//                    PlayerGUI().openGUI(sender)
//            }.exceptionally { throwable ->
//                AsyncActionAnnounce.announceError(sender)
//                Logger.error("Failed to fetch player profiles for the command")
//                Logger.debug("Error details", throwable, DEBUGLEVEL.DETAILED)
//                null
//            }
            }
        }
    }
}