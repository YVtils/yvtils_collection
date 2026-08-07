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

package yv.tils.essentials.commands.handler

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import yv.tils.configv2.language.LanguageHandler
import yv.tils.essentials.config.StatesFile
import yv.tils.essentials.language.LangStrings
import yv.tils.essentials.permissions.Permissions
import yv.tils.utils.logger.Logger
import yv.tils.utils.server.ServerUtils

class PvPHandler {
    fun handler(sender: CommandSender, state: Boolean? = null) {
        val currentState = getPvPState()

        if (state == null) {
            handler(sender, !currentState)
            return
        }

        if (state == currentState) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_PVP_ALREADY,
                    sender
                )
            )
            return
        }

        setPvPState(state)

        sender.sendMessage(
            LanguageHandler.getMessage(
                if (state) LangStrings.COMMAND_PVP_ENABLE else LangStrings.COMMAND_PVP_DISABLE,
                sender
            )
        )

        if (sender is Player) {
            Logger.info("PvP state changed to ${if (state) "enabled" else "disabled"} by ${sender.name}")
        }
    }

    fun checkStateHandler(sender: CommandSender) {
        val currentState = getPvPState()

        sender.sendMessage(
            LanguageHandler.getMessage(
                if (currentState) LangStrings.COMMAND_PVP_STATE_ENABLED else LangStrings.COMMAND_PVP_STATE_DISABLED,
                sender
            )
        )
    }

    fun onDamage(e: EntityDamageByEntityEvent) {
        if (getPvPState()) return

        if (e.entity is Player && e.damager is Player) {
            if (e.damager.hasPermission(Permissions.BYPASS_PVP_DISABLED.permission.name)) return
            e.isCancelled = true
            e.damager.sendActionBar(
                LanguageHandler.getMessage(
                    LangStrings.PVP_DISABLED,
                    e.damager
                )
            )
        }
    }

    fun getPvPState(): Boolean {
        return ServerUtils.isPvPEnabled
    }

    fun setPvPState(state: Boolean) {
        ServerUtils.isPvPEnabled = state
        savePvPState(state)
    }

    fun loadPvPState() {
        setPvPState(StatesFile.state.pvpState)
    }

    fun savePvPState(state: Boolean) {
        StatesFile().updatePvPState(state)
    }
}