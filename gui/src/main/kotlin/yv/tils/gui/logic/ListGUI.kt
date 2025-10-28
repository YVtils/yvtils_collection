package yv.tils.gui.logic

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils

object ListGUI {
    fun openList(player: Player, context: ListContext) {
        val items = context.items
        val title = context.entryKey
        val contentSize = items.size
        val innerPerRow = 7 // leave borders
        val rowsNeeded = ((contentSize.toDouble() / innerPerRow).coerceAtLeast(1.0)).toInt()
        val totalRows = (rowsNeeded + 2).coerceAtMost(6) // cap to reasonable size
        val size = totalRows * 9

        val compTitle: Component = MessageUtils.replacer("<${Colors.MAIN.color}>$title", mapOf())
        val holder = context.holder
        val inv = InventoryHandler.createInventory(holder, compTitle, size)
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
        pageItems.forEachIndexed { idx, name ->
            val slotIndex = innerSlots.getOrNull(idx) ?: return@forEachIndexed
            inv.setItem(slotIndex, createListItem(name, player))
        }

        Filler.fillInventory(inv, blockedSlots = innerSlots)

        if (context.page > 1) {
            inv.setItem(size - 9, HeadUtils.createCustomHead(Heads.PREVIOUS_PAGE, player, "action.gui.nav.previousPage"))
        } else {
            // On first page, put a quit item in the back slot
            inv.setItem(size - 9, HeadUtils.createCustomHead(Heads.X_CHARACTER, player, "action.gui.nav.quit"))
        }

        // bottom-center: always show Add
        inv.setItem(size - 5, HeadUtils.createCustomHead(Heads.PLUS_CHARACTER, player, "action.gui.nav.addItem"))

        if (context.page < context.totalPages) {
            inv.setItem(size - 1, HeadUtils.createCustomHead(Heads.NEXT_PAGE, player, "action.gui.nav.nextPage"))
        }

        player.openInventory(inv)
    }

    private fun createListItem(name: String, player: Player): ItemStack {
        val mat = try {
            Material.valueOf(name)
        } catch (_: Exception) {
            Material.BARRIER
        }
        val item = ItemStack(mat)
        val meta = item.itemMeta
        meta.displayName(MessageUtils.convert("<white>$name"))
        
        val actionsLabel = LanguageHandler.getRawMessage("action.gui.list.actions", player.uniqueId)
        val removeAction = LanguageHandler.getRawMessage("action.gui.list.remove", player.uniqueId)
        
        val loreLines = listOf(
            "<dark_gray>————————",
            "<white>$actionsLabel:",
            "<white>$removeAction",
            "<dark_gray>————————"
        )
        meta.lore(MessageUtils.handleLore(loreLines.joinToString("<newline>")))
        item.itemMeta = meta
        return item
    }
}
