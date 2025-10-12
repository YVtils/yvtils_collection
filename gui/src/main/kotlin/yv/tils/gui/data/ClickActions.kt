package yv.tils.gui.data

import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

data class ClickActions(val name: String, val description: String, val click: ClickType, val action: (InventoryClickEvent) -> Unit) {
    companion object {
        val OPEN_SETTING = ClickActions("OPEN_SETTING", "Open settings menu", ClickType.RIGHT) { event ->
            // Open settings menu logic
        }

        val TOGGLE_OPTION = ClickActions("TOGGLE_OPTION", "Toggle an option", ClickType.LEFT) { event ->
            // Toggle option logic
        }

        val INCREMENT_VALUE = ClickActions("INCREMENT_VALUE", "Increment a value", ClickType.LEFT) { event ->
            // Increment value logic
        }

        val INCREMENT_VALUE_SHIFT = ClickActions("INCREMENT_VALUE_SHIFT", "Increment a value by 10 times the normal amount", ClickType.SHIFT_LEFT) { event ->
            // Increment value by 10 logic
        }

        val DECREMENT_VALUE = ClickActions("DECREMENT_VALUE", "Decrement a value", ClickType.RIGHT) { event ->
            // Decrement value logic
        }

        val DECREMENT_VALUE_SHIFT = ClickActions("DECREMENT_VALUE_SHIFT", "Decrement a value by 10 times the normal amount", ClickType.SHIFT_RIGHT) { event ->
            // Decrement value by 10 logic
        }

        val MODIFY_TEXT = ClickActions("MODIFY_TEXT", "Modify text value", ClickType.LEFT) { event ->
            // Modify text logic
        }
    }
}

class ClickLogic {
    fun compareClick(e: InventoryClickEvent, a: ClickActions): Boolean {
        return e.click == a.click
    }
}