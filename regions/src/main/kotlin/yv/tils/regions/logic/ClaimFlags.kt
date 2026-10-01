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

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.configs.FlagPolicy
import yv.tils.regions.configs.RegionsConfigState
import yv.tils.regions.language.LangStrings.*
import yv.tils.regions.language.RegionFailure

enum class ClaimRole { OWNER, MEMBER, VISITOR }

/** The live flag registry is authoritative, including flags registered by other plugins. */
object ClaimFlags {
    val roles: List<StateFlag> = listOf(
        Flags.BLOCK_BREAK, Flags.BLOCK_PLACE, Flags.CHEST_ACCESS, Flags.USE,
        Flags.INTERACT, Flags.DAMAGE_ANIMALS, Flags.ENTRY, Flags.ITEM_PICKUP,
        Flags.ITEM_DROP, Flags.SLEEP, Flags.RIDE, Flags.PLACE_VEHICLE,
        Flags.DESTROY_VEHICLE, Flags.ITEM_FRAME_ROTATE, Flags.ENDERPEARL,
    )
    val global: List<StateFlag> = listOf(
        Flags.PVP, Flags.TNT, Flags.CREEPER_EXPLOSION, Flags.OTHER_EXPLOSION,
        Flags.FIRE_SPREAD, Flags.MOB_SPAWNING, Flags.LAVA_FIRE, Flags.GHAST_FIREBALL,
        Flags.WITHER_DAMAGE, Flags.ENDER_BUILD, Flags.WATER_FLOW, Flags.LAVA_FLOW,
    )

    fun all(): List<Flag<*>> = WorldGuard.getInstance().flagRegistry.toList().sortedBy { it.name }
    fun locked(flag: Flag<*>): Boolean = flag is RegionGroupFlag || flag == Flags.PASSTHROUGH ||
            flag == Flags.BUILD || flag == Flags.NONPLAYER_PROTECTION_DOMAINS

    fun supportsRoles(flag: Flag<*>) = !locked(flag) && flag.regionGroupFlag != null
    fun policy(flag: Flag<*>, config: RegionsConfigState = ConfigFile.state): FlagPolicy =
        config.flagPolicies[flag.name] ?: FlagPolicy(
            enabled = flag.name in config.enabledRoleFlags || flag.name in config.enabledGlobalFlags,
            roleBased = flag in roles,
        )

    fun enabled(flag: Flag<*>, role: ClaimRole?): Boolean {
        val policy = policy(flag)
        return !locked(flag) && policy.enabled && policy.roleBased == (role != null)
    }

    fun available(role: ClaimRole?): List<Flag<*>> = all().filter { enabled(it, role) }

    @Suppress("UNCHECKED_CAST")
    private fun typed(flag: Flag<*>) = flag as Flag<Any>
    fun encode(flag: Flag<*>, value: Any?): String = if (value == null) "" else
        YamlConfiguration().apply { set("value", typed(flag).marshal(value)) }.saveToString()

    fun decode(flag: Flag<*>, encoded: String): Any? {
        if (encoded.isBlank()) return null
        val yaml = YamlConfiguration().apply { loadFromString(encoded) }
        fun plain(value: Any?): Any? = when (value) {
            is org.bukkit.configuration.ConfigurationSection -> value.getValues(false).mapValues { plain(it.value) }
            is List<*> -> value.map { plain(it) }
            else -> value
        }
        return typed(flag).unmarshal(plain(yaml.get("value"))) ?: error(INVALID_FLAG.key)
    }

    fun parse(player: CommandSender, flag: Flag<*>, input: String): Any? {
        if (input.equals("unset", true)) return null
        try {
            return typed(flag).parseInput(
                FlagContext.create().setSender(
                    if (player is Player) WorldGuardPlugin.inst().wrapPlayer(player) else BukkitAdapter.adapt(player)
                ).setInput(input).build()
            )
        } catch (e: com.sk89q.worldguard.protection.flags.InvalidFlagFormat) {
            throw RegionFailure(INVALID_FLAG, cause = e)
        }
    }

    fun defaultValue(flag: Flag<*>, role: ClaimRole?, config: RegionsConfigState = ConfigFile.state): Any? {
        val policy = policy(flag, config)
        val key = role?.name ?: "GLOBAL"
        policy.defaults[key]?.let { return decode(flag, it) }
        // Preserve the original protective defaults when upgrading configurations.
        if (flag in roles && role != null) return if (role != ClaimRole.VISITOR ||
            flag in listOf(Flags.ENTRY, Flags.ITEM_PICKUP, Flags.ITEM_DROP, Flags.ENDERPEARL)
        ) StateFlag.State.ALLOW else StateFlag.State.DENY
        return null
    }

    fun value(claim: Claim, flag: Flag<*>, role: ClaimRole?): Any? =
        ClaimService.target(claim, role).getFlag(typed(flag))

    fun write(claim: Claim, flag: Flag<*>, role: ClaimRole?, value: Any?) {
        val target = ClaimService.target(claim, role)
        ClaimPolicies.write(target, typed(flag), role, value)
    }

    private fun reset(claim: Claim, flag: Flag<*>, config: RegionsConfigState) {
        for (role in listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER)) {
            val target = ClaimService.target(claim, role)
            target.setFlag(typed(flag), null)
            flag.regionGroupFlag?.let { target.setFlag(it, null) }
        }
        if (policy(flag, config).roleBased) for (role in ClaimRole.entries) write(
            claim,
            flag,
            role,
            defaultValue(flag, role, config)
        )
        else write(claim, flag, null, defaultValue(flag, null, config))
    }

    fun applyDefaults(claim: Claim, config: RegionsConfigState) {
        for (flag in all().filterNot(::locked)) reset(claim, flag, config)
    }

    fun restoreDefaults(sender: CommandSender, claim: Claim) {
        yv.tils.regions.data.Permissions.FLAGS_RESET.require(sender)
        ClaimService.requireOwner(sender, claim)
        val snapshots = listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER)
            .associate { ClaimService.target(claim, it).let { region -> region to region.flags.toMap() } }
        var subzoneRollback: (() -> Unit)? = null
        try {
            applyDefaults(claim, ConfigFile.state)
            subzoneRollback = ClaimSubzones.sync(claim)
            ClaimService.manager(claim.world).saveChanges()
        } catch (e: Exception) {
            subzoneRollback?.invoke()
            snapshots.forEach { (region, flags) -> region.flags = flags }
            runCatching { ClaimService.manager(claim.world).saveChanges() }
            throw RegionFailure(FLAG_SAVE_FAILED, cause = e)
        }
    }

    fun set(player: CommandSender, claim: Claim, flag: Flag<*>, role: ClaimRole?, value: Any?) {
        yv.tils.regions.data.Permissions.FLAGS_EDIT.require(player)
        ClaimService.requireOwner(player, claim)
        check(enabled(flag, role)) { FLAG_CHANGED.key }
        val target = ClaimService.target(claim, role)
        val before = target.flags.toMap()
        write(claim, flag, role, value)
        var subzoneRollback: (() -> Unit)? = null
        try {
            subzoneRollback = ClaimSubzones.sync(claim)
            ClaimService.manager(claim.world).saveChanges()
        } catch (e: Exception) {
            subzoneRollback?.invoke()
            target.flags = before
            throw RegionFailure(FLAG_SAVE_FAILED, cause = e)
        }
    }

    fun cycle(value: Any?, flag: Flag<*>): Any = when (flag) {
        is StateFlag -> if (value == StateFlag.State.ALLOW) StateFlag.State.DENY else StateFlag.State.ALLOW
        is BooleanFlag -> value != true
        else -> error(FLAG_INPUT.key)
    }

    fun changed(before: RegionsConfigState, after: RegionsConfigState) =
        all().filterNot(::locked).filter { policy(it, before) != policy(it, after) }

    /** Worlds not loaded during an admin update catch up once they become available. */
    fun catchUp(world: org.bukkit.World) {
        val config = ConfigFile.state
        val applied = config.appliedWorldRevisions[world.uid.toString()] ?: 0
        if (applied >= config.policyRevision) return
        val names = config.policyChanges.filterKeys { it.toInt() > applied }.values.flatten().toSet()
        val flags = all().filter { it.name in names && !locked(it) }
        val manager = ClaimService.manager(world)
        val snapshots =
            mutableListOf<Pair<com.sk89q.worldguard.protection.regions.ProtectedRegion, Map<Flag<*>, Any>>>()
        val subzoneRollbacks = mutableListOf<() -> Unit>()
        try {
            for (claim in ClaimService.claims(world)) {
                listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER).map { ClaimService.target(claim, it) }
                    .forEach { snapshots += it to it.flags.toMap() }
                flags.forEach { reset(claim, it, config) }
                subzoneRollbacks += ClaimSubzones.sync(claim, config = config)
            }
            manager.saveChanges()
            ConfigFile().applyState(config.copy(appliedWorldRevisions = config.appliedWorldRevisions + (world.uid.toString() to config.policyRevision)))
        } catch (e: Exception) {
            subzoneRollbacks.asReversed().forEach { it() }
            snapshots.forEach { (region, values) -> region.flags = values }
            throw RegionFailure(POLICY_SAVE_FAILED, cause = e)
        }
    }

    /** Resets every affected claim; rolls back WorldGuard and configuration on failure. */
    fun propagate(before: RegionsConfigState, after: RegionsConfigState): () -> Unit {
        val changed = changed(before, after)
        val openChanged = before.allowOpenSubzones != after.allowOpenSubzones
        if (changed.isEmpty() && !openChanged) return {}
        val snapshots =
            mutableListOf<Pair<com.sk89q.worldguard.protection.regions.ProtectedRegion, Map<Flag<*>, Any>>>()
        val managers = mutableSetOf<com.sk89q.worldguard.protection.managers.RegionManager>()
        val subzoneRollbacks = mutableListOf<() -> Unit>()
        val rollback = {
            subzoneRollbacks.asReversed().forEach { it() }
            snapshots.forEach { (region, flags) -> region.flags = flags }
            managers.forEach { it.saveChanges() }
        }
        try {
            for (world in Bukkit.getWorlds()) {
                val manager = WorldGuard.getInstance().platform.regionContainer.get(
                    com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world)
                ) ?: continue
                for (claim in ClaimService.claims(world)) {
                    val targets = listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER).map { ClaimService.target(claim, it) }
                    targets.forEach { snapshots += it to it.flags.toMap() }
                    managers += manager
                    changed.forEach { reset(claim, it, after) }
                    subzoneRollbacks += ClaimSubzones.sync(claim, config = after)
                }
            }
            managers.forEach { it.saveChanges() }
        } catch (e: Exception) {
            try {
                rollback()
            } catch (restore: Exception) {
                e.addSuppressed(restore)
            }
            throw RegionFailure(POLICY_SAVE_FAILED, cause = e)
        }
        return rollback
    }
}
