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
}
