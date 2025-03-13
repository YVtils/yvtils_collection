package yv.tils.essentials.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import yv.tils.essentials.commands.handler.FlyHandler
import yv.tils.essentials.commands.handler.GodHandler

class EntityDamage : Listener {
    @EventHandler
    fun onEvent(e: EntityDamageEvent) {
        FlyHandler().onLandingDamage(e)
        GodHandler().onDamage(e)
    }
}