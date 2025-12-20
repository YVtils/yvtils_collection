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

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.core.ClickMode
import yv.tils.gui.core.GuiGenericHolder
import yv.tils.gui.core.GuiManager
import yv.tils.gui.logic.ConfigGUI
import yv.tils.gui.logic.GuiHolder
import yv.tils.gui.logic.ListContext
import yv.tils.gui.logic.ListGUI
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils

class InventoryClickListener : Listener {
    companion object {
        private val ENTRY_SLOTS = listOf(10, 11, 12, 13, 14, 15, 16)
    }

    @EventHandler
    fun onEvent(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        val uuid = player.uniqueId

        // Try new generic GUI system first
        val genericHolder = e.inventory.holder as? GuiGenericHolder
        if (genericHolder != null) {
            val context = GuiManager.getContext(uuid)
            if (context != null) {
                val clickMode = context.definition?.clickMode
                val isGuiClick = e.rawSlot >= 0 && e.rawSlot < e.inventory.size
                
                // Apply click mode
                when (clickMode) {
                    ClickMode.CANCEL_ALL -> {
                        e.isCancelled = true
                    }
                    ClickMode.ALLOW_PLAYER_INVENTORY -> {
                        e.isCancelled = isGuiClick
                    }
                    ClickMode.ALLOW_ALL -> {
                        e.isCancelled = false
                    }

                    else -> {
                        e.isCancelled = false
                    }
                }
                
                if (isGuiClick) {
                    GuiManager.handleClick(player, e.rawSlot, e.click)
                }
            } else {
                e.isCancelled = true
            }
            return
        }

        val holder = e.inventory.holder as? GuiHolder ?: return
        e.isCancelled = true

        val listContext = GuiListenerState.pendingList[uuid]
        if (listContext != null && listContext.inventory != null && e.inventory == listContext.inventory) {
            handleListClick(e, player, uuid, listContext)
            return
        }

        if (handleNavigation(e, player, holder)) return

        handleEntryClick(e, player, holder)
    }

    private fun handleNavigation(e: InventoryClickEvent, player: Player, holder: GuiHolder): Boolean {
        val invSize = e.inventory.size
        val navLeft = invSize - 9
        val navRight = invSize - 1
        val perPage = ENTRY_SLOTS.size
        val totalPages = ((holder.entries.size + perPage - 1) / perPage).coerceAtLeast(1)

        when (e.rawSlot) {
            navLeft -> {
                if (totalPages > 1 && holder.page > 1) {
                    holder.page--
                    reopenConfigGUI(player, holder)
                    return true
                }
            }
            navRight -> {
                if (totalPages > 1 && holder.page < totalPages) {
                    holder.page++
                    reopenConfigGUI(player, holder)
                    return true
                }
            }
        }
        return false
    }

    private fun handleEntryClick(e: InventoryClickEvent, player: Player, holder: GuiHolder) {
        val index = ENTRY_SLOTS.indexOf(e.rawSlot).takeIf { it >= 0 } ?: return
        val perPage = ENTRY_SLOTS.size
        val globalIndex = (holder.page - 1) * perPage + index
        if (globalIndex >= holder.entries.size) return

        val entry = holder.entries[globalIndex]

        when (entry.type) {
            EntryType.BOOLEAN -> if (e.click == ClickType.LEFT) {
                val current = entry.value as? Boolean ?: (entry.defaultValue as? Boolean ?: false)
                entry.value = !current
                holder.dirty = true
                reopenConfigGUI(player, holder)
            }

            EntryType.INT, EntryType.DOUBLE -> {
                val delta = if (e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT) 10 else 1
                when (e.click) {
                    ClickType.LEFT, ClickType.SHIFT_LEFT -> {
                        modifyNumericValue(entry, delta)
                        holder.dirty = true
                        reopenConfigGUI(player, holder)
                    }
                    ClickType.RIGHT, ClickType.SHIFT_RIGHT -> {
                        modifyNumericValue(entry, -delta)
                        holder.dirty = true
                        reopenConfigGUI(player, holder)
                    }
                    else -> {}
                }
            }

            EntryType.STRING -> if (e.click == ClickType.LEFT) {
                GuiListenerState.pendingChat[player.uniqueId] = holder to entry.key
                try { player.closeInventory() } catch (_: Exception) {}
                player.sendMessage(
                    LanguageHandler.getMessage(
                        "action.gui.enterValue.prompt",
                        player,
                        mapOf("key" to entry.key)
                    )
                )
            }

            EntryType.LIST, EntryType.MAP -> {
                val list = (entry.value as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                    ?: (entry.defaultValue as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                    ?: mutableListOf()

                val backCallback = {
                    val targetEntry = holder.entries.find { it.key == entry.key }
                    if (targetEntry != null) {
                        targetEntry.value = list.toList()
                        holder.dirty = true
                    }
                    Bukkit.getScheduler().runTask(Data.instance, Runnable {
                        ConfigGUI.createGUI(player, holder.configName, holder.entries, holder.onSave, holder)
                    })
                }

                GuiListenerState.pendingList[player.uniqueId] = ListContext(holder, entry.key, list, backCallback)
                ListGUI.openList(player, GuiListenerState.pendingList[player.uniqueId]!!)
            }

            else -> player.sendMessage(LanguageHandler.getMessage("action.gui.valueInfo", player,mapOf("value" to (entry.value ?: entry.defaultValue).toString())))
        }
    }

    private fun modifyNumericValue(entry: ConfigEntry, delta: Int) {
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
            else -> {}
        }
    }

    private fun reopenConfigGUI(player: Player, holder: GuiHolder) {
        ConfigGUI.createGUI(player, holder.configName, holder.entries, holder.onSave, holder)
    }

    private fun handleListClick(
        e: InventoryClickEvent,
        player: Player,
        uuid: java.util.UUID,
        listContext: ListContext
    ) {
        val raw = e.rawSlot
        val inv = e.inventory
        val invSize = inv.size
        val clicked = try {
            inv.getItem(raw)
        } catch (_: ArrayIndexOutOfBoundsException) {
            return
        }

        // Filler/gray pane acts as a 'back' button
        if (clicked?.type == Material.GRAY_STAINED_GLASS_PANE) {
            GuiListenerState.pendingList.remove(uuid)
            listContext.backCallback.invoke()
            return
        }

        val navLeft = invSize - 9
        val addSlot = invSize - 5
        val navRight = invSize - 1

        when (raw) {
            navLeft -> {
                if (listContext.totalPages > 1 && listContext.page > 1) {
                    listContext.page--
                    ListGUI.openList(player, listContext)
                } else {
                    GuiListenerState.pendingList.remove(uuid)
                    listContext.backCallback.invoke()
                }
                return
            }
            addSlot -> {
                promptAddItem(player, uuid, listContext)
                return
            }
            navRight -> {
                if (listContext.totalPages > 1 && listContext.page < listContext.totalPages) {
                    listContext.page++
                    ListGUI.openList(player, listContext)
                }
                return
            }
        }

        // Check display name-based controls
        val clickedName = clicked?.itemMeta?.displayName()?.let { MessageUtils.strip(it) } ?: ""
        if (clickedName.isNotBlank()) {
            when {
                "previous" in clickedName.lowercase() || "back" in clickedName.lowercase() -> {
                    if (listContext.page > 1) listContext.page--
                    ListGUI.openList(player, listContext)
                    return
                }
                "next" in clickedName.lowercase() -> {
                    if (listContext.page < listContext.totalPages) listContext.page++
                    ListGUI.openList(player, listContext)
                    return
                }
                "quit" in clickedName.lowercase() || "close" in clickedName.lowercase() -> {
                    try { player.closeInventory() } catch (_: Exception) {}
                    GuiListenerState.pendingList.remove(uuid)
                    listContext.backCallback.invoke()
                    return
                }
                "add" in clickedName.lowercase() || "plus" in clickedName.lowercase() -> {
                    promptAddItem(player, uuid, listContext)
                    return
                }
            }
        }

        // Handle item clicks (right removes)
        if (clicked != null) {
            val name = MessageUtils.strip(clicked.itemMeta?.displayName())
            when (e.click) {
                ClickType.RIGHT, ClickType.SHIFT_RIGHT -> listContext.items.remove(name)
                else -> {}
            }
            ListGUI.openList(player, listContext)
        }
    }

    private fun promptAddItem(player: Player, uuid: java.util.UUID, listContext: ListContext) {
        try { player.closeInventory() } catch (_: Exception) {}
        player.sendMessage(LanguageHandler.getMessage("action.gui.enterValue.promptList", player,mapOf()))
        GuiListenerState.pendingAdd[uuid] = listContext
    }
}
