package yv.tils.gui.logic

import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitTask

data class ListContext(
    val holder: GuiHolder,
    val entryKey: String,
    val items: MutableList<String>,
    val backCallback: () -> BukkitTask,
    var perPage: Int = 10,
    var totalPages: Int = 0,
    var page: Int = 1,
    var inventory: Inventory? = null
)
