/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.server.configs

import org.bukkit.entity.Player
import yv.tils.gui.logic.DataClassConfigGui

class ManageGUI {
    fun openGUI(sender: Player) {
        DataClassConfigGui.open(
            sender,
            "Server Config",
            ConfigFile.state,
            saver = { updated -> ConfigFile().applyState(updated) }
        )
    }
}
