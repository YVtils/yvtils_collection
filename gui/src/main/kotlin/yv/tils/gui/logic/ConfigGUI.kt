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
    }

    fun createGUI(player: Player, configName: String, config: MutableList<ConfigEntry>) {
        var inv = InventoryHandler().createInventory(
            MessageUtils.replacer(
                guiTitle,
                mapOf("configName" to configName)
            ),
            GUI_SIZE
        )

        inv = fillInventory(inv, config)
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
        // TODO: Implement logic
        return item
    }
}

