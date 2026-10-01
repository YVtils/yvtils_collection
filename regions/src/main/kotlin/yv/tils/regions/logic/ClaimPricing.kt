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

/** Price by footprint dimensions, independent of chunk alignment and world height. */
object ClaimPricing {
    fun price(x: Long, z: Long, config: RegionsConfigState): Long {
        require(x > 0 && z > 0 && config.freeClaimChunks >= 0 && config.diamondsPerChunk >= 0)
        val chunks = (maxOf(x, z) - 1) / 16 + 1
        return Math.multiplyExact((chunks - config.freeClaimChunks).coerceAtLeast(0), config.diamondsPerChunk.toLong())
    }

    fun due(price: Long, credit: Long): Long = (price - credit).coerceAtLeast(0)
}
