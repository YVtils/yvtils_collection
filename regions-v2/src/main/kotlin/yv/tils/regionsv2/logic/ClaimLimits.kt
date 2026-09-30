package yv.tils.regionsv2.logic

import yv.tils.regionsv2.configs.RegionsV2ConfigState
import yv.tils.regionsv2.data.ClaimRecord
import java.util.UUID

/** Pure limit checks include metadata for unloaded worlds. */
object ClaimLimits {
    fun below(count: Int, maximum: Int) = maximum == -1 || count < maximum
    fun ownership(
        records: Collection<ClaimRecord>,
        uuid: UUID,
        world: UUID,
        config: RegionsV2ConfigState,
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
        config: RegionsV2ConfigState
    ): Boolean =
        below(
            memberCount,
            config.maxMembersPerClaim
        ) && below(
            records.count { it.uuid != claim.toString() && uuid.toString() in it.members },
            config.maxMembershipsPerPlayer
        )
}
