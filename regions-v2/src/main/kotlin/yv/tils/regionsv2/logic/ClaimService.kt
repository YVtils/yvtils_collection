package yv.tils.regionsv2.logic

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.domains.DefaultDomain
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.data.*
import yv.tils.regionsv2.language.LangStrings.*
import java.util.UUID

data class Claim(val world: World, val region: ProtectedRegion) {
    val uuid: UUID get() = UUID.fromString(region.id.removePrefix(ClaimService.PREFIX))
    val metadata: ClaimRecord get() = ClaimMetadata.state.claims[uuid.toString()] ?: error(METADATA_MISSING.key)
    val name: String get() = metadata.name
}

object ClaimService {
    const val PREFIX = "yv2_"
    fun admin(sender: CommandSender) = sender is org.bukkit.command.ConsoleCommandSender ||
            sender is org.bukkit.command.RemoteConsoleCommandSender || Permissions.ADMIN_OTHERS.allowed(sender)

    fun manager(world: World): RegionManager = WorldGuard.getInstance().platform.regionContainer
        .get(BukkitAdapter.adapt(world)) ?: error(NO_MANAGER.key)

    fun claims(world: World): List<Claim> = manager(world).regions.values.filter {
        it.id.startsWith(PREFIX) && !it.id.contains("__") && runCatching { UUID.fromString(it.id.removePrefix(PREFIX)) }.isSuccess
    }.map { Claim(world, it) }

    fun all(): List<Claim> = Bukkit.getWorlds().flatMap { world ->
        if (WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(world)) == null) emptyList() else claims(
            world
        )
    }

    fun find(world: World, name: String): Claim = resolve(name, world)
    fun resolve(name: String, world: World? = null): Claim {
        val explicit = runCatching { UUID.fromString(name.removePrefix(PREFIX)) }.isSuccess
        val matches = all().filter {
            (explicit || world == null || it.world == world) &&
                    (it.name.equals(name, true) || it.uuid.toString().equals(name, true) || it.region.id == name)
        }
        check(matches.size == 1) { if (matches.isEmpty()) CLAIM_NOT_FOUND.key else AMBIGUOUS.key }
        return matches.single()
    }

    fun at(location: Location): Claim? {
        val manager =
            WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(location.world)) ?: return null
        return manager.getApplicableRegions(BukkitAdapter.asBlockVector(location)).regions.firstOrNull {
            it.id.startsWith(PREFIX) && !it.id.contains("__") && runCatching { UUID.fromString(it.id.removePrefix(PREFIX)) }.isSuccess
        }?.let { Claim(location.world, it) }
    }

    fun role(claim: Claim, uuid: UUID) = when {
        claim.region.owners.contains(uuid) -> ClaimRole.OWNER
        claim.region.members.contains(uuid) -> ClaimRole.MEMBER
        else -> ClaimRole.VISITOR
    }

    fun target(claim: Claim, role: ClaimRole?): ProtectedRegion = when (role) {
        null, ClaimRole.VISITOR -> claim.region
        else -> manager(claim.world).getRegion("${claim.region.id}__${role.name.lowercase()}")
            ?: error(POLICY_MISSING.key)
    }

    fun requireOwner(sender: CommandSender, claim: Claim) {
        Permissions.MANAGE.require(sender)
        check(ConfigFile.state.enabled) { DISABLED.key }
        check(
            admin(sender) || sender is Player && sender.hasPermission(Permissions.MANAGE.permission.name) && role(
                claim,
                sender.uniqueId
            ) == ClaimRole.OWNER
        ) { OWNER_REQUIRED.key }
        check(manager(claim.world).getRegion(claim.region.id) === claim.region) { STALE_CLAIM.key }
        target(claim, ClaimRole.OWNER); target(claim, ClaimRole.MEMBER)
    }

    private fun record(claim: Claim, previous: ClaimRecord = claim.metadata) = previous.copy(
        owners = claim.region.owners.uniqueIds.map(UUID::toString),
        members = claim.region.members.uniqueIds.map(UUID::toString)
    )

    private fun ownership(uuid: UUID, world: World, exclude: UUID? = null) {
        check(
            ClaimLimits.ownership(
                ClaimMetadata.state.claims.values,
                uuid,
                world.uid,
                ConfigFile.state,
                exclude
            )
        ) { OWNERSHIP_LIMIT.key }
    }

    fun bounds(world: World, x1: Int, z1: Int, x2: Int, z2: Int) = ClaimBounds(
        world,
        BlockVector3.at(minOf(x1, x2), world.minHeight, minOf(z1, z2)),
        BlockVector3.at(maxOf(x1, x2), world.maxHeight - 1, maxOf(z1, z2))
    )

    private fun validate(bounds: ClaimBounds, ignored: Set<String> = emptySet()) {
        val config = ConfigFile.state
        check(bounds.world.name !in config.disabledWorlds) { WORLD_DISABLED.key }
        val area = bounds.sides[0].toLong() * bounds.sides[2]
        check(area >= config.minClaimArea) { TOO_SMALL.key }
        check(
            (config.maxClaimVolume == -1L || bounds.volume <= config.maxClaimVolume) &&
                    (config.maxClaimSide == -1 || listOf(
                        bounds.sides[0],
                        bounds.sides[2]
                    ).all { it <= config.maxClaimSide })
        ) { TOO_LARGE.key }
        check(
            bounds.world.worldBorder.isInside(
                Location(
                    bounds.world,
                    bounds.min.x() + 0.5,
                    0.0,
                    bounds.min.z() + 0.5
                )
            ) &&
                    bounds.world.worldBorder.isInside(
                        Location(
                            bounds.world,
                            bounds.max.x() + 0.5,
                            0.0,
                            bounds.max.z() + 0.5
                        )
                    )
        ) { WORLD_BORDER.key }
        val candidate = ProtectedCuboidRegion("candidate", bounds.min, bounds.max)
        check(manager(bounds.world).getApplicableRegions(candidate).regions.none { it.id !in ignored }) { OVERLAP.key }
    }

    fun create(player: Player, name: String): Claim = create(
        player,
        name,
        player.uniqueId,
        ClaimSelection.bounds(player)
    ).also { ClaimSelection.clear(player.uniqueId) }

    fun create(sender: CommandSender, name: String, owner: UUID, bounds: ClaimBounds): Claim {
        check(ConfigFile.state.enabled) { DISABLED.key }
        Permissions.CLAIM.require(sender)
        if (sender !is Player || sender.uniqueId != owner || sender.world != bounds.world) Permissions.ADMIN_CREATE.require(sender)
        check(sender !is Player || Permissions.BYPASS_SURVIVAL.allowed(sender) || !ConfigFile.state.survivalOnly || sender.gameMode == GameMode.SURVIVAL) { SURVIVAL_REQUIRED.key }
        check(sender !is Player || sender.world == bounds.world || Permissions.ADMIN_CREATE.allowed(sender)) { SAME_WORLD.key }
        check(name.isNotBlank() && name.length <= 64 && name.none { it.isISOControl() }) { INVALID_NAME.key }
        if (!Permissions.BYPASS_LIMITS.allowed(sender)) ownership(owner, bounds.world)
        validate(bounds)
        val uuid = UUID.randomUUID()
        val region = ProtectedCuboidRegion(PREFIX + uuid, bounds.min, bounds.max).apply { owners.addPlayer(owner) }
        val claim = Claim(bounds.world, region)
        val manager = manager(bounds.world)
        val regions = listOf(
            region,
            ClaimPolicies.create(region, ClaimRole.OWNER),
            ClaimPolicies.create(region, ClaimRole.MEMBER)
        )
        val payment = ClaimCurrency.plan(sender, bounds, setOf(owner))
        ClaimCurrency.pay(sender, payment.due) {
            regions.forEach(manager::addRegion)
            try {
                ClaimFlags.applyDefaults(claim, ConfigFile.state)
                manager.saveChanges()
                ClaimMetadata.put(
                    record(
                        claim,
                        ClaimRecord(
                            uuid.toString(), bounds.world.uid.toString(), name, System.currentTimeMillis(),
                            currencyCredit = payment.resultingCredit
                        )
                    )
                )
            } catch (e: Exception) {
                regions.forEach { manager.removeRegion(it.id) }; runCatching { manager.saveChanges() }
                throw IllegalStateException(CREATE_SAVE_FAILED.key, e)
            }
        }
        return claim
    }

    fun setRole(sender: CommandSender, claim: Claim, uuid: UUID, role: ClaimRole) {
        requireOwner(sender, claim)
        val previous = role(claim, uuid)
        if (previous == ClaimRole.OWNER || role == ClaimRole.OWNER) Permissions.OWNERS_EDIT.require(sender)
        else Permissions.MEMBERS_EDIT.require(sender)
        check(previous != ClaimRole.OWNER || role == ClaimRole.OWNER || claim.region.owners.uniqueIds.size > 1) { LAST_OWNER.key }
        if (!Permissions.BYPASS_LIMITS.allowed(sender) && role != previous) {
            if (role == ClaimRole.OWNER) ownership(uuid, claim.world, claim.uuid)
            if (role == ClaimRole.MEMBER) {
                check(
                    ClaimLimits.below(
                        claim.region.members.uniqueIds.size,
                        ConfigFile.state.maxMembersPerClaim
                    )
                ) { MEMBER_LIMIT.key }
                check(
                    ClaimLimits.membership(
                        ClaimMetadata.state.claims.values,
                        uuid,
                        claim.uuid,
                        claim.region.members.uniqueIds.size,
                        ConfigFile.state
                    )
                ) { MEMBERSHIP_LIMIT.key }
            }
        }
        val originals = listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER).map { target(claim, it) }
            .associateWith { DefaultDomain(it.owners) to DefaultDomain(it.members) }
        val owners = claim.region.owners.uniqueIds.toMutableSet().apply {
            if (role == ClaimRole.OWNER) add(uuid) else remove(uuid)
        }
        val payment = if (role == ClaimRole.OWNER && previous != ClaimRole.OWNER) ClaimCurrency.plan(
            sender,
            ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint),
            owners,
            listOf(claim)
        ) else ClaimCurrency.Payment(0, ClaimCurrency.credit(claim))
        var subzoneRollback: (() -> Unit)? = null
        ClaimCurrency.pay(sender, payment.due) {
            claim.region.owners.removePlayer(uuid); claim.region.members.removePlayer(uuid)
            when (role) {
                ClaimRole.OWNER -> claim.region.owners.addPlayer(uuid); ClaimRole.MEMBER -> claim.region.members.addPlayer(
                uuid
            ); else -> Unit
            }
            try {
                for (r in listOf(ClaimRole.OWNER, ClaimRole.MEMBER)) ClaimPolicies.sync(
                    claim.region,
                    target(claim, r),
                    r
                )
                subzoneRollback = ClaimSubzones.sync(claim)
                manager(claim.world).saveChanges()
                ClaimMetadata.put(record(claim).copy(currencyCredit = payment.resultingCredit))
            } catch (e: Exception) {
                subzoneRollback?.invoke()
                originals.forEach { (region, domains) ->
                    region.owners = domains.first; region.members = domains.second
                }
                runCatching { manager(claim.world).saveChanges() }; throw IllegalStateException(
                    ROLE_SAVE_FAILED.key,
                    e
                )
            }
        }
    }

    fun delete(sender: CommandSender, claim: Claim) {
        Permissions.DELETE.require(sender)
        requireOwner(sender, claim)
        val manager = manager(claim.world)
        val removed =
            listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER).map { target(claim, it) } + ClaimSubzones.regions(claim)
        removed.forEach { manager.removeRegion(it.id) }
        try {
            manager.saveChanges(); ClaimMetadata.remove(claim.uuid)
        } catch (e: Exception) {
            removed.forEach(manager::addRegion); runCatching { manager.saveChanges() }; throw IllegalStateException(
                DELETE_SAVE_FAILED.key,
                e
            )
        }
    }

    fun resize(sender: CommandSender, claim: Claim, bounds: ClaimBounds): Claim {
        Permissions.RESIZE.require(sender)
        requireOwner(sender, claim); check(bounds.world == claim.world) { SAME_WORLD.key }
        val originals = listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER).map { target(claim, it) }
        check(claim.metadata.subzones.all {
            SubzonePolicies.contained(
                SubzonePolicies.min(it),
                SubzonePolicies.max(it),
                bounds.min,
                bounds.max
            )
        }) { SUBZONE_OUTSIDE.key }
        validate(bounds, (originals + ClaimSubzones.regions(claim)).map { it.id }.toSet())
        val replacements = originals.map { old ->
            ProtectedCuboidRegion(old.id, bounds.min, bounds.max).apply {
                copyFrom(old); parent = null
            }
        }
        val manager = manager(claim.world)
        val payment = ClaimCurrency.plan(sender, bounds, claim.region.owners.uniqueIds, listOf(claim))
        ClaimCurrency.pay(sender, payment.due) {
            try {
                replacements.forEach(manager::addRegion); manager.saveChanges()
                ClaimMetadata.put(claim.metadata.copy(currencyCredit = payment.resultingCredit))
            } catch (e: Exception) {
                originals.forEach(manager::addRegion); runCatching { manager.saveChanges() }; throw IllegalStateException(
                    RESIZE_FAILED.key,
                    e
                )
            }
        }
        return Claim(claim.world, replacements.first())
    }

    fun merge(sender: CommandSender, keep: Claim, other: Claim): Claim {
        Permissions.MERGE.require(sender)
        requireOwner(sender, keep); requireOwner(sender, other)
        check(keep.uuid != other.uuid && keep.world == other.world) { MERGE_DISTINCT.key }
        check(keep.region.owners.uniqueIds == other.region.owners.uniqueIds && keep.region.members.uniqueIds == other.region.members.uniqueIds) { MERGE_DOMAINS.key }
        val roles = listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER)
        check(roles.all {
            target(keep, it).flags == target(
                other,
                it
            ).flags
        } && keep.metadata.welcome == other.metadata.welcome && keep.metadata.goodbye == other.metadata.goodbye) { MERGE_FLAGS.key }
        val merged = ClaimGeometry.mergeBounds(
            keep.region.minimumPoint,
            keep.region.maximumPoint,
            other.region.minimumPoint,
            other.region.maximumPoint
        )
        val bounds = ClaimBounds(keep.world, merged.first, merged.second)
        val manager = manager(keep.world)
        val originals = (roles.map { target(keep, it) } + roles.map { target(other, it) } +
                ClaimSubzones.regions(keep) + ClaimSubzones.regions(other))
        validate(bounds, originals.map { it.id }.toSet())
        val replacements = originals.take(3)
            .map { old -> ProtectedCuboidRegion(old.id, bounds.min, bounds.max).apply { copyFrom(old); parent = null } }
        val payment = ClaimCurrency.plan(sender, bounds, keep.region.owners.uniqueIds, listOf(keep, other))
        val zones = keep.metadata.subzones + other.metadata.subzones
        check(ConfigFile.state.maxSubzonesPerClaim == -1 || zones.size <= ConfigFile.state.maxSubzonesPerClaim) { SUBZONE_LIMIT.key }
        ClaimCurrency.pay(sender, payment.due) {
            try {
                originals.forEach { manager.removeRegion(it.id) }; replacements.forEach(manager::addRegion)
                ClaimSubzones.sync(Claim(keep.world, replacements.first()), zones)
                manager.saveChanges()
                ClaimMetadata.save(
                    (ClaimMetadata.state.claims - other.uuid.toString()) +
                            (keep.uuid.toString() to keep.metadata.copy(
                                currencyCredit = payment.resultingCredit, subzones = zones
                            ))
                )
            } catch (e: Exception) {
                ClaimSubzones.regions(keep).forEach { manager.removeRegion(it.id) }
                originals.forEach(manager::addRegion); runCatching { manager.saveChanges() }; throw IllegalStateException(
                    MERGE_FAILED.key,
                    e
                )
            }
        }
        return Claim(keep.world, replacements.first())
    }

    fun rename(sender: CommandSender, claim: Claim, name: String) {
        Permissions.RENAME.require(sender)
        requireOwner(sender, claim)
        check(name.isNotBlank() && name.length <= 64 && name.none { it.isISOControl() }) { INVALID_NAME.key }
        ClaimMetadata.put(claim.metadata.copy(name = name))
    }

    fun messages(sender: CommandSender, claim: Claim, welcome: Boolean, text: String) {
        Permissions.ADMIN_MESSAGES.require(sender)
        requireOwner(
            sender,
            claim
        ); check(text.length <= 160 && text.none { it.isISOControl() }) { INVALID_MESSAGE.key }
        ClaimMetadata.put(if (welcome) claim.metadata.copy(welcome = text) else claim.metadata.copy(goodbye = text))
    }

    fun refreshMetadata(world: World) {
        if (WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(world)) == null) return
        var records = ClaimMetadata.state.claims
        val present = claims(world).map { it.uuid.toString() }.toSet()
        records = records.filterValues { it.world != world.uid.toString() || it.uuid in present }
        for (claim in claims(world)) {
            val previous = records[claim.uuid.toString()] ?: continue
            records = records + (claim.uuid.toString() to record(claim, previous))
        }
        if (records != ClaimMetadata.state.claims) ClaimMetadata.save(records)
    }

    /** Upgrade this module's earlier name-keyed cuboids; never reads the legacy regions module. */
    fun upgradeNames(world: World) {
        val manager = WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(world)) ?: return
        val oldClaims = manager.regions.values.filter {
            it.id.startsWith(PREFIX) && !it.id.contains("__") &&
                    runCatching { UUID.fromString(it.id.removePrefix(PREFIX)) }.isFailure
        }.toList()
        for (old in oldClaims) {
            val policies =
                listOf("owner", "member").map { manager.getRegion("${old.id}__$it") ?: error(POLICY_MISSING.key) }
            val uuid = UUID.randomUUID()
            val id = PREFIX + uuid
            val originals = listOf(old) + policies
            val replacements = originals.mapIndexed { index, region ->
                ProtectedCuboidRegion(
                    if (index == 0) id else "${id}__${if (index == 1) "owner" else "member"}",
                    BlockVector3.at(region.minimumPoint.x(), world.minHeight, region.minimumPoint.z()),
                    BlockVector3.at(region.maximumPoint.x(), world.maxHeight - 1, region.maximumPoint.z())
                ).apply { copyFrom(region); parent = null }
            }
            try {
                originals.forEach { manager.removeRegion(it.id) }; replacements.forEach(manager::addRegion)
                manager.saveChanges()
                ClaimMetadata.put(
                    record(
                        Claim(world, replacements.first()),
                        ClaimRecord(
                            uuid.toString(),
                            world.uid.toString(),
                            old.id.removePrefix(PREFIX),
                            System.currentTimeMillis()
                        )
                    )
                )
            } catch (e: Exception) {
                replacements.forEach { manager.removeRegion(it.id) }; originals.forEach(manager::addRegion)
                runCatching { manager.saveChanges() }; throw IllegalStateException(FAILED.key, e)
            }
        }
    }
}
