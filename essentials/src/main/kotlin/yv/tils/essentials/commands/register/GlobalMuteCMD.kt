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

package yv.tils.essentials.commands.register

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.stringArgument
import yv.tils.essentials.commands.handler.GlobalMuteHandler

class GlobalMuteCMD {
    val command = commandTree("globalmute") {
        withPermission("yvtils.command.globalmute")
        withPermission(CommandPermission.OP)
        withUsage("globalmute [state]")
        withAliases("gmute")

        val gmuteHandler = GlobalMuteHandler()

        stringArgument("state", true) {
            replaceSuggestions(ArgumentSuggestions.strings("true", "false", "toggle"))
            anyExecutor { sender, args ->
                gmuteHandler.globalMute(sender, args)
            }
        }

        anyExecutor { sender, _ ->
            gmuteHandler.globalMute(sender)
        }
    }
}