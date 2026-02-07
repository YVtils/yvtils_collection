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

package yv.tils.gui.logic

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import yv.tils.config.data.ConfigEntry

class GuiHolder(
    val configName: String,
    val entries: MutableList<ConfigEntry>,
    val onSave: ((MutableList<ConfigEntry>) -> Unit)? = null,
) : InventoryHolder {
    private var invInternal: Inventory? = null

    fun setInventory(inv: Inventory) {
        this.invInternal = inv
    }

    override fun getInventory(): Inventory = invInternal!!
    var dirty: Boolean = false
    // current page for paginated views (1-based)
    var page: Int = 1
}
