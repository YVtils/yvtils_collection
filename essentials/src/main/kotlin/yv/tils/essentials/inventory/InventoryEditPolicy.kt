package yv.tils.essentials.inventory

import org.bukkit.inventory.ItemStack
import org.bukkit.Material

object InventoryEditPolicy {
    fun same(expected: ItemStack?, current: ItemStack?): Boolean {
        val first = expected?.takeUnless { it.type == Material.AIR || it.amount <= 0 }
        val second = current?.takeUnless { it.type == Material.AIR || it.amount <= 0 }
        return first == second
    }
}
