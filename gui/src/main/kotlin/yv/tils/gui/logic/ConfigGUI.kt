package yv.tils.gui.logic

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import yv.tils.config.data.ConfigEntry
import yv.tils.gui.data.ConfigEntryTypes
import yv.tils.gui.utils.Filler
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils

class ConfigGUI {
    companion object {
        private const val GUI_SIZE = 3 * 9
        private val guiTitle = "<${Colors.MAIN.color}><configName>"

        private val keySlots = listOf(10, 11, 12, 13, 14, 15, 16)

        private var multiSite = false

        fun slotToIndex(slot: Int): Int? {
            val idx = keySlots.indexOf(slot)
            return if (idx >= 0) idx else null
        }
    }

    fun createGUI(player: Player, configName: String, config: MutableList<ConfigEntry>, saver: ((MutableList<ConfigEntry>) -> Unit)? = null) {
        val title = MessageUtils.replacer(guiTitle, mapOf("configName" to configName))
    // hide documentation entry from the UI (it's metadata), prevents off-by-one mapping
    val displayEntries = config.filter { it.key != "documentation" }.toMutableList()
    val holder = GuiHolder(configName, displayEntries, saver)
    var inv = InventoryHandler().createInventory(holder, title, GUI_SIZE)

    inv = fillInventory(inv, displayEntries)
        inv = Filler().fillInventory(inv, blockedSlots = keySlots.toMutableList())

        if (multiSite) {
            // TODO: Implement multi-site logic
        }

        player.openInventory(inv)
    }

    private fun fillInventory(
        inv: Inventory,
        entries: List<ConfigEntry>,
    ): Inventory {
        var slotIndex = 0
        for (entry in entries) {
            if (slotIndex >= keySlots.size) {
                multiSite = true
                break
            }
            val item = buildKeyItem(entry) ?: continue

            inv.setItem(keySlots[slotIndex], item)
            slotIndex++
        }
        return inv
    }
    private fun buildKeyItem(entry: ConfigEntry): ItemStack? {
        val itemMaterial = entry.invItem ?: entry.dynamicInvItem?.invoke(entry)
        
        if (itemMaterial == null) {
            return null
        }

        val item = ItemStack(itemMaterial)
        val meta = item.itemMeta

        meta.displayName(MessageUtils.convert("<${Colors.MAIN.color}>${entry.key}"))
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)

        val valueStr = when (val v = entry.value) {
            is List<*> -> {
                val list = v.take(5).joinToString("\n", "\n", "") { "- $it" }
                if (v.size > 5) "$list\n..." else list
            }
            is Map<*, *> -> {
                val entries = v.entries.take(5).joinToString("\n", "\n", "") { "- ${it.key}=${it.value}" }
                if (v.entries.size > 5) "$entries\n..." else entries
            }
            null -> entry.defaultValue?.toString() ?: "<none>"
            else -> v.toString()
        }
        val defaultStr = when (val v = entry.defaultValue) {
            is List<*> -> {
                val list = v.take(5).joinToString("\n", "\n", "") { "- $it" }
                if (v.size > 5) "$list\n..." else list
            }

            is Map<*, *> -> {
                val entries = v.entries.take(5).joinToString("\n", "\n", "") { "- ${it.key}=${it.value}" }
                if (v.entries.size > 5) "$entries\n..." else entries
            }
            null -> "<none>"
            else -> v.toString()
        }

        val actions = ConfigEntryTypes.fromEntryType(entry.type).clickActions
        val actionsLines = actions.joinToString("<newline>") { "<gray>${it.name}: <white>${it.description}" }

        val loreLines = mutableListOf<String>()
        loreLines.add("<dark_gray>————————")
        if (!entry.description.isNullOrBlank()) {
            loreLines.add("<gray>${entry.description}")
            loreLines.add("<dark_gray>————————")
        }
        loreLines.add("<white>Value: <green>$valueStr")
        loreLines.add("<white>Default: <yellow>$defaultStr")
        loreLines.add("<dark_gray> ")
        loreLines.add("<white>Actions:")
        loreLines.add(actionsLines)
        loreLines.add("<dark_gray>————————")

        meta.lore(MessageUtils.handleLore(loreLines.joinToString("<newline>")))
        item.itemMeta = meta
        return item
    }
}

