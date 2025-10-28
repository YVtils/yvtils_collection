package yv.tils.gui.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import yv.tils.gui.logic.GuiHolder
import yv.tils.utils.logger.Logger

class InventoryClose : Listener {
    @EventHandler
    fun onEvent(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        val uuid = player.uniqueId

        Logger.dev("InvClose - 1")

        val holder = e.inventory.holder as? GuiHolder ?: return

        Logger.dev("InvClose - 2")


        // Check if this is a list GUI being closed
        val listContext = GuiListenerState.pendingList[uuid]
        if (listContext != null && listContext.inventory == e.inventory) {
            Logger.dev("InvClose - 3.1")
            // User closed list GUI without clicking back - save changes directly
            Logger.debug("InventoryCloseListener: list GUI closed directly, saving changes", 1)
            GuiListenerState.pendingList.remove(uuid)

            // Write list changes back to the entry
            val targetEntry = listContext.holder.entries.find { it.key == listContext.entryKey }
            if (targetEntry != null) {
                Logger.dev("Found targetEntry for key: ${listContext.entryKey}")
                Logger.dev("Items in list: ${listContext.items}")
                targetEntry.value = listContext.items.toList()
                Logger.dev("Updated targetEntry.value = ${targetEntry.value}")
                listContext.holder.entries[listContext.holder.entries.indexOf(targetEntry)] = targetEntry
                listContext.holder.dirty = true
                
                Logger.dev("List changes written back, holder.dirty = ${listContext.holder.dirty}")
                
                // Save the config immediately
                try {
                    Logger.dev("About to invoke onSave with ${listContext.holder.entries.size} entries")
                    listContext.holder.onSave?.invoke(listContext.holder.entries)
                    Logger.info("Config saved for ${listContext.holder.configName}")
                    player.sendMessage("Config saved for ${listContext.holder.configName}.")
                } catch (ex: Exception) {
                    player.sendMessage("Failed to save config for ${listContext.holder.configName}: ${ex.message}")
                    Logger.error("Failed to save config for ${listContext.holder.configName}: ${ex.message}")
                    ex.printStackTrace()
                }
            } else {
                Logger.dev("ERROR: Could not find targetEntry for key: ${listContext.entryKey}")
            }
            return
        }

        Logger.dev("InvClose - 3.2")

        // Handle main config GUI close
        if (!holder.dirty) return

        Logger.dev("InvClose - 4")

        try {
            holder.onSave?.invoke(holder.entries)
            Logger.info("Config saved for ${holder.configName}")
            player.sendMessage("Config saved for ${holder.configName}.")  // TODO: localize
        } catch (ex: Exception) {
            player.sendMessage("Failed to save config for ${holder.configName}: ${ex.message}") // TODO: localize
            Logger.error("Failed to save config for ${holder.configName}: ${ex.message}")
        }
    }
}
