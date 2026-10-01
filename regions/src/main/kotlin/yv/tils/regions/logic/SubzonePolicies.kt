package yv.tils.regions.logic

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.domains.DefaultDomain
import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import yv.tils.regions.data.SubzoneRecord

/** Materialized inheritance avoids WorldGuard parent domains leaking owners into member policies. */
object SubzonePolicies {
    fun contained(min: BlockVector3, max: BlockVector3, parentMin: BlockVector3, parentMax: BlockVector3): Boolean =
        min.x() <= max.x() && min.y() <= max.y() && min.z() <= max.z() &&
                min.x() >= parentMin.x() && min.y() >= parentMin.y() && min.z() >= parentMin.z() &&
                max.x() <= parentMax.x() && max.y() <= parentMax.y() && max.z() <= parentMax.z()

    fun min(zone: SubzoneRecord): BlockVector3 = BlockVector3.at(zone.minX, zone.minY, zone.minZ)
    fun max(zone: SubzoneRecord): BlockVector3 = BlockVector3.at(zone.maxX, zone.maxY, zone.maxZ)
    fun prefix(claimId: String) = "${claimId}__zone_"

    fun create(
        base: ProtectedRegion, owner: ProtectedRegion, member: ProtectedRegion, zone: SubzoneRecord,
        stateFlags: List<StateFlag>
    ): List<ProtectedRegion> {
        val sources = listOf(base, owner, member)
        return sources.mapIndexed { index, source ->
            ProtectedCuboidRegion(
                prefix(base.id) + zone.uuid + when (index) {
                    1 -> "__owner"; 2 -> "__member"; else -> ""
                }, min(zone), max(zone)
            ).apply {
                priority = base.priority + if (index == 0) 2 else 3
                owners = DefaultDomain(source.owners)
                members = DefaultDomain(source.members)
                flags = source.flags.toMap()
                if (zone.openProtection) {
                    // Explicit ALLOW at higher priority, not passthrough alone (which doesn't erase lower DENY).
                    for (flag in stateFlags) ClaimPolicies.write(this, flag, null, StateFlag.State.ALLOW)
                    ClaimPolicies.write(this, Flags.BUILD, null, StateFlag.State.ALLOW)
                    ClaimPolicies.write(this, Flags.PASSTHROUGH, null, StateFlag.State.ALLOW)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun override(regions: List<ProtectedRegion>, flag: Flag<*>, role: ClaimRole?, value: Any?) {
        val target = when (role) {
            ClaimRole.OWNER -> regions[1]
            ClaimRole.MEMBER -> regions[2]
            else -> regions[0]
        }
        ClaimPolicies.write(target, flag as Flag<Any>, role, value)
    }
}
