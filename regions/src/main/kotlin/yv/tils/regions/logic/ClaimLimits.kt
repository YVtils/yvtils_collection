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
import yv.tils.regions.data.ClaimRecord
import java.util.*

/** Pure limit checks include metadata for unloaded worlds. */
object ClaimLimits {
    fun below(count: Int, maximum: Int) = maximum == -1 || count < maximum
    fun ownership(
        records: Collection<ClaimRecord>,
        uuid: UUID,
        world: UUID,
        config: RegionsConfigState,
        exclude: UUID? = null
    ): Boolean {
        val owned = records.filter { it.uuid != exclude?.toString() && uuid.toString() in it.owners }
        return below(owned.size, config.maxClaimsTotal) && below(
            owned.count { it.world == world.toString() },
            config.maxClaimsPerWorld
        )
    }

    fun membership(
        records: Collection<ClaimRecord>,
        uuid: UUID,
        claim: UUID,
        memberCount: Int,
        config: RegionsConfigState
    ): Boolean =
        below(
            memberCount,
            config.maxMembersPerClaim
        ) && below(
            records.count { it.uuid != claim.toString() && uuid.toString() in it.members },
            config.maxMembershipsPerPlayer
        )
}
