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

package yv.tils.regions.logic

import yv.tils.regions.configs.RegionsConfigState
import java.util.*

/** Pure prospective cluster calculation. Credits stay attached to claims, never copied to neighbours. */
object ClaimClusters {
    data class Footprint(
        val id: UUID,
        val world: UUID,
        val owners: Set<UUID>,
        val minX: Int, val minZ: Int, val maxX: Int, val maxZ: Int,
        val credit: Long = 0
    )

    data class Quote(val price: Long, val credit: Long, val due: Long, val claims: Set<UUID>)

    fun connected(a: Footprint, b: Footprint, distance: Int): Boolean {
        require(distance >= 0)
        fun gap(aMin: Int, aMax: Int, bMin: Int, bMax: Int) =
            maxOf(0L, aMin.toLong() - bMax - 1, bMin.toLong() - aMax - 1)
        return a.world == b.world && a.owners.any { it in b.owners } &&
                gap(a.minX, a.maxX, b.minX, b.maxX) <= distance &&
                gap(a.minZ, a.maxZ, b.minZ, b.maxZ) <= distance
    }

    fun quote(candidate: Footprint, existing: List<Footprint>, config: RegionsConfigState): Quote {
        val cluster = mutableListOf(candidate)
        if (config.clusterPricingEnabled) {
            val remaining = existing.filter { it.id != candidate.id && it.world == candidate.world }.toMutableList()
            var index = 0
            while (index < cluster.size) {
                val neighbours = remaining.filter { connected(cluster[index], it, config.clusterDistanceBlocks) }
                cluster.addAll(neighbours)
                remaining.removeAll(neighbours.toSet())
                index++
            }
        }
        val width = cluster.maxOf { it.maxX }.toLong() - cluster.minOf { it.minX } + 1
        val depth = cluster.maxOf { it.maxZ }.toLong() - cluster.minOf { it.minZ } + 1
        val price = ClaimPricing.price(width, depth, config)
        val credit = cluster.fold(0L) { total, claim ->
            require(claim.credit >= 0)
            Math.addExact(total, claim.credit)
        }
        return Quote(price, credit, ClaimPricing.due(price, credit), cluster.map { it.id }.toSet())
    }
}
