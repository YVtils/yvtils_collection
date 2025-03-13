package yv.tils.essentials.commands.register

import data.Data
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.stringArgument
import language.LanguageHandler
import org.bukkit.entity.Player
import yv.tils.essentials.commands.handler.GamemodeHandler

class GamemodeCMD {
    val command = commandTree("gm") {
        withPermission("yvtils.smp.command.gamemode")
        withPermission(CommandPermission.OP)
        withUsage("gm <gamemode> [player]")
        withAliases("gamemode")

        stringArgument("gamemode", false) {
            replaceSuggestions(
                ArgumentSuggestions.strings(
                    "survival",
                    "creative",
                    "adventure",
                    "spectator",
                    "0",
                    "1",
                    "2",
                    "3"
                )
            )
            playerArgument("player", true) {
                anyExecutor { sender, args ->

                    if (sender !is Player && args[1] == null) {
                        sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                        return@anyExecutor
                    }

                    val gmHandler = GamemodeHandler()

                    if (args[1] is Player) {
                        val target = args[1] as Player
                        gmHandler.gamemodeSwitch(target, args[0].toString(), sender)
                    } else {
                        gmHandler.gamemodeSwitch(sender as Player, args[0].toString())
                    }
                }
            }
        }
    }
}