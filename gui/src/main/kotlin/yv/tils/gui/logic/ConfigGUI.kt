package yv.tils.gui.logic

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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
    }

    fun createGUI(player: Player, configName: String, config: MutableMap<String, Any>) {
        val entries = mutableListOf<ConfigEntry>()
        for ((key, value) in config) {
            val type = when (value) {
                is Int -> ConfigEntryTypes.INT
                is Double -> ConfigEntryTypes.DOUBLE
                is Boolean -> ConfigEntryTypes.BOOLEAN
                is String -> ConfigEntryTypes.STRING
                else -> ConfigEntryTypes.UNKNOWN
            }
            entries.add(ConfigEntry(key, value, type, Material.PAPER))
        }

        var inv = InventoryHandler().createInventory(
            MessageUtils.replacer(
                guiTitle,
                mapOf("configName" to configName)
            ),
            GUI_SIZE
        )

        inv = fillInventory(inv, entries)
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
        for (i in entries.indices) {
            if (i >= keySlots.size) {
                multiSite = true
                break
            }
            val entry = entries[i]
            inv.setItem(keySlots[i], buildKeyItem(entry))
        }
        return inv
    }

    private fun buildKeyItem(entry: ConfigEntry): ItemStack {
        val item = ItemStack(entry.item)
        // TODO: Implement logic
        return item
    }

    data class ConfigEntry(
        val key: String,
        val value: Any,
        val type: ConfigEntryTypes,
        val item: Material,
    )
}

