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