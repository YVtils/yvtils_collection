/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.listeners

import yv.tils.fusion.FusionYVtils
import yv.tils.fusion.logic.items.FusionItems

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import yv.tils.fusion.api.FusionRegistry

class FusionInteractionListener : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun consume(event: org.bukkit.event.player.PlayerItemConsumeEvent) {
        if (FusionItems.kind(event.item) == "NOURISHING_FLASK") event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun interact(event: PlayerInteractEvent) {
        if (!FusionYVtils.active) return
        val item = event.item ?: return
        if (FusionItems.id(item) == null) return
        val kind = FusionItems.kind(item) ?: return
        try {
            FusionRegistry.behavior(kind)?.interact(event, item)
        } catch (error: Exception) {
            yv.tils.utils.logger.Logger.error("Fusion item behavior failed: $kind", error)
        }
    }
}
