package yv.tils.regions.listeners.cause

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.PlayerFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class EntityDamageByEntity: Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: EntityDamageByEntityEvent) {
        val player = e.damager
        val target = e.entity

        if (player !is Player) return
        if (target !is Player) {
            // TODO: PvE damage -> example: player hits villager
            return
        }

        val region = RegionLogic.getRegion(player.location)
        val targetRegion = RegionLogic.getRegion(target.location)

        if (region == null && targetRegion == null) return

        val flagTrigger = PlayerFlagTriggerEvent(player, target, region, targetRegion, Flag.PVP)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
        }
    }
}