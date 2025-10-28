package yv.tils.gui.logic

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import yv.tils.config.data.ConfigEntry
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.data.ConfigEntryTypes
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils

object ConfigGUI {
    private const val GUI_SIZE = 3 * 9
    private val guiTitle = "<${Colors.MAIN.color}><configName>"
    private val keySlots = listOf(10, 11, 12, 13, 14, 15, 16)

    fun slotToIndex(slot: Int): Int? {
        val idx = keySlots.indexOf(slot)
        return if (idx >= 0) idx else null
    }

    fun createGUI(
        player: Player,
        configName: String,
        config: MutableList<ConfigEntry>,
        saver: ((MutableList<ConfigEntry>) -> Unit)? = null,
        reuseHolder: GuiHolder? = null
    ) {
        val title = MessageUtils.replacer(guiTitle, mapOf("configName" to configName))
        // hide documentation entry from the UI (it's metadata), prevents off-by-one mapping
        val displayEntries = config.filter { it.key != "documentation" }.toMutableList()
        val holder = if (reuseHolder != null && reuseHolder.configName == configName) {
            // reuse existing holder state (page, etc.) and refresh entries
            reuseHolder.entries.clear()
            reuseHolder.entries.addAll(displayEntries)
            reuseHolder
        } else {
            GuiHolder(configName, displayEntries, saver)
        }
        
        // ensure holder.page is within bounds
        val perPage = keySlots.size
        val totalPages = ((displayEntries.size + perPage - 1) / perPage).coerceAtLeast(1)
        if (holder.page > totalPages) holder.page = totalPages

        var inv = InventoryHandler.createInventory(holder, title, GUI_SIZE)
        inv = fillInventory(inv, displayEntries, holder.page, player)
        inv = Filler.fillInventory(inv, blockedSlots = keySlots)

        // pagination controls: only when there are multiple pages
        if (totalPages > 1) {
            if (holder.page > 1) {
                inv.setItem(GUI_SIZE - 9, HeadUtils.createCustomHead(Heads.PREVIOUS_PAGE, player, "action.gui.nav.previousPage"))
            }
            if (holder.page < totalPages) {
                inv.setItem(GUI_SIZE - 1, HeadUtils.createCustomHead(Heads.NEXT_PAGE, player, "action.gui.nav.nextPage"))
            }
        }

        player.openInventory(inv)
    }

    private fun fillInventory(
        inv: Inventory,
        entries: List<ConfigEntry>,
        page: Int = 1,
        player: Player
    ): Inventory {
        val perPage = keySlots.size
        val startIndex = ((page - 1) * perPage).coerceAtLeast(0)
        val endIndex = (startIndex + perPage).coerceAtMost(entries.size)
        val pageSlice = if (startIndex < endIndex) entries.subList(startIndex, endIndex) else emptyList()

        pageSlice.forEachIndexed { slotIndex, entry ->
            buildKeyItem(entry, player)?.let { item ->
                inv.setItem(keySlots[slotIndex], item)
            }
        }
        return inv
    }

    private fun buildKeyItem(entry: ConfigEntry, player: Player): ItemStack? {
        val itemMaterial = entry.invItem ?: entry.dynamicInvItem?.invoke(entry) ?: return null
        val item = ItemStack(itemMaterial)
        val meta = item.itemMeta

        meta.displayName(MessageUtils.convert("<${Colors.MAIN.color}>${entry.key}"))
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)

        val valueStr = formatValue(entry.value, entry.defaultValue)
        val defaultStr = formatValue(entry.defaultValue)
        val actions = ConfigEntryTypes.fromEntryType(entry.type).clickActions
        
        val valueLabel = LanguageHandler.getRawMessage("action.gui.lore.value", player.uniqueId)
        val defaultLabel = LanguageHandler.getRawMessage("action.gui.lore.default", player.uniqueId)
        val actionsLabel = LanguageHandler.getRawMessage("action.gui.lore.actions", player.uniqueId)
        
        val actionsLines = actions.joinToString("<newline>") { 
            "<gray>${it.getName()}: <white>${it.getDescription()}"
        }

        val loreLines = buildList {
            add("<dark_gray>————————")
            if (!entry.description.isNullOrBlank()) {
                add("<gray>${entry.description}")
                add("<dark_gray>————————")
            }
            add("<white>$valueLabel: <green>$valueStr")
            add("<white>$defaultLabel: <yellow>$defaultStr")
            add("<dark_gray> ")
            add("<white>$actionsLabel:")
            add(actionsLines)
            add("<dark_gray>————————")
        }

        meta.lore(MessageUtils.handleLore(loreLines.joinToString("<newline>")))
        item.itemMeta = meta
        return item
    }

    private fun formatValue(value: Any?, fallback: Any? = null): String {
        val v = value ?: fallback ?: return "<none>"
        return when (v) {
            is List<*> -> {
                val list = v.take(5).joinToString("\n", "\n", "") { "- $it" }
                if (v.size > 5) "$list\n..." else list
            }
            is Map<*, *> -> {
                val entries = v.entries.take(5).joinToString("\n", "\n", "") { "- ${it.key}=${it.value}" }
                if (v.entries.size > 5) "$entries\n..." else entries
            }
            else -> v.toString()
        }
    }
}

