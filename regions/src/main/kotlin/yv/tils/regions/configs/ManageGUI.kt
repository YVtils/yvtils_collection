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

package yv.tils.regions.configs

import org.bukkit.entity.Player
import yv.tils.regions.logic.ClaimService
import yv.tils.regions.data.Permissions
import yv.tils.regions.gui.ClaimsGui

class ManageGUI {
    fun openGUI(sender: Player) {
        if (!Permissions.ADMIN_CONFIG.allowed(sender)) return
        ClaimsGui.configuration(sender)
    }
}
