package yv.tils.status.commands

import data.Data
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import language.LanguageHandler
import org.bukkit.entity.Player
import yv.tils.status.logic.StatusHandler as handler

class StatusCommand {
    val command = commandTree("status") {
        withPermission("yvtils.command.status")
        withPermission(CommandPermission.NONE)
        withUsage("status <set/default/clear> [status/player]")
        withAliases("prefix", "role")

        literalArgument("set", false) {
            withPermission("yvtils.command.status.set")
            withPermission(CommandPermission.NONE)
            greedyStringArgument("status", false) {
                playerExecutor { player, args ->
                    handler().setStatus(player, args[0] as String)
                }
            }
        }

        literalArgument("default", false) {
            greedyStringArgument("status", false) {
                withPermission("yvtils.command.status.default")
                withPermission(CommandPermission.NONE)
                replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    val suggestions = handler().generateDefaultStatus()
                    suggestions.toTypedArray()
                })

                playerExecutor { player, args ->
                    handler().setDefaultStatus(player, args[0] as String)
                }
            }
        }

        literalArgument("clear", false) {
            withPermission("yvtils.command.status.clear")
            withPermission(CommandPermission.NONE)
            playerArgument("player", true) {
                withPermission("yvtils.command.status.clear.others")
                withPermission(CommandPermission.OP)
                anyExecutor { sender, args ->
                    if (sender !is Player && args[0] == null) {
                        sender.sendMessage(LanguageHandler.getMessage("command.missing.player", params = mapOf("prefix" to Data.prefix)))
                        return@anyExecutor
                    }

                    if (args[0] == null) {
                        if (!sender.hasPermission("yvtils.command.status.clear.others")) {
                            sender.sendMessage(LanguageHandler.getMessage(
                                "command.status.clear.notAllowed",
                                sender,
                                mapOf(
                                    "prefix" to Data.prefix,
                                )
                            ))
                            return@anyExecutor
                        }
                    }

                    if (args[0] is Player) {
                        val target = args[0] as Player
                        handler().clearStatus(target, sender)
                    } else {
                        handler().clearStatus(sender as Player)
                    }
                }
            }
        }
    }
}