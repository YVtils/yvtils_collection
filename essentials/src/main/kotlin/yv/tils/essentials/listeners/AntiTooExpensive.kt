/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.essentials.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.inventory.view.AnvilView
import yv.tils.essentials.config.ConfigFile
import yv.tils.configv2.language.LanguageHandler
import yv.tils.essentials.language.LangStrings
import yv.tils.utils.modules.Core
import java.util.UUID

/** Set the limit before calculation, including the first operation in a newly opened anvil. */
class AntiTooExpensive : Listener {
    private val lastCosts = mutableMapOf<UUID, Int>()
    private val pending = mutableSetOf<UUID>()

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onOpen(event: InventoryOpenEvent) {
        if (!ConfigFile.state.disableTooExpensive) return
        (event.view as? AnvilView)?.maximumRepairCost = Int.MAX_VALUE
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepare(event: PrepareAnvilEvent) {
        if (ConfigFile.state.disableTooExpensive) event.view.maximumRepairCost = Int.MAX_VALUE
        val player = event.view.player as? Player ?: return
        if (!pending.add(player.uniqueId)) return
        // Paper finalizes the cost/result after PrepareAnvilEvent; collapse multiple updates per tick.
        Bukkit.getScheduler().runTask(Core.instance, Runnable {
            pending.remove(player.uniqueId)
            val view = player.openInventory as? AnvilView
            if (view?.topInventory != event.inventory) return@Runnable
            val cost = view.repairCost
            if (!ConfigFile.state.disableTooExpensive || !ConfigFile.state.showAnvilCost ||
                player.gameMode == GameMode.CREATIVE || cost < 40 || view.topInventory.getItem(2) == null
            ) {
                lastCosts.remove(player.uniqueId)
                return@Runnable
            }
            if (lastCosts.put(player.uniqueId, cost) != cost) player.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.ANVIL_COST, player,
                    mapOf("cost" to cost, "levels" to player.level)
                )
            )
        })
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        lastCosts.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        lastCosts.remove(event.player.uniqueId); pending.remove(event.player.uniqueId)
    }
}
