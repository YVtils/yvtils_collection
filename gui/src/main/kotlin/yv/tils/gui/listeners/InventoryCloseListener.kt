package yv.tils.gui.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import yv.tils.gui.logic.ConfigGUI
import yv.tils.gui.logic.GuiHolder
import yv.tils.utils.logger.Logger

class InventoryCloseListener : Listener {
    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val holder = e.inventory.holder as? GuiHolder ?: return
        if (!holder.dirty) return

        Logger.debug("InventoryCloseListener: closing inventory for ${holder.configName}, dirty=${holder.dirty}", 1)

        try {
            holder.onSave?.invoke(holder.entries)
            Logger.info("Config saved for ${holder.configName}")
            val player = e.player
            player.sendMessage("Config saved for ${holder.configName}.")  // TODO: localize
        } catch (ex: Exception) {
            e.player.sendMessage("Failed to save config for ${holder.configName}: ${ex.message}") // TODO: localize
            Logger.error("Failed to save config for ${holder.configName}: ${ex.message}")
        }
    }
}
