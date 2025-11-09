/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.gui.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.logic.GuiHolder
import yv.tils.utils.logger.Logger

class InventoryClose : Listener {
    @EventHandler
    fun onEvent(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        val uuid = player.uniqueId
        val holder = e.inventory.holder as? GuiHolder ?: return

        // Check if this is a list GUI being closed
        val listContext = GuiListenerState.pendingList[uuid]
        if (listContext != null && listContext.inventory == e.inventory) {
            handleListClose(player, uuid, listContext)
            return
        }

        // Handle main config GUI close
        if (holder.dirty) {
            saveConfig(player, holder)
        }
    }

    private fun handleListClose(player: Player, uuid: java.util.UUID, listContext: yv.tils.gui.logic.ListContext) {
        Logger.debug("InventoryCloseListener: list GUI closed directly, saving changes", 1)
        GuiListenerState.pendingList.remove(uuid)

        // Write list changes back to the entry
        val targetEntry = listContext.holder.entries.find { it.key == listContext.entryKey }
        if (targetEntry != null) {
            targetEntry.value = listContext.items.toList()
            listContext.holder.dirty = true
            saveConfig(player, listContext.holder)
        } else {
            Logger.debug("InventoryCloseListener: could not find targetEntry for key ${listContext.entryKey}", 1)
        }
    }

    private fun saveConfig(player: Player, holder: GuiHolder) {
        try {
            holder.onSave?.invoke(holder.entries)
            Logger.info("Config saved for ${holder.configName}")
            player.sendMessage(LanguageHandler.getMessage("action.gui.configSaved", player,mapOf("config" to holder.configName)))
        } catch (ex: Exception) {
            player.sendMessage(LanguageHandler.getMessage("action.gui.configSaveFailed", player,mapOf("config" to holder.configName, "error" to (ex.message ?: "Unknown error"))))
            Logger.error("Failed to save config for ${holder.configName}: ${ex.message}")
        }
    }
}
