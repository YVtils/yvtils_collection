package yv.tils.essentials.commands.handler

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data

class PingHandler {
    fun ping(player: Player, sender: CommandSender = player) {
        val ping = player.ping

        if (sender != player) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.ping.other",
                    sender,
                    params = mapOf("prefix" to Data.prefix, "player" to player.name, "ping" to ping.toString())
                )
            )
        } else {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.ping.self",
                    sender,
                    params = mapOf("prefix" to Data.prefix, "ping" to ping.toString())
                )
            )
        }
    }
}
