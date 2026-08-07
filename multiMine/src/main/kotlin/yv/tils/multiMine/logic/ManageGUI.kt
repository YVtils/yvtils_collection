/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.multiMine.logic

import org.bukkit.entity.Player
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.multiMine.configs.ConfigFile

class ManageGUI {
    fun openGUI(sender: Player) {
        DataClassConfigGui.open(
            sender,
            "MultiMine Config",
            ConfigFile.state,
            saver = { updated -> ConfigFile().applyState(updated) }
        )
    }
}
