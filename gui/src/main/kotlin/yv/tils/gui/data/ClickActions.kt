/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.gui.data

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.logic.ConfigGUI
import yv.tils.gui.logic.GuiHolder

data class ClickActions(
    val nameKey: String,
    val descriptionKey: String,
    val click: ClickType,
    val action: (InventoryClickEvent) -> Unit
) {
    fun getName(): String {
        return LanguageHandler.getCleanMessage(nameKey)
    }

    fun getDescription(): String {
        return LanguageHandler.getCleanMessage(descriptionKey)
    }

    companion object {
        val OPEN_SETTING = ClickActions("action.gui.click.name.openSetting", "action.gui.click.openSetting", ClickType.RIGHT) { event ->
            getEventContext(event)?.let { (player, entry) ->
                player.sendMessage(LanguageHandler.getMessage("action.gui.settingInfo", player,mapOf("key" to entry.key, "value" to (entry.value ?: entry.defaultValue).toString())))
            }
        }

        val TOGGLE_OPTION = ClickActions("action.gui.click.name.toggleOption", "action.gui.click.toggleOption", ClickType.LEFT) { event ->
            getEventContext(event)?.let { (player, entry, holder) ->
                if (entry.type != EntryType.BOOLEAN) return@ClickActions
                val current = entry.value as? Boolean ?: (entry.defaultValue as? Boolean ?: false)
                entry.value = !current
                ConfigGUI.createGUI(player, holder.configName, holder.entries, null, holder)
            }
        }

        val INCREMENT_VALUE = ClickActions("action.gui.click.name.incrementValue", "action.gui.click.incrementValue", ClickType.LEFT) { event ->
            modifyNumericValue(event, 1)
        }

        val INCREMENT_VALUE_SHIFT = ClickActions("action.gui.click.name.incrementValueShift", "action.gui.click.incrementValueShift", ClickType.SHIFT_LEFT) { event ->
            modifyNumericValue(event, 10)
        }

        val DECREMENT_VALUE = ClickActions("action.gui.click.name.decrementValue", "action.gui.click.decrementValue", ClickType.RIGHT) { event ->
            modifyNumericValue(event, -1)
        }

        val DECREMENT_VALUE_SHIFT = ClickActions("action.gui.click.name.decrementValueShift", "action.gui.click.decrementValueShift", ClickType.SHIFT_RIGHT) { event ->
            modifyNumericValue(event, -10)
        }

        val MODIFY_TEXT = ClickActions("action.gui.click.name.modifyText", "action.gui.click.modifyText", ClickType.LEFT) { event ->
            getEventContext(event)?.let { (player, entry) ->
                player.sendMessage(LanguageHandler.getMessage("action.gui.enterValue.prompt", player, mapOf("key" to entry.key)))
            }
        }

        // Helper functions
        private data class EventContext(val player: Player, val entry: ConfigEntry, val holder: GuiHolder)

        private fun getEventContext(event: InventoryClickEvent): EventContext? {
            val holder = event.clickedInventory?.holder as? GuiHolder ?: return null
            val index = ConfigGUI.slotToIndex(event.rawSlot) ?: return null
            val entry = holder.entries.getOrNull(index) ?: return null
            val player = event.whoClicked as? Player ?: return null
            return EventContext(player, entry, holder)
        }

        private fun modifyNumericValue(event: InventoryClickEvent, delta: Int) {
            getEventContext(event)?.let { (player, entry, holder) ->
                when (entry.type) {
                    EntryType.INT -> {
                        val current = (entry.value as? Number)?.toInt()
                            ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                        entry.value = current + delta
                    }
                    EntryType.DOUBLE -> {
                        val current = (entry.value as? Number)?.toDouble()
                            ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                        entry.value = current + delta.toDouble()
                    }
                    else -> return@let
                }
                ConfigGUI.createGUI(player, holder.configName, holder.entries, null, holder)
            }
        }
    }
}