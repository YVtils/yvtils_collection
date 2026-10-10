/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.listeners

import yv.tils.fusion.logic.items.FusionItems

import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.persistence.PersistentDataType

class FrameListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun place(event: HangingPlaceEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val stack = event.itemStack ?: return
        if (FusionItems.kind(stack) != "INVISIBLE_FRAME") return
        frame.isVisible = false
        frame.persistentDataContainer.set(
            FusionItems.entityKey, PersistentDataType.STRING,
            FusionItems.encode(stack.clone().apply { amount = 1 })
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun breakFrame(event: HangingBreakEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val encoded = frame.persistentDataContainer.get(FusionItems.entityKey, PersistentDataType.STRING) ?: return
        val original = FusionItems.decode(encoded)
        val displayed = frame.item.clone()
        val creative = ((event as? HangingBreakByEntityEvent)?.remover as? Player)?.gameMode == GameMode.CREATIVE
        event.isCancelled = true
        val location = frame.location
        val world = frame.world
        frame.remove()
        if (!creative && world.getGameRuleValue(GameRule.DO_ENTITY_DROPS) != false) {
            world.dropItemNaturally(location, original)
            if (!displayed.type.isAir && Math.random() < frame.itemDropChance) world.dropItemNaturally(
                location,
                displayed
            )
        }
    }
}
