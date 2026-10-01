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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.regions.configs.RegionsConfigState
import java.util.*

class ClaimClustersTest {
    private val world = UUID.randomUUID()
    private val owner = UUID.randomUUID()
    private val config = RegionsConfigState()
    private fun claim(x: Int, width: Int = 32, credit: Long = 0, owners: Set<UUID> = setOf(owner)) =
        ClaimClusters.Footprint(UUID.randomUUID(), world, owners, x, 0, x + width - 1, 31, credit)

    @Test
    fun `adjacent free claims cost the combined longest side price`() {
        val quote = ClaimClusters.quote(claim(32), listOf(claim(0)), config)
        assertEquals(2L, quote.due)
        assertEquals(2, quote.claims.size)
    }

    @Test
    fun `sixteen empty blocks connect but seventeen do not`() {
        assertEquals(3L, ClaimClusters.quote(claim(48), listOf(claim(0)), config).due)
        assertEquals(0L, ClaimClusters.quote(claim(49), listOf(claim(0)), config).due)
        assertTrue(ClaimClusters.connected(claim(0), claim(32), 0))
        assertFalse(ClaimClusters.connected(claim(0), claim(33), 0))
    }

    @Test
    fun `transitive chains include indirect neighbours and shared coowners`() {
        val other = UUID.randomUUID()
        val quote = ClaimClusters.quote(
            claim(0), listOf(
                claim(32, owners = setOf(owner, other)), claim(64, owners = setOf(other))
            ), config
        )
        assertEquals(4L, quote.due)
        assertEquals(3, quote.claims.size)
    }

    @Test
    fun `different worlds and unrelated owners are excluded`() {
        val existing = listOf(claim(32).copy(world = UUID.randomUUID()), claim(32, owners = setOf(UUID.randomUUID())))
        assertEquals(0L, ClaimClusters.quote(claim(0), existing, config).due)
    }

    @Test
    fun `moving or resizing a claim uses prospective geometry and excludes its old footprint`() {
        val original = claim(1000, credit = 1)
        val moved = original.copy(minX = 32, maxX = 79)
        val quote = ClaimClusters.quote(moved, listOf(original, claim(0)), config)
        assertEquals(2L, quote.due)
        assertEquals(2, quote.claims.size)
    }

    @Test
    fun `adding ownership connects formerly unrelated claims`() {
        val other = UUID.randomUUID()
        val target = claim(32, owners = setOf(other))
        assertEquals(0L, ClaimClusters.quote(target, listOf(claim(0)), config).due)
        assertEquals(2L, ClaimClusters.quote(target.copy(owners = setOf(owner, other)), listOf(claim(0)), config).due)
    }

    @Test
    fun `credits remain on their original claims when cluster splits`() {
        val a = claim(0)
        val b = claim(32, credit = 2)
        assertEquals(0L, ClaimClusters.quote(b, listOf(a), config).due)
        val moved = b.copy(minX = 1000, maxX = 1031)
        assertEquals(2L, ClaimClusters.quote(moved, listOf(a), config).credit)
        assertEquals(0L, ClaimClusters.quote(a, listOf(moved), config).credit)
        assertEquals(2L, ClaimClusters.quote(claim(32), listOf(a, moved), config).due)
    }

    @Test
    fun `bridging clusters sums credits once and merge preserves sum`() {
        val quote = ClaimClusters.quote(claim(32), listOf(claim(0, credit = 1), claim(64, credit = 1)), config)
        assertEquals(2L, quote.credit)
        assertEquals(2L, quote.due)
        val merged = claim(0, width = 96, credit = 4)
        assertEquals(0L, ClaimClusters.quote(merged, emptyList(), config).due)
    }

    @Test
    fun `diagonal proximity and negative coordinates are consistent`() {
        val diagonal = claim(32).copy(minZ = 32, maxZ = 63)
        assertTrue(ClaimClusters.connected(claim(0), diagonal, 0))
        assertEquals(2L, ClaimClusters.quote(claim(0), listOf(claim(-32)), config).due)
    }

    @Test
    fun `cluster pricing can be disabled and distance customised`() {
        assertEquals(
            0L,
            ClaimClusters.quote(claim(32), listOf(claim(0)), config.copy(clusterPricingEnabled = false)).due
        )
        assertEquals(0L, ClaimClusters.quote(claim(40), listOf(claim(0)), config.copy(clusterDistanceBlocks = 7)).due)
    }
}
