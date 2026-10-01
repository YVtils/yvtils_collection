package yv.tils.regionsv2.logic

import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.configs.RegionsV2ConfigState
import yv.tils.regionsv2.data.ClaimMetadata
import yv.tils.regionsv2.data.SubzoneRecord
import yv.tils.regionsv2.language.LangStrings.*
import java.util.UUID

object ClaimSubzones {
    fun refresh(world: org.bukkit.World) {
        val manager = com.sk89q.worldguard.WorldGuard.getInstance().platform.regionContainer
            .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world)) ?: return
        val rollbacks = mutableListOf<() -> Unit>()
        try {
            ClaimService.claims(world).forEach { rollbacks += sync(it) }
            if (rollbacks.isNotEmpty()) manager.saveChanges()
        } catch (e: Exception) {
            rollbacks.asReversed().forEach { it() }
            runCatching { manager.saveChanges() }
            throw IllegalStateException(SUBZONE_SAVE_FAILED.key, e)
        }
    }

    fun regions(claim: Claim): List<ProtectedRegion> = ClaimService.manager(claim.world).regions.values
        .filter { it.id.startsWith(SubzonePolicies.prefix(claim.region.id)) }

    fun requireZone(sender: CommandSender, claim: Claim, id: String): SubzoneRecord {
        yv.tils.regionsv2.data.Permissions.SUBZONES_VIEW.require(sender)
        ClaimService.requireOwner(sender, claim)
        return claim.metadata.subzones.firstOrNull { it.uuid == id } ?: error(SUBZONE_MISSING.key)
    }

    /** Rebuild inherited flags/domains and apply only allowed explicit overrides. Caller persists WorldGuard. */
    fun sync(
        claim: Claim, zones: List<SubzoneRecord> = claim.metadata.subzones,
        config: RegionsV2ConfigState = ConfigFile.state
    ): () -> Unit {
        val manager = ClaimService.manager(claim.world)
        val before = regions(claim)
        val built = zones.flatMap { zone ->
            check(
                SubzonePolicies.contained(
                    SubzonePolicies.min(zone), SubzonePolicies.max(zone),
                    claim.region.minimumPoint, claim.region.maximumPoint
                )
            ) { SUBZONE_OUTSIDE.key }
            val result = SubzonePolicies.create(
                claim.region, ClaimService.target(claim, ClaimRole.OWNER),
                ClaimService.target(claim, ClaimRole.MEMBER),
                zone.copy(openProtection = zone.openProtection && config.allowOpenSubzones),
                ClaimFlags.all().filterIsInstance<StateFlag>()
            )
            if (!(zone.openProtection && config.allowOpenSubzones)) {
                for ((scope, values) in zone.overrides) for ((name, encoded) in values) {
                    val role = if (scope == "GLOBAL") null else ClaimRole.valueOf(scope)
                    val flag = ClaimFlags.all().firstOrNull { it.name == name } ?: continue
                    val policy = ClaimFlags.policy(flag, config)
                    if (ClaimFlags.locked(flag) || !policy.enabled || policy.roleBased != (role != null)) continue
                    SubzonePolicies.override(result, flag, role, ClaimFlags.decode(flag, encoded))
                }
            }
            result
        }
        val rollback = {
            built.forEach { manager.removeRegion(it.id) }
            before.forEach(manager::addRegion)
        }
        try {
            before.forEach { manager.removeRegion(it.id) }
            built.forEach(manager::addRegion)
        } catch (e: Exception) {
            rollback()
            throw e
        }
        return rollback
    }

    private fun save(sender: CommandSender, claim: Claim, zones: List<SubzoneRecord>) {
        ClaimService.requireOwner(sender, claim)
        val rollback = sync(claim, zones)
        try {
            ClaimService.manager(claim.world).saveChanges()
            ClaimMetadata.put(claim.metadata.copy(subzones = zones))
        } catch (e: Exception) {
            rollback()
            runCatching { ClaimService.manager(claim.world).saveChanges() }
            throw IllegalStateException(SUBZONE_SAVE_FAILED.key, e)
        }
    }

    fun create(player: Player, claim: Claim, name: String): SubzoneRecord {
        yv.tils.regionsv2.data.Permissions.SUBZONES_CREATE.require(player)
        ClaimService.requireOwner(player, claim)
        check(name.isNotBlank() && name.length <= 64 && name.none { it.isISOControl() }) { INVALID_NAME.key }
        check(
            ClaimLimits.below(
                claim.metadata.subzones.size,
                ConfigFile.state.maxSubzonesPerClaim
            )
        ) { SUBZONE_LIMIT.key }
        val bounds = SubzoneSelection.bounds(player, claim)
        check(
            SubzonePolicies.contained(
                bounds.min,
                bounds.max,
                claim.region.minimumPoint,
                claim.region.maximumPoint
            )
        ) { SUBZONE_OUTSIDE.key }
        check(claim.metadata.subzones.none {
            bounds.min.x() <= it.maxX && bounds.max.x() >= it.minX &&
                    bounds.min.y() <= it.maxY && bounds.max.y() >= it.minY &&
                    bounds.min.z() <= it.maxZ && bounds.max.z() >= it.minZ
        }) { SUBZONE_OVERLAP.key }
        val zone = SubzoneRecord(
            UUID.randomUUID().toString(), name,
            bounds.min.x(), bounds.min.y(), bounds.min.z(), bounds.max.x(), bounds.max.y(), bounds.max.z()
        )
        save(player, claim, claim.metadata.subzones + zone)
        SubzoneSelection.clear(player.uniqueId)
        return zone
    }

    fun delete(sender: CommandSender, claim: Claim, id: String) {
        yv.tils.regionsv2.data.Permissions.SUBZONES_DELETE.require(sender)
        requireZone(sender, claim, id)
        save(sender, claim, claim.metadata.subzones.filterNot { it.uuid == id })
    }

    fun open(sender: CommandSender, claim: Claim, id: String, enabled: Boolean) {
        yv.tils.regionsv2.data.Permissions.SUBZONES_OPEN.require(sender)
        val zone = requireZone(sender, claim, id)
        check(!enabled || ConfigFile.state.allowOpenSubzones) { SUBZONE_OPEN_DISABLED.key }
        save(
            sender,
            claim,
            claim.metadata.subzones.map { if (it.uuid == id) zone.copy(openProtection = enabled) else it })
    }

    fun set(sender: CommandSender, claim: Claim, id: String, flag: Flag<*>, role: ClaimRole?, value: Any?) {
        yv.tils.regionsv2.data.Permissions.SUBZONES_FLAGS.require(sender)
        val zone = requireZone(sender, claim, id)
        check(!zone.openProtection || !ConfigFile.state.allowOpenSubzones) { SUBZONE_OPEN_ACTIVE.key }
        check(ClaimFlags.enabled(flag, role)) { FLAG_CHANGED.key }
        val key = role?.name ?: "GLOBAL"
        val values = zone.overrides[key].orEmpty()
        val updated = if (value == null) values - flag.name else values + (flag.name to ClaimFlags.encode(flag, value))
        save(sender, claim, claim.metadata.subzones.map {
            if (it.uuid == id) zone.copy(overrides = zone.overrides + (key to updated)) else it
        })
    }

    fun value(claim: Claim, id: String, flag: Flag<*>, role: ClaimRole?): Any? {
        val zone = claim.metadata.subzones.firstOrNull { it.uuid == id } ?: error(SUBZONE_MISSING.key)
        return zone.overrides[role?.name ?: "GLOBAL"]?.get(flag.name)?.let { ClaimFlags.decode(flag, it) }
    }
}
