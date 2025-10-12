package yv.tils.gui.logic

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.EventPriority
import yv.tils.config.data.EntryType

class GuiEventListener : Listener {
    private val pendingChat = mutableMapOf<java.util.UUID, Pair<GuiHolder, String>>()
    private val pendingList = mutableMapOf<java.util.UUID, ListContext>()
    private val pendingAdd = mutableSetOf<java.util.UUID>()

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder
        if (holder !is GuiHolder) return

        e.isCancelled = true

        // if this player has a pending list context and the clicked inventory is the list
        val player = e.whoClicked as? org.bukkit.entity.Player ?: return
        val uuid = player.uniqueId
        val listContext = pendingList[uuid]
        if (listContext != null && listContext.inventory != null && e.inventory == listContext.inventory) {
            // treat this as list GUI click
            e.isCancelled = true
            val raw = e.rawSlot
            val invSize = e.inventory.size
            // back button assumed at bottom-left
            if (raw == invSize - 9) {
                // back
                pendingList.remove(uuid)
                listContext.backCallback.invoke()
                return
            }
            // if clicking a placeholder (filler) act as Back
            val maybe = e.inventory.getItem(raw)
            if (maybe != null && maybe.type == org.bukkit.Material.GRAY_STAINED_GLASS_PANE) {
                pendingList.remove(uuid)
                listContext.backCallback.invoke()
                return
            }

            // add/prev/next handling
            if (raw == invSize - 9 && listContext.totalPages > 1) {
                // prev page
                if (listContext.page > 1) listContext.page = listContext.page - 1
                ListGUI().openList(player, listContext)
                return
            }

            if (raw == invSize - 1) {
                if (listContext.totalPages > 1) {
                    // next page
                    if (listContext.page < listContext.totalPages) listContext.page = listContext.page + 1
                    ListGUI().openList(player, listContext)
                    return
                } else {
                    // single page: act as Add
                    player.sendMessage("Type block Material name to add (e.g. OAK_LOG). Type cancel to abort.")
                    pendingAdd.add(uuid)
                    return
                }
            }

            // handle click types on items
            val clicked = e.inventory.getItem(raw) ?: return
            val name = yv.tils.utils.message.MessageUtils.strip(clicked.itemMeta?.displayName)
            when (e.click) {
                ClickType.LEFT, ClickType.SHIFT_LEFT -> {
                    // toggle (if present remove, else add)
                    if (listContext.items.contains(name)) listContext.items.remove(name) else listContext.items.add(name)
                }
                ClickType.RIGHT, ClickType.SHIFT_RIGHT -> {
                    // explicit remove
                    listContext.items.remove(name)
                }
                else -> {
                    // unknown click - ignore
                }
            }
            // reopen to refresh
            ListGUI().openList(player, listContext)
            return
        }

        // map slots to entries by order
        val index = when (val slot = e.rawSlot) {
            in listOf(10, 11, 12, 13, 14, 15, 16) -> listOf(10, 11, 12, 13, 14, 15, 16).indexOf(slot)
            else -> -1
        }
        if (index < 0) return

    val entries = holder.entries
    if (index >= entries.size) return

    val entry = entries[index]
        // determine action by click type and entry type
        val entryType = entry.type

        fun reopen() {
            // reopen GUI to refresh
            ConfigGUI().createGUI(player, holder.configName, holder.entries)
        }

        when (entryType) {
            EntryType.BOOLEAN -> {
                // toggle on left click
                if (e.click == ClickType.LEFT) {
                    val cur = entry.value as? Boolean ?: (entry.defaultValue as? Boolean ?: false)
                    entry.value = !cur
                    holder.entries[holder.entries.indexOf(entry)] = entry
                    holder.dirty = true
                    reopen()
                }
            }

            EntryType.INT, EntryType.DOUBLE -> {
                val isShift =
                    e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT
                val delta = if (isShift) 10 else 1
                if (e.click == ClickType.LEFT || e.click == ClickType.SHIFT_LEFT) {
                    // increment
                    if (entry.type == EntryType.INT) {
                        val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                        entry.value = cur + delta
                        holder.dirty = true
                    } else {
                        val cur =
                            (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                        entry.value = cur + delta
                        holder.dirty = true
                    }
                    reopen()
                } else if (e.click == ClickType.RIGHT || e.click == ClickType.SHIFT_RIGHT) {
                    // decrement
                    if (entry.type == EntryType.INT) {
                        val cur = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                        entry.value = cur - delta
                        holder.dirty = true
                    } else {
                        val cur =
                            (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                        entry.value = cur - delta
                        holder.dirty = true
                    }
                    reopen()
                }
            }

            EntryType.STRING -> {
                // left click => prompt player to type new value in chat (simple prompt)
                if (e.click == ClickType.LEFT) {
                    player.sendMessage("Please type the new value for ${entry.key} in chat. Type cancel to abort.")
                    // start chat capture
                    pendingChat[player.uniqueId] = holder to entry.key
                    // store placeholder so UI shows change immediately if desired
                    // entry.value remains until chat input
                    reopen()
                }
            }

            EntryType.LIST, EntryType.MAP -> {
                // open sub-setting: for lists, show a list GUI
                val list = (entry.value as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                    ?: (entry.defaultValue as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                    ?: mutableListOf()

                val back = {
                    // write list back into entry and mark dirty, then reopen main GUI
                    val targetEntry = holder.entries.find { it.key == entry.key }
                    if (targetEntry != null) {
                        targetEntry.value = list.toList()
                        holder.entries[holder.entries.indexOf(targetEntry)] = targetEntry
                        holder.dirty = true
                    }
                    org.bukkit.Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                        ConfigGUI().createGUI(player, holder.configName, holder.entries, holder.onSave)
                    })
                }

                pendingList[player.uniqueId] = ListContext(holder, entry.key, list, back)
                ListGUI().openList(player, pendingList[player.uniqueId]!!)
            }

            else -> {
                // unknown types: show value
                player.sendMessage("Value: ${entry.value ?: entry.defaultValue}")
            }
        }
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val holder = e.inventory.holder as? GuiHolder ?: return
        if (!holder.dirty) return

        // debug
        yv.tils.utils.logger.Logger.debug("GuiEventListener: closing inventory for ${holder.configName}, dirty=${holder.dirty}", 1)

        // persist using provided saver if available
        try {
            holder.onSave?.invoke(holder.entries)
            yv.tils.utils.logger.Logger.info("Config saved for ${holder.configName}")
            val player = e.player
            player.sendMessage("Config saved for ${holder.configName}.")
        } catch (ex: Exception) {
            yv.tils.utils.logger.Logger.error("Failed to save config for ${holder.configName}: ${ex.message}")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(e: AsyncPlayerChatEvent) {
        val uuid = e.player.uniqueId
        // If user is in "add block" flow, handle that first
        if (pendingAdd.remove(uuid)) {
            val ctx = pendingList[uuid]
            if (ctx == null) {
                e.player.sendMessage("No list open to add to.")
                e.isCancelled = true
                return
            }

            val message = e.message
            if (message.equals("cancel", ignoreCase = true)) {
                e.player.sendMessage("Add cancelled.")
                e.isCancelled = true
                return
            }

            val name = message.trim().uppercase().replace(' ', '_')
            try {
                val m = org.bukkit.Material.valueOf(name)
                // add to list and reopen GUI on main thread
                ctx.items.add(m.name)
                org.bukkit.Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                    ListGUI().openList(e.player, ctx)
                })
                e.player.sendMessage("Added $name to list.")
            } catch (ex: Exception) {
                e.player.sendMessage("Unknown material: $name. Add aborted.")
            }

            e.isCancelled = true
            return
        }

        val pending = pendingChat.remove(uuid) ?: return
        val (holder, key) = pending

        val message = e.message
        if (message.equals("cancel", ignoreCase = true)) {
            e.player.sendMessage("Edit cancelled.")
            e.isCancelled = true
            return
        }

        // find entry and set value
        val entry = holder.entries.find { it.key == key } ?: return
        entry.value = message
        holder.dirty = true

        // cancel original chat event so it doesn't appear globally
        e.isCancelled = true
        // reopen GUI sync on main thread
        org.bukkit.Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
            ConfigGUI().createGUI(e.player, holder.configName, holder.entries, holder.onSave)
        })
    }
}
