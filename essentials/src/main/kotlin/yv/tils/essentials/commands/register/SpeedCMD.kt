package yv.tils.essentials.commands.register

import data.Data
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import language.LanguageHandler
import org.bukkit.entity.Player
import yv.tils.essentials.commands.handler.SpeedHandler

class SpeedCMD {
    private val speedHandler = SpeedHandler()

    val command = commandTree("speed") {
        withPermission("yvtils.smp.command.speed")
        withPermission(CommandPermission.OP)
        withUsage("speed <speed> [player]")

        integerArgument("speed", -10, 10, false) {
            replaceSuggestions(
                ArgumentSuggestions.strings(
                    "10",
                    "9",
                    "8",
                    "7",
                    "6",
                    "5",
                    "4",
                    "3",
                    "2",
                    "1",
                    "0",
                    "-1",
                    "-2",
                    "-3",
                    "-4",
                    "-5",
                    "-6",
                    "-7",
                    "-8",
                    "-9",
                    "-10"
                )
            )
            playerArgument("player", true) {
                anyExecutor { sender, args ->
                    if (sender !is Player && args[1] == null) {
                        sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                        return@anyExecutor
                    }


                    if (args[1] is Player) {
                        val target = args[1] as Player
                        speedHandler.speedSwitch(target, args[0].toString(), sender)
                    } else {
                        speedHandler.speedSwitch(sender as Player, args[0].toString())
                    }
                }
            }
        }

        literalArgument("reset", false) {
            playerArgument("player", true) {
                anyExecutor { sender, args ->
                    if (sender !is Player && args[1] == null) {
                        sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                        return@anyExecutor
                    }

                    if (args[1] is Player) {
                        val target = args[1] as Player
                        speedHandler.speedReset(target, sender)
                    } else {
                        speedHandler.speedReset(sender as Player)
                    }
                }
            }
        }
    }
}