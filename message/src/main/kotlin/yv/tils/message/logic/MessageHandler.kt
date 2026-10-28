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

package yv.tils.message.logic

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.message.data.Permissions
import yv.tils.utils.modules.Core
import yv.tils.utils.stats.ModuleTelemetry
import java.util.*

class MessageHandler {
    companion object {
        val chatSession = mutableMapOf<UUID, UUID>()
    }

    fun sendMessage(sender: CommandSender, target: Player, message: String) {
        if (!target.hasPermission(Permissions.RECEIVE.permission.name)) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.msg.cannotReceive",
                    sender,
                    mapOf("prefix" to Core.prefix, "receiver" to target.name)
                )
            )
            return
        }

        val senderName = if (sender is Player) {
            chatSession[sender.uniqueId] = target.uniqueId
            chatSession[target.uniqueId] = sender.uniqueId
            sender.name
        } else {
            "Console"
        }

        if (sender == target) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.msg.note",
                    sender,
                    mapOf(
                        "prefix" to Core.prefix,
                        "message" to message
                    )
                )
            )
            return
        }

        val targetName = target.name

        sender.sendMessage(
            LanguageHandler.getMessage(
                "command.msg.message",
                sender,
                mapOf(
                    "prefix" to Core.prefix,
                    "sender" to senderName,
                    "receiver" to targetName,
                    "message" to message
                )
            )
        )
        target.sendMessage(
            LanguageHandler.getMessage(
                "command.msg.message",
                target.uniqueId,
                mapOf(
                    "prefix" to Core.prefix,
                    "sender" to senderName,
                    "receiver" to targetName,
                    "message" to message
                )
            )
        )
        ModuleTelemetry.record(ModuleTelemetry.Event.MESSAGE_DELIVERIES)
    }

    fun sendMessages(
        sender: CommandSender,
        targets: Collection<Player>,
        message: String,
        requiresAllPermission: Boolean = targets.size > 1,
    ) {
        if (requiresAllPermission && !sender.hasPermission(Permissions.COMMAND_MSG_ALL.permission.name)) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.msg.multipleDenied",
                    sender,
                    mapOf("prefix" to Core.prefix)
                )
            )
            return
        }

        targets
            .filter { !requiresAllPermission || it.hasPermission(Permissions.RECEIVE.permission.name) }
            .forEach { sendMessage(sender, it, message) }
    }

    fun removeSession(uuid: UUID) {
        chatSession.entries.removeIf { it.key == uuid || it.value == uuid }
    }

    fun clearSessions() {
        chatSession.clear()
    }
}
