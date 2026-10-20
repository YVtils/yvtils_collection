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

package yv.tils.server.maintenance

import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageBroadcast
import yv.tils.config.language.LanguageHandler
import yv.tils.server.configs.ConfigFile
import yv.tils.utils.modules.Core
import yv.tils.utils.player.PlayerUtils
import yv.tils.utils.server.ServerUtils

class MaintenanceHandler {
    companion object {
        const val BYPASS_PERMISSION = "yvtils.bypass.maintenance"

        var maintenance: Boolean = false
            private set

        /** Synchronizes shared state before enforcing a configuration change. */
        fun applyState(enabled: Boolean, announce: Boolean = true) {
            val changed = maintenance != enabled
            maintenance = enabled
            ServerUtils.serverInMaintenance = enabled

            if (!announce || !changed) return

            if (enabled) {
                PlayerUtils.onlinePlayersAsPlayers(BYPASS_PERMISSION, false).forEach { player ->
                    player.kick(
                        LanguageHandler.getMessage(
                            "maintenance.player.join.unallowed",
                            player,
                            mapOf("prefix" to Core.prefix)
                        )
                    )
                }
            }

            LanguageBroadcast.broadcast(
                "maintenance.announcement.global",
                BYPASS_PERMISSION,
                mapOf(
                    "prefix" to Core.prefix,
                    "state" to LanguageHandler.getRawMessage(if (enabled) "maintenance.state.on" else "maintenance.state.off"),
                )
            )
        }
    }

    fun maintenance(sender: CommandSender, args: CommandArguments? = null) {
        val enabled = when (args?.get("state")?.toString() ?: "toggle") {
            "toggle" -> !maintenance
            "true" -> true
            "false" -> false
            else -> return
        }
        val changed = maintenance != enabled
        ConfigFile.set("maintenance.enabled", enabled)

        sender.sendMessage(
            LanguageHandler.getMessage(
                if (changed) "maintenance.announcement.sender.changed" else "maintenance.announcement.sender.already",
                sender,
                mapOf(
                    "prefix" to Core.prefix,
                    "state" to LanguageHandler.getRawMessage(
                        if (enabled) "maintenance.state.on" else "maintenance.state.off", sender
                    ),
                )
            )
        )
    }

    fun loadState() {
        applyState(ConfigFile.state.maintenance.enabled, announce = false)
    }
}
