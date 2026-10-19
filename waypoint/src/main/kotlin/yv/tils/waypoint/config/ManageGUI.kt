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

package yv.tils.waypoint.config

import org.bukkit.entity.Player
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.utils.message.MessageUtils
import yv.tils.waypoint.language.WaypointText
import yv.tils.waypoint.permissions.Permissions

object ManageGUI {
    fun open(player: Player) {
        if (!player.hasPermission(Permissions.CONFIG.permission.name)) {
            WaypointText.send(player, "denied")
            return
        }
        DataClassConfigGui.open(
            player,
            MessageUtils.strip(WaypointText.text(player, "config")),
            ConfigFile.state.copy()
        ) {
            check(player.hasPermission(Permissions.CONFIG.permission.name))
            ConfigFile.apply(it)
        }
    }
}
