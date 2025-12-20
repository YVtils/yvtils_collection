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
import org.bukkit.inventory.InventoryHolder
import yv.tils.gui.core.GuiGenericHolder

object InventoryHandler {
    /**
     * Creates an inventory with a GuiHolder (legacy config system).
     */
    fun createInventory(holder: GuiHolder, name: Component, size: Int): Inventory {
        val inv = Bukkit.createInventory(holder, size, name)
        holder.setInventory(inv)
        return inv
    }

    /**
     * Creates an inventory with a GuiGenericHolder (new generic system).
     */
    fun createInventory(holder: GuiGenericHolder, name: Component, size: Int): Inventory {
        val inv = Bukkit.createInventory(holder, size, name)
        holder.setInventory(inv)
        return inv
    }

    /**
     * Creates an inventory with any InventoryHolder (generic fallback).
     */
    fun createInventory(holder: InventoryHolder, name: Component, size: Int): Inventory {
        return Bukkit.createInventory(holder, size, name)
    }
}