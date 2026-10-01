/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */
package yv.tils.regions.logic

import com.sk89q.worldguard.domains.DefaultDomain
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.RegionGroup
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion

/** Native WorldGuard group policies, independently testable without a Bukkit server. */
object ClaimPolicies {
    fun create(region: ProtectedRegion, role: ClaimRole): ProtectedRegion {
        require(role != ClaimRole.VISITOR)
        return ProtectedCuboidRegion(
            "${region.id}__${role.name.lowercase()}",
            region.minimumPoint,
            region.maximumPoint
        ).apply {
            // Do not parent these: WorldGuard inherits parent ownership, which would
            // make primary owners match the member-only policy as well.
            priority = region.priority + 1
            setFlag(Flags.PASSTHROUGH, StateFlag.State.ALLOW)
            setFlag(Flags.PASSTHROUGH.regionGroupFlag, RegionGroup.ALL)
            sync(region, this, role)
        }
    }

    fun sync(region: ProtectedRegion, policy: ProtectedRegion, role: ClaimRole) {
        val owners = region.owners.uniqueIds
        val members = region.members.uniqueIds
        policy.owners =
            DefaultDomain().apply { (if (role == ClaimRole.OWNER) owners else members).forEach(::addPlayer) }
        policy.members =
            DefaultDomain().apply { (if (role == ClaimRole.OWNER) members else owners).forEach(::addPlayer) }
    }

    fun <T> write(target: ProtectedRegion, flag: Flag<T>, role: ClaimRole?, state: T?) {
        target.setFlag(flag, state)
        flag.regionGroupFlag?.let { group ->
            target.setFlag(
                group, when (role) {
                    null -> RegionGroup.ALL
                    ClaimRole.VISITOR -> RegionGroup.NON_MEMBERS
                    else -> RegionGroup.OWNERS
                }
            )
        }
    }
}
