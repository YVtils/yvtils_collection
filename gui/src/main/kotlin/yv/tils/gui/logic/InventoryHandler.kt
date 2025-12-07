/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.gui.logic

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

object InventoryHandler {
    fun createInventory(holder: GuiHolder, name: Component, size: Int): Inventory {
        val inv = Bukkit.createInventory(holder, size, name)
        holder.setInventory(inv)
        return inv
    }
}