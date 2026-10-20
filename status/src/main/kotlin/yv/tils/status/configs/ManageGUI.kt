/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.status.configs

import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.status.data.Permissions
import yv.tils.utils.modules.Core

class ManageGUI {
    fun openGUI(sender: Player) {
        if (!canConfigure(sender)) return
        DataClassConfigGui.open(
            sender,
            "Status Config",
            ConfigFile.state,
            saver = { updated ->
                if (canConfigure(sender)) ConfigFile().applyState(updated)
            }
        )
    }

    private fun canConfigure(player: Player): Boolean {
        if (player.hasPermission(Permissions.CONFIG.permission.name)) return true
        player.sendMessage(LanguageHandler.getMessage(
            "status.permission.denied", player, mapOf("prefix" to Core.prefix)
        ))
        return false
    }
}
