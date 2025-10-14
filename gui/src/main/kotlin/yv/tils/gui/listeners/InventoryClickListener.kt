package yv.tils.gui.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import yv.tils.config.data.EntryType
import yv.tils.gui.logic.ConfigGUI
import yv.tils.gui.logic.GuiHolder
import yv.tils.gui.logic.ListContext
import yv.tils.gui.logic.ListGUI
import yv.tils.utils.data.Data
import yv.tils.utils.message.MessageUtils

private val ENTRY_SLOTS = listOf(10, 11, 12, 13, 14, 15, 16)

class InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder
        if (holder !is GuiHolder) return

        e.isCancelled = true

        val player = e.whoClicked as Player
        val uuid = player.uniqueId

        // Handle pending list UI first
        val listContext = GuiListenerState.pendingList[uuid]
        if (listContext != null && listContext.inventory != null && e.inventory == listContext.inventory) {
            if (handleListClick(e, player, uuid, listContext)) return
            return
        }

        // Map raw slot to configured entries
        val index = ENTRY_SLOTS.indexOf(e.rawSlot).takeIf { it >= 0 } ?: return
        val entries = holder.entries
        if (index >= entries.size) return

        val entry = entries[index]

        fun reopen() {
            ConfigGUI().createGUI(player, holder.configName, holder.entries, holder.onSave)
        }

        when (entry.type) {
            EntryType.BOOLEAN -> if (e.click == ClickType.LEFT) {
                val cur = entry.value as? Boolean ?: (entry.defaultValue as? Boolean ?: false)
                entry.value = !cur
                holder.entries[holder.entries.indexOf(entry)] = entry
                holder.dirty = true
                reopen()
            }

            EntryType.INT, EntryType.DOUBLE -> {
                val isShift = e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT
                val delta = if (isShift) 10 else 1
                when (e.click) {
                    ClickType.LEFT, ClickType.SHIFT_LEFT -> {
                        if (entry.type == EntryType.INT) {
                            val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                            entry.value = cur + delta
                        } else {
                            val cur = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                            entry.value = cur + delta
                        }
                        holder.dirty = true
                        reopen()
                    }
                    ClickType.RIGHT, ClickType.SHIFT_RIGHT -> {
                        if (entry.type == EntryType.INT) {
                            val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                            entry.value = cur - delta
                        } else {
                            val cur = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                            entry.value = cur - delta
                        }
                        holder.dirty = true
                        reopen()
                    }
                    else -> {}
                }
            }

            EntryType.STRING -> if (e.click == ClickType.LEFT) {
                player.sendMessage("Please type the new value for ${entry.key} in chat. Type cancel to abort.")  // TODO: localize
                GuiListenerState.pendingChat[player.uniqueId] = holder to entry.key
                reopen()
            }

            EntryType.LIST, EntryType.MAP -> {
                val list = (entry.value as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                    ?: (entry.defaultValue as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                    ?: mutableListOf()

                val back = {
                    val targetEntry = holder.entries.find { it.key == entry.key }
                    if (targetEntry != null) {
                        targetEntry.value = list.toList()
                        holder.entries[holder.entries.indexOf(targetEntry)] = targetEntry
                        holder.dirty = true
                    }
                    Bukkit.getScheduler().runTask(Data.instance, Runnable {
                        ConfigGUI().createGUI(player, holder.configName, holder.entries, holder.onSave)
                    })
                }

                GuiListenerState.pendingList[player.uniqueId] = ListContext(holder, entry.key, list, back)
                ListGUI().openList(player, GuiListenerState.pendingList[player.uniqueId]!!)
            }

            else -> player.sendMessage("Value: ${entry.value ?: entry.defaultValue}")
        }
    }

    private fun handleListClick(e: InventoryClickEvent, player: Player, uuid: java.util.UUID, listContext: ListContext): Boolean {
        val raw = e.rawSlot
        val inv = e.inventory
        val invSize = inv.size
        val clicked = inv.getItem(raw)

        // filler/gray pane acts as a 'back' button
        if (clicked?.type == Material.GRAY_STAINED_GLASS_PANE) {
            GuiListenerState.pendingList.remove(uuid)
            listContext.backCallback.invoke()
            return true
        }

        val navLeft = invSize - 9
        val addSlot = invSize - 5
        val navRight = invSize - 1

        when (raw) {
            navLeft -> {
                if (listContext.totalPages > 1 && listContext.page > 1) {
                    listContext.page--
                    ListGUI().openList(player, listContext)
                    return true
                }
                GuiListenerState.pendingList.remove(uuid)
                listContext.backCallback.invoke()
                return true
            }
            addSlot -> {
                try { player.closeInventory() } catch (_: Exception) {}
                val prompt = "<green>Type block Material name to add (e.g. <white>OAK_LOG<green>)\n<yellow>Type <red>cancel<yellow> to abort." // TODO: localize
                player.sendMessage(MessageUtils.replacer(prompt, mapOf()))
                GuiListenerState.pendingAdd[uuid] = listContext
                return true
            }
            navRight -> {
                if (listContext.totalPages > 1) {
                    if (listContext.page < listContext.totalPages) listContext.page++
                    ListGUI().openList(player, listContext)
                    return true
                }
            }
        }

        // check display name-based controls (previous/next/quit/add)
        val clickedName = clicked?.itemMeta?.displayName()?.let { MessageUtils.strip(it) } ?: ""
        if (clickedName.isNotBlank()) {
            val lower = clickedName.lowercase()
            when {
                "previous" in lower || "back" in lower -> {
                    if (listContext.page > 1) listContext.page--
                    ListGUI().openList(player, listContext)
                    return true
                }
                "next" in lower -> {
                    if (listContext.page < listContext.totalPages) listContext.page++
                    ListGUI().openList(player, listContext)
                    return true
                }
                "quit" in lower || "close" in lower -> {
                    try { player.closeInventory() } catch (_: Exception) {}
                    GuiListenerState.pendingList.remove(uuid)
                    listContext.backCallback.invoke()
                    return true
                }
                "add" in lower || "plus" in lower -> {
                    try { player.closeInventory() } catch (_: Exception) {}
                    val prompt = "<green>Type block Material name to add (e.g. <white>OAK_LOG<green>)\n<yellow>Type <red>cancel<yellow> to abort."  // TODO: localize
                    player.sendMessage(MessageUtils.replacer(prompt, mapOf()))
                    GuiListenerState.pendingAdd[uuid] = listContext
                    return true
                }
            }
        }

        // treat item clicks (right removes)
        if (clicked == null) return false
        val name = MessageUtils.strip(clicked.itemMeta?.displayName())
        when (e.click) {
            ClickType.RIGHT, ClickType.SHIFT_RIGHT -> listContext.items.remove(name)
            else -> {}
        }
        ListGUI().openList(player, listContext)
        return true
    }
}
