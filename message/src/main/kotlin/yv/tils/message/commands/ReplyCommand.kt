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

package yv.tils.message.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.message.logic.MessageHandler
import yv.tils.utils.data.Data

class ReplyCommand {
    val command = commandTree("reply") {
        withPermission("yvtils.command.reply")
        withPermission(CommandPermission.NONE)
        withUsage("reply <message>")
        withAliases("r")

        greedyStringArgument("yv/tils/message") {
            playerExecutor { sender, args ->
                reply(sender, args)
            }
        }
    }

    private fun reply(sender: Player, args: CommandArguments) {
        val target = MessageHandler.chatSession[sender.uniqueId] ?: run {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.reply.noSession",
                    sender.uniqueId,
                    mapOf(
                        "prefix" to Data.prefix
                    )
                )
            )
            return
        }

        val message = args[0] as String
        MessageHandler().sendMessage(sender, Bukkit.getPlayer(target)!!, message)
    }
}
