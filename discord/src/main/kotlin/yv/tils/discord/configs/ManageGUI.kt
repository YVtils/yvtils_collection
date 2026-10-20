/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.discord.configs

import org.bukkit.entity.Player
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.common.permissions.Permissions

class ManageGUI {
    fun openGUI(sender: Player) {
        if (!sender.hasPermission(Permissions.YVTILS_MANAGE_COMMAND.permission.name)) return
        var expected = ConfigFile.state.detachedCopy()
        DataClassConfigGui.open(
            sender,
            "Discord Config",
            expected.detachedCopy(),
            saver = { updated ->
                check(sender.hasPermission(Permissions.YVTILS_MANAGE_COMMAND.permission.name)) { "No permission to save Discord configuration" }
                check(ConfigFile.state == expected) { "Discord configuration changed; reopen the editor" }
                ConfigFile().applyState(updated)
                expected = ConfigFile.state.detachedCopy()
            }
        )
    }
}
