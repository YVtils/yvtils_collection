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

import com.sk89q.worldedit.math.BlockVector3
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.regions.configs.RegionsConfigState
import yv.tils.regions.data.ClaimRecord
import java.util.*

class ClaimLimitsGeometryTest {
    private val owner = UUID.randomUUID()
    private val worldA = UUID.randomUUID()
    private val worldB = UUID.randomUUID()
    private fun record(world: UUID) = ClaimRecord(
        UUID.randomUUID().toString(),
        world.toString(),
        "Same name",
        owners = listOf(owner.toString()),
        members = listOf(owner.toString())
    )

    @Test
    fun `total ownership includes unloaded worlds and independent per-world limits`() {
        val records = listOf(record(worldA), record(worldB))
        assertFalse(
            ClaimLimits.ownership(
                records,
                owner,
                worldA,
                RegionsConfigState(maxClaimsTotal = 2, maxClaimsPerWorld = -1)
            )
        )
        assertTrue(
            ClaimLimits.ownership(
                records,
                owner,
                worldA,
                RegionsConfigState(maxClaimsTotal = -1, maxClaimsPerWorld = 2)
            )
        )
        assertFalse(
            ClaimLimits.ownership(
                records,
                owner,
                worldA,
                RegionsConfigState(maxClaimsTotal = -1, maxClaimsPerWorld = 1)
            )
        )
        assertTrue(
            ClaimLimits.ownership(
                records,
                owner,
                worldA,
                RegionsConfigState(maxClaimsTotal = -1, maxClaimsPerWorld = -1)
            )
        )
    }

    @Test
    fun `zero disables additions and minus one is unlimited`() {
        assertFalse(ClaimLimits.below(0, 0))
        assertTrue(ClaimLimits.below(Int.MAX_VALUE, -1))
        assertFalse(ClaimLimits.below(5, 5))
    }

    @Test
    fun `membership limits include other worlds but exclude current claim`() {
        val records = listOf(record(worldA), record(worldB))
        val config = RegionsConfigState(maxMembersPerClaim = 2, maxMembershipsPerPlayer = 1)
        assertFalse(ClaimLimits.membership(records, owner, UUID.randomUUID(), 0, config))
        assertTrue(
            ClaimLimits.membership(
                records,
                owner,
                UUID.fromString(records[0].uuid),
                0,
                config.copy(maxMembershipsPerPlayer = 2)
            )
        )
        assertFalse(ClaimLimits.membership(emptyList(), owner, UUID.randomUUID(), 2, config))
    }

    @Test
    fun `adjacent rectangles merge without adding land`() {
        val a = BlockVector3.at(0, -64, 0);
        val b = BlockVector3.at(9, 319, 9)
        val result = ClaimGeometry.mergeBounds(a, b, BlockVector3.at(10, -64, 0), BlockVector3.at(19, 319, 9))
        assertEquals(a, result.first); assertEquals(BlockVector3.at(19, 319, 9), result.second)
    }

    @Test
    fun `merging rejects gaps and L shaped unions`() {
        val a = BlockVector3.at(0, -64, 0);
        val b = BlockVector3.at(9, 319, 9)
        assertThrows(IllegalStateException::class.java) {
            ClaimGeometry.mergeBounds(
                a,
                b,
                BlockVector3.at(11, -64, 0),
                BlockVector3.at(19, 319, 9)
            )
        }
        assertThrows(IllegalStateException::class.java) {
            ClaimGeometry.mergeBounds(
                a,
                b,
                BlockVector3.at(10, -64, 0),
                BlockVector3.at(19, 319, 4)
            )
        }
    }
}
