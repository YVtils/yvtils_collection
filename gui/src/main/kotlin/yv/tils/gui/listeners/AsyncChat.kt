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

package yv.tils.gui.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.logic.ConfigGUI
import yv.tils.gui.logic.ListContext
import yv.tils.gui.logic.ListGUI
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils

class AsyncChat : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEvent(e: AsyncChatEvent) {
        val uuid = e.player.uniqueId
        
        // Check for "add block" flow first
        val addCtx = GuiListenerState.pendingAdd.remove(uuid)
        if (addCtx != null) {
            handleAddBlock(e, addCtx)
            return
        }

        // Check for text edit flow
        val pending = GuiListenerState.pendingChat.remove(uuid) ?: return
        handleTextEdit(e, pending)
    }

    private fun handleAddBlock(e: AsyncChatEvent, addCtx: ListContext) {
        val message = MessageUtils.stripChatMessage(e.message()).trim()
        
        if (message.equals("cancel", ignoreCase = true)) {
            e.player.sendMessage(LanguageHandler.getMessage("action.gui.cancelled", e.player))
            e.isCancelled = true
            reopenListGUI(e.player, addCtx)
            return
        }

        val name = message.uppercase().replace(' ', '_')
        try {
            Material.valueOf(name)
            addCtx.items.add(name)
            reopenListGUI(e.player, addCtx)
            e.player.sendMessage(LanguageHandler.getMessage("action.gui.itemAdded", e.player, mapOf("item" to name)))
            Logger.debug("Player ${e.player.name} added item $name to list", 2)
        } catch (ex: IllegalArgumentException) {
            e.player.sendMessage(LanguageHandler.getMessage("action.gui.invalidItem", e.player,mapOf("item" to name)))
            Logger.debug("Player ${e.player.name} tried to add invalid material: $name", 2)
        }

        e.isCancelled = true
    }

    private fun handleTextEdit(e: AsyncChatEvent, pending: Pair<yv.tils.gui.logic.GuiHolder, String>) {
        val (holder, key) = pending
        val message = MessageUtils.stripChatMessage(e.message()).trim()

        if (message.equals("cancel", ignoreCase = true)) {
            e.player.sendMessage(LanguageHandler.getMessage("action.gui.cancelled", e.player))
            e.isCancelled = true
            Logger.debug("Player ${e.player.name} cancelled text edit", 2)
            // Reopen the GUI when cancelled
            Bukkit.getScheduler().runTask(Data.instance, Runnable {
                ConfigGUI.createGUI(e.player, holder.configName, holder.entries, holder.onSave, holder)
            })
            return
        }

        val entry = holder.entries.find { it.key == key } ?: return
        entry.value = message
        holder.dirty = true
        Logger.debug("Player ${e.player.name} updated config entry $key to: $message", 2)

        e.isCancelled = true
        Bukkit.getScheduler().runTask(Data.instance, Runnable {
            ConfigGUI.createGUI(e.player, holder.configName, holder.entries, holder.onSave, holder)
        })
    }

    private fun reopenListGUI(player: org.bukkit.entity.Player, context: ListContext) {
        Bukkit.getScheduler().runTask(Data.instance, Runnable {
            ListGUI.openList(player, context)
            // Re-register the context so clicks work
            GuiListenerState.pendingList[player.uniqueId] = context
        })
    }
}
