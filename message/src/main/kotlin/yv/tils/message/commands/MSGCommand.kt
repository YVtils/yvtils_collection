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

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentManyPlayers
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import org.bukkit.entity.Player
import yv.tils.message.data.Permissions
import yv.tils.message.logic.MessageHandler

class MSGCommand {
    val command = commandTree("msg") {
        withPermission(Permissions.COMMAND_MSG.permission.name)
        withUsage("msg <player|@a> <message>")
        withAliases("message", "tell", "whisper", "dm", "w")

        entitySelectorArgumentManyPlayers("player", allowEmpty = false) {
            greedyStringArgument("message") {
                anyExecutor { sender, args ->
                    @Suppress("UNCHECKED_CAST")
                    val targets = args[0] as Collection<Player>
                    val message = args[1] as String

                    MessageHandler().sendMessages(
                        sender, targets, message,
                        requiresAllPermission = targets.size > 1 || args.getRaw(0)?.startsWith("@a") == true
                    )
                }
            }
        }
    }
}
