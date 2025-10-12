package yv.tils.gui.logic

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

class InventoryHandler {
    fun createInventory(name: Component, size: Int): Inventory {
        val inv = Bukkit.createInventory(null, size, name)
        return inv
    }
}