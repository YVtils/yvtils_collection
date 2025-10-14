package yv.tils.gui.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.gui.logic.ConfigGUI
import yv.tils.gui.logic.ListGUI
import yv.tils.utils.data.Data
import yv.tils.utils.message.MessageUtils

class PlayerChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(e: AsyncChatEvent) {
        val uuid = e.player.uniqueId
        // If user is in "add block" flow, handle that first
        val addCtx = GuiListenerState.pendingAdd.remove(uuid)
        if (addCtx != null) {
            val message = MessageUtils.stripChatMessage(e.message()).trim()
            if (message.equals("cancel", ignoreCase = true)) {
                e.player.sendMessage("Add cancelled.") // TODO: localize
                e.isCancelled = true
                Bukkit.getScheduler().runTask(Data.instance, Runnable {
                    ListGUI().openList(e.player, addCtx)
                })
                return
            }

            val name = message.trim().uppercase().replace(' ', '_')
            try {
                val m = Material.valueOf(name)
                addCtx.items.add(m.name)
                Bukkit.getScheduler().runTask(Data.instance, Runnable {
                    ListGUI().openList(e.player, addCtx)
                })
                e.player.sendMessage("Added $name to list.")  // TODO: localize
                // TODO: Test if sprites are a good idea for implementing 1.21.9+
            } catch (_: Exception) {
                e.player.sendMessage("Unknown material: $name. Add aborted.")  // TODO: localize
            }

            e.isCancelled = true
            return
        }

        val pending = GuiListenerState.pendingChat.remove(uuid) ?: return
        val (holder, key) = pending

        val message = MessageUtils.stripChatMessage(e.message()).trim()
        if (message.equals("cancel", ignoreCase = true)) {
            e.player.sendMessage("Edit cancelled.")  // TODO: localize
            e.isCancelled = true
            return
        }

        val entry = holder.entries.find { it.key == key } ?: return
        entry.value = message
        holder.dirty = true

        e.isCancelled = true
        Bukkit.getScheduler().runTask(Data.instance, Runnable {
            ConfigGUI().createGUI(e.player, holder.configName, holder.entries, holder.onSave)
        })
    }
}
