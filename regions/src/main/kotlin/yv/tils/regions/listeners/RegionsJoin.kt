/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldLoadEvent
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.logic.ClaimFlags
import yv.tils.regions.logic.ClaimSelection
import yv.tils.regions.logic.ClaimService
import yv.tils.utils.modules.Core

/** Releases transient selections and particle tasks when a player disconnects. */
class RegionsJoin : Listener {
    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        Bukkit.getScheduler().runTask(Core.instance, Runnable {
            if (ConfigFile.state.enabled) {
                ClaimService.upgradeNames(event.world)
                ClaimService.refreshMetadata(event.world)
                if (com.sk89q.worldguard.WorldGuard.getInstance().platform.regionContainer.get(
                        com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(
                            event.world
                        )
                    ) != null
                )
                    ClaimFlags.catchUp(event.world)
                yv.tils.regions.logic.ClaimSubzones.refresh(event.world)
            }
        })
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        ClaimSelection.clear(event.player.uniqueId)
        yv.tils.regions.logic.SubzoneSelection.clear(event.player.uniqueId)
        yv.tils.regions.logic.ClaimOccupancy.forget(event.player.uniqueId)
    }
}
