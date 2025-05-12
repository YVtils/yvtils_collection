package yv.tils.regions.listeners.cause

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.regions.data.Flag
import yv.tils.regions.listeners.custom.flags.PlayerFlagTriggerEvent
import yv.tils.regions.logic.RegionLogic

class PlayerTeleport : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onEvent(e: org.bukkit.event.player.PlayerTeleportEvent) {
        val player = e.player
        val from = e.from
        val to = e.to
        val sourceRegion = RegionLogic.getRegion(from) ?: return
        val targetRegion = RegionLogic.getRegion(to) ?: return

        val flagTrigger = PlayerFlagTriggerEvent(player, null, sourceRegion, targetRegion, Flag.TELEPORT)
        flagTrigger.callEvent()
        if (flagTrigger.isCancelled) {
            e.isCancelled = true
            return
        }
    }
}