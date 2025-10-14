package yv.tils.gui.utils

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import yv.tils.utils.message.MessageUtils

class Filler {
    fun fillInventory(
        inv: Inventory,
        blockedSlots: MutableList<Int> = mutableListOf(),
        onlySlots: MutableList<Int> = mutableListOf()
    ): Inventory {
        for (i in 0 until inv.size) {
            if (onlySlots.isNotEmpty() && !onlySlots.contains(i)) continue
            if (blockedSlots.contains(i)) continue
            if (inv.getItem(i) == null) {
                inv.setItem(i, mainFillerItem())
            }
        }
        return inv
    }

    fun mainFillerItem(): ItemStack {
        val item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta = item.itemMeta
        meta.displayName(MessageUtils.convert(" "))
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        meta?.isHideTooltip = true
        item.itemMeta = meta
        return item
    }

    fun secondaryFillerItem(): ItemStack {
        val item = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
        val meta = item.itemMeta
        meta.displayName(MessageUtils.convert(" "))
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        meta?.isHideTooltip = true
        item.itemMeta = meta
        return item
    }
}