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

package yv.tils.vanish.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.utils.modules.Core
import yv.tils.vanish.configs.ConfigFile
import yv.tils.vanish.logic.VanishService
import yv.tils.vanish.permissions.VanishPermissions

class VanishListener(private val service: VanishService) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun join(event: PlayerJoinEvent) = service.refresh()

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        if (service.hidden(event.player.uniqueId)) event.quitMessage(null)
        // Keep hidden status through other modules' connection-message handlers.
        Bukkit.getScheduler()
            .runTask(Core.instance, Runnable { service.quit(event.player) })
    }

    @EventHandler(ignoreCancelled = true)
    fun pickup(event: EntityPickupItemEvent) {
        if (service.capability(
                event.entity.uniqueId,
                VanishPermissions.PICKUP,
                ConfigFile.state.preventPickup
            )
        ) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun target(event: EntityTargetLivingEntityEvent) {
        if (service.capability(
                event.target?.uniqueId,
                VanishPermissions.MOB_TARGET,
                ConfigFile.state.preventMobTarget
            )
        ) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun drop(event: PlayerDropItemEvent) {
        if (service.capability(
                event.player.uniqueId,
                VanishPermissions.DROP,
                ConfigFile.state.preventDrop
            )
        ) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun damage(event: EntityDamageByEntityEvent) {
        if (service.capability(event.damager.uniqueId, VanishPermissions.DAMAGE, ConfigFile.state.preventDamage) ||
            service.capability(event.entity.uniqueId, VanishPermissions.DAMAGE, ConfigFile.state.preventDamage)
        ) event.isCancelled = true
    }
}
