package yv.tils.regionsv2.logic

import yv.tils.regionsv2.configs.RegionsV2ConfigState

/** Price by footprint dimensions, independent of chunk alignment and world height. */
object ClaimPricing {
    fun price(x: Long, z: Long, config: RegionsV2ConfigState): Long {
        require(x > 0 && z > 0 && config.freeClaimChunks >= 0 && config.diamondsPerChunk >= 0)
        val chunks = (maxOf(x, z) - 1) / 16 + 1
        return Math.multiplyExact((chunks - config.freeClaimChunks).coerceAtLeast(0), config.diamondsPerChunk.toLong())
    }

    fun due(price: Long, credit: Long): Long = (price - credit).coerceAtLeast(0)
}
