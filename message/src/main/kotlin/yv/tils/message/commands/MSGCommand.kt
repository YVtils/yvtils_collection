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

package yv.tils.message.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import yv.tils.message.logic.MessageHandler

class MSGCommand {
    val command = commandTree("msg") {
        withPermission("yvtils.command.msg")
        withPermission(CommandPermission.NONE)
        withUsage("msg <player> <message>")
        withAliases("yv/tils/message", "tell", "whisper", "dm", "w")

        playerArgument("yv/tils/player") {
            greedyStringArgument("yv/tils/message") {
                anyExecutor { sender, args ->
                    val target = args[0] as Player
                    val message = args[1] as String

                    MessageHandler().sendMessage(sender, target, message)
                }
            }
        }
    }
}
