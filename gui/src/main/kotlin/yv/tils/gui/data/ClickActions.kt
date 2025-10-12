package yv.tils.gui.data

import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

data class ClickActions(val name: String, val description: String, val click: ClickType, val action: (InventoryClickEvent) -> Unit) {
    companion object {
        val OPEN_SETTING = ClickActions("OPEN_SETTING", "Open settings menu", ClickType.RIGHT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            player.sendMessage("Setting ${entry.key}: ${entry.value ?: entry.defaultValue}")
        }

        val TOGGLE_OPTION = ClickActions("TOGGLE_OPTION", "Toggle an option", ClickType.LEFT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            if (entry.type != yv.tils.config.data.EntryType.BOOLEAN) return@ClickActions
            val cur = entry.value as? Boolean ?: (entry.defaultValue as? Boolean ?: false)
            entry.value = !cur
            // refresh GUI
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            yv.tils.gui.logic.ConfigGUI().createGUI(player, holder.configName, holder.entries)
        }

        val INCREMENT_VALUE = ClickActions("INCREMENT_VALUE", "Increment a value", ClickType.LEFT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            when (entry.type) {
                yv.tils.config.data.EntryType.INT -> {
                    val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                    entry.value = cur + 1
                }
                yv.tils.config.data.EntryType.DOUBLE -> {
                    val cur = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                    entry.value = cur + 1.0
                }
                else -> return@ClickActions
            }
            yv.tils.gui.logic.ConfigGUI().createGUI(player, holder.configName, holder.entries)
        }

        val INCREMENT_VALUE_SHIFT = ClickActions("INCREMENT_VALUE_SHIFT", "Increment a value by 10 times the normal amount", ClickType.SHIFT_LEFT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            when (entry.type) {
                yv.tils.config.data.EntryType.INT -> {
                    val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                    entry.value = cur + 10
                }
                yv.tils.config.data.EntryType.DOUBLE -> {
                    val cur = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                    entry.value = cur + 10.0
                }
                else -> return@ClickActions
            }
            yv.tils.gui.logic.ConfigGUI().createGUI(player, holder.configName, holder.entries)
        }

        val DECREMENT_VALUE = ClickActions("DECREMENT_VALUE", "Decrement a value", ClickType.RIGHT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            when (entry.type) {
                yv.tils.config.data.EntryType.INT -> {
                    val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                    entry.value = cur - 1
                }
                yv.tils.config.data.EntryType.DOUBLE -> {
                    val cur = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                    entry.value = cur - 1.0
                }
                else -> return@ClickActions
            }
            yv.tils.gui.logic.ConfigGUI().createGUI(player, holder.configName, holder.entries)
        }

        val DECREMENT_VALUE_SHIFT = ClickActions("DECREMENT_VALUE_SHIFT", "Decrement a value by 10 times the normal amount", ClickType.SHIFT_RIGHT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            when (entry.type) {
                yv.tils.config.data.EntryType.INT -> {
                    val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                    entry.value = cur - 10
                }
                yv.tils.config.data.EntryType.DOUBLE -> {
                    val cur = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                    entry.value = cur - 10.0
                }
                else -> return@ClickActions
            }
            yv.tils.gui.logic.ConfigGUI().createGUI(player, holder.configName, holder.entries)
        }

        val MODIFY_TEXT = ClickActions("MODIFY_TEXT", "Modify text value", ClickType.LEFT) { event ->
            val holder = event.clickedInventory?.holder as? yv.tils.gui.logic.GuiHolder ?: return@ClickActions
            val index = yv.tils.gui.logic.ConfigGUI.slotToIndex(event.rawSlot) ?: return@ClickActions
            val entry = holder.entries.getOrNull(index) ?: return@ClickActions
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return@ClickActions
            player.sendMessage("Type new value for ${entry.key} in chat (not implemented). Current: ${entry.value ?: entry.defaultValue}")
        }
    }
}

class ClickLogic {
    fun compareClick(e: InventoryClickEvent, a: ClickActions): Boolean {
        return e.click == a.click
    }
}