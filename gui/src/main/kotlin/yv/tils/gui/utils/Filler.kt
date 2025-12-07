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

package yv.tils.gui.utils

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import yv.tils.utils.message.MessageUtils

object Filler {
    fun fillInventory(
        inv: Inventory,
        blockedSlots: List<Int> = emptyList(),
        onlySlots: List<Int> = emptyList()
    ): Inventory {
        for (i in 0 until inv.size) {
            if (onlySlots.isNotEmpty() && i !in onlySlots) continue
            if (i in blockedSlots) continue
            if (inv.getItem(i) == null) {
                inv.setItem(i, mainFillerItem())
            }
        }
        return inv
    }

    fun mainFillerItem(): ItemStack = createFillerItem(Material.GRAY_STAINED_GLASS_PANE)

    fun secondaryFillerItem(): ItemStack = createFillerItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE)

    private fun createFillerItem(material: Material): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(MessageUtils.convert(" "))
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        meta.isHideTooltip = true
        item.itemMeta = meta
        return item
    }
}