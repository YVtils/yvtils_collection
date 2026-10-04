/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import yv.tils.remadeEnderDragon.configs.Attack
import yv.tils.remadeEnderDragon.language.Messages
import yv.tils.remadeEnderDragon.logic.FightManager
import yv.tils.remadeEnderDragon.data.Permissions
import yv.tils.remadeEnderDragon.gui.ManageGUI
import yv.tils.utils.logger.Logger

class DragonCommand(manager: FightManager) {
    val command = commandTree("redragon") {
        withAliases("remadeenderdragon")
        withPermission(Permissions.ADMIN.permission.name)
        withUsage("redragon <start/stop/status/reload/config/trigger>")
        literalArgument("start") {
            playerExecutor { p, _ ->
                Messages.send(p, if (manager.startHere(p.world) != null) "started" else "unavailable")
            }
        }
        literalArgument("stop") {
            playerExecutor { p, _ -> Messages.send(p, if (manager.stop(p.world)) "stopped" else "unavailable") }
        }
        literalArgument("status") {
            playerExecutor { p, _ ->
                val fight = manager.here(p.world)
                if (fight == null) Messages.send(p, "unavailable")
                else fight.sendStatus(p)
            }
        }
        literalArgument("reload") {
            anyExecutor { sender, _ ->
                runCatching { manager.reload() }
                    .onSuccess {
                        Logger.info("[Remade Ender Dragon] Configuration reloaded by ${sender.name}.")
                        Messages.send(sender, "reloaded")
                    }
                    .onFailure {
                        Logger.error("[Remade Ender Dragon] Configuration reload failed: ${it.message}", it)
                        Messages.send(sender, "reload-failed", mapOf("error" to (it.message ?: it.javaClass.simpleName)))
                    }
            }
        }
        literalArgument("config") {
            playerExecutor { p, _ -> ManageGUI(manager).openGUI(p) }
        }
        literalArgument("trigger") {
            stringArgument("attack") {
                replaceSuggestions(ArgumentSuggestions.strings(*Attack.entries.map { it.id }.toTypedArray()))
                playerExecutor { p, args ->
                    val attack = Attack.entries.firstOrNull { it.id == args["attack"] as String }
                    val fight = manager.startHere(p.world, preview = true)
                    if (attack == null || fight == null || !fight.trigger(attack, debug = true)) Messages.send(p, "unavailable")
                    else {
                        Logger.info("[Remade Ender Dragon] ${p.name} previewed ${attack.id} in ${p.world.name}.")
                        Messages.send(p, "result", mapOf("result" to Messages.plain("attack.${attack.id}", p)))
                    }
                }
            }
        }
        anyExecutor { sender, _ -> Messages.send(sender, "help") }
    }
}
