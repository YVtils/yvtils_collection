/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regionsv2.logic

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import org.bukkit.Location

/**
 * Region helpers using direct, typed calls to WorldGuard's live API. Core's plugin
 * dependency provides classpath access to WorldGuard and its WorldEdit dependency.
 */
object Regions {
    /**
     * @return the IDs of every WorldGuard region applicable to [location], or an empty list if the
     *   location's world has no region manager (e.g. region support is disabled for that world).
     */
    fun applicableRegionIds(location: Location): List<String> {
        val regionContainer = WorldGuard.getInstance().platform.regionContainer
        val regionManager = regionContainer.get(BukkitAdapter.adapt(location.world)) ?: return emptyList()

        return regionManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(location)).toList()
    }
}
