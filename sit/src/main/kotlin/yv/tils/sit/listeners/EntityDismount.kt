package yv.tils.sit.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import yv.tils.sit.logic.DismountListener

class EntityDismount : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: EntityDismountEvent) {
        DismountListener().onDismount(e)
    }
}