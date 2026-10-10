/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.fusion.logic.crafting.Crafting
import yv.tils.fusion.logic.items.flask.NourishingFlask

class PlayerQuit : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        Crafting.forget(event.player.uniqueId)
        NourishingFlask.forget(event.player.uniqueId)
    }
}
