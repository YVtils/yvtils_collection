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

package yv.tils.regionsv2.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.regionsv2.logic.ClaimSelection
import yv.tils.regionsv2.logic.ClaimService
import yv.tils.regionsv2.logic.ClaimFlags
import yv.tils.regionsv2.configs.ConfigFile
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.Bukkit
import yv.tils.utils.modules.Core

/** Releases transient selections and particle tasks when a player disconnects. */
class RegionsV2Join : Listener {
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
            }
        })
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        ClaimSelection.clear(event.player.uniqueId)
        yv.tils.regionsv2.logic.ClaimOccupancy.forget(event.player.uniqueId)
    }
}
