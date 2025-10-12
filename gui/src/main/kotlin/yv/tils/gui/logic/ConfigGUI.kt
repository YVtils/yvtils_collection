package yv.tils.gui.logic

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import yv.tils.config.data.ConfigEntry
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

        // Display name: key in main color
        meta?.displayName(MessageUtils.convert("<${Colors.MAIN.color}>${entry.key}"))

        val valueStr = entry.value?.toString() ?: entry.defaultValue?.toString() ?: "<none>"
        val defaultStr = entry.defaultValue?.toString() ?: "<none>"

        val actions = yv.tils.gui.data.ConfigEntryTypes.fromEntryType(entry.type).clickActions
        val actionsLines = actions.joinToString("<newline>") { "<gray>${it.name}: <white>${it.description}" }

        val loreLines = mutableListOf<String>()
        loreLines.add("<dark_gray>————————")
        loreLines.add("<white>Value: <green>$valueStr")
        loreLines.add("<white>Default: <yellow>$defaultStr")
        entry.description?.let { loreLines.add("<gray>$it") }
        loreLines.add("<dark_gray> ")
        loreLines.add("<white>Actions:")
        loreLines.add(actionsLines)
        loreLines.add("<dark_gray>————————")

        meta?.lore(MessageUtils.handleLore(loreLines.joinToString("<newline>")))
        item.itemMeta = meta
        return item
    }
}

