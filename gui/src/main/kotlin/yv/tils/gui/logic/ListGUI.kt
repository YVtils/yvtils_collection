package yv.tils.gui.logic

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import yv.tils.gui.utils.Filler
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils

class ListGUI {
    companion object {
        private val fillerItem = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        private val backItem = ItemStack(Material.ARROW)
    }

    fun openList(player: Player, context: ListContext) {
        // compute rows needed (border + content)
        val items = context.items
        val title = context.entryKey
        val backCallback = context.backCallback
        val contentSize = items.size
        val cols = 9
    val innerPerRow = 7 // leave borders
    val rowsNeeded = ((contentSize.toDouble() / innerPerRow).coerceAtLeast(1.0)).toInt()
        val totalRows = (rowsNeeded + 2).coerceAtMost(6) // cap to reasonable size
        val size = totalRows * cols

    val compTitle: Component = MessageUtils.replacer("<${Colors.MAIN.color}>$title", mapOf())
        val holder = context.holder
        val inv = InventoryHandler().createInventory(holder, compTitle, size)
        holder.setInventory(inv)
        context.inventory = inv

        val rows = size / 9

        // compute inner slots (leave a 1-slot border on all sides)
        val innerSlots = mutableListOf<Int>()
        for (r in 1 until rows - 1) {
            for (c in 1..innerPerRow) {
                innerSlots.add(r * 9 + c)
            }
        }

        // store per-page and total-pages in context before rendering
        val perPage = innerSlots.size
        context.perPage = perPage
        context.totalPages = ((items.size + perPage - 1) / perPage).coerceAtLeast(1)
        if (context.page > context.totalPages) context.page = context.totalPages

        // determine slice for current page
        val startIndex = (context.page - 1) * perPage
        val endIndex = (startIndex + perPage).coerceAtMost(items.size)
        val pageItems = if (startIndex < endIndex) items.subList(startIndex, endIndex) else emptyList()

        // place page items into inner slots left-to-right, top-to-bottom
        var idx = 0
        for (slotIndex in innerSlots) {
            if (idx >= pageItems.size) break
            val name = pageItems[idx]
            val mat = try {
                Material.valueOf(name)
            } catch (ex: Exception) {
                Material.PAPER
            }
            val it = ItemStack(mat)
            val meta = it.itemMeta
            meta?.displayName(MessageUtils.convert("<white>$name"))
            // add action lore
            val loreLines = listOf(
                "<dark_gray>————————",
                "<white>Actions:",
                "<gray>Left-click: <white>Toggle",
                "<gray>Right-click: <red>Remove",
                "<dark_gray>————————"
            )
            meta?.lore(MessageUtils.handleLore(loreLines.joinToString("<newline>")))
            it.itemMeta = meta
            inv.setItem(slotIndex, it)
            idx++
        }

    // fill border (do not overwrite inner slots)
    Filler().fillInventory(inv, blockedSlots = innerSlots.toMutableList())

    // navigation: prev/next if multiple pages
        if (context.totalPages > 1) {
            // prev (bottom-left)
            val prev = ItemStack(Material.ARROW)
            val pm = prev.itemMeta
            pm?.displayName(MessageUtils.convert("<yellow>Previous page"))
            prev.itemMeta = pm
            inv.setItem(size - 9, prev)

            // next (bottom-right)
            val next = ItemStack(Material.ARROW)
            val nm = next.itemMeta
            nm?.displayName(MessageUtils.convert("<yellow>Next page"))
            next.itemMeta = nm
            inv.setItem(size - 1, next)
        } else {
            // show add button on bottom-right
            val addItem = ItemStack(Material.NAME_TAG)
            val addMeta = addItem.itemMeta
            addMeta?.displayName(MessageUtils.convert("<green>Add block"))
            addItem.itemMeta = addMeta
            inv.setItem(size - 1, addItem)
        }

        player.openInventory(inv)
    }
}
