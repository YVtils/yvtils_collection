package yv.tils.message.logic

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import java.util.*

class MessageHandler {
    companion object {
        val chatSession = mutableMapOf<UUID, UUID>()
    }

    fun sendMessage(sender: CommandSender, target: Player, message: String) {
        val senderName = if (sender is Player) {
            chatSession[sender.uniqueId] = target.uniqueId
            chatSession[target.uniqueId] = sender.uniqueId
            sender.name
        } else {
            "Console"
        }

        if (sender == target) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.msg.note",
                sender,
                mapOf(
                    "prefix" to Data.prefix,
                    "yv/tils/message" to message
                )
            ))
            return
        }

        val targetName = target.name

        sender.sendMessage(
            LanguageHandler.getMessage(
                "command.msg.message",
                sender,
                mapOf(
                    "prefix" to Data.prefix,
                    "sender" to senderName,
                    "receiver" to targetName,
                    "yv/tils/message" to message
                )
            )
        )
        target.sendMessage(
            LanguageHandler.getMessage(
                "command.msg.message",
                target.uniqueId,
                mapOf(
                    "prefix" to Data.prefix,
                    "sender" to senderName,
                    "receiver" to targetName,
                    "yv/tils/message" to message
                )
            )
        )
    }

    fun removeSession(uuid: UUID) {
        val target = chatSession[uuid]
        if (target != null) {
            chatSession.remove(target)
            chatSession.remove(uuid)
        }
    }

    fun clearSessions() {
        chatSession.clear()
    }
}
