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

package yv.tils.gui.core

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * Generic inventory holder for the new GUI system.
 * This replaces the config-specific GuiHolder for generic GUIs.
 */
class GuiGenericHolder(
    val context: GuiContext
) : InventoryHolder {
    private var invInternal: Inventory? = null

    fun setInventory(inv: Inventory) {
        this.invInternal = inv
    }

    override fun getInventory(): Inventory = invInternal
        ?: throw IllegalStateException("Inventory not initialized for GUI ${context.guiId}")
}
