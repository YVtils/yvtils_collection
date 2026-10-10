/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.logic

import org.bukkit.*
import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent
import org.bukkit.entity.*
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.remadeEnderDragon.configs.*
import yv.tils.utils.modules.Core
import yv.tils.utils.logger.Logger
import java.util.UUID

data class FightHistory(var completedWorlds: List<String> = emptyList())
private data class Landing(val world: UUID, val cap: Double, var expires: Long, var airborne: Boolean = false)

class FightManager(val terrain: TemporaryTerrain) : Listener {
    private val fights = mutableMapOf<UUID, DragonFight>()
    private val stopped = mutableSetOf<UUID>()
    private val landings = mutableMapOf<UUID, Landing>()
    private var history = ObjectMapperFileUtils.load("/remade-ender-dragon/history.json", FightHistory())
    private var tick = 0L
    private var task: BukkitTask? = null
    private val key = NamespacedKey(Core.instance, "remade_dragon_entity")
    private val healthKey = NamespacedKey(Core.instance, "remade_dragon_original_health")
    private val rewardedCrystals = mutableSetOf<UUID>()
    private val customDamage = mutableMapOf<UUID, Double>()

    /** Scope piercing to our synchronous scripted hit, never vanilla dragon/melee damage. */
    fun attackDamage(player: Player, amount: Double, dragon: EnderDragon, piercing: Double) {
        val previous = customDamage.put(player.uniqueId, piercing)
        try {
            player.damage(amount, dragon)
        } finally {
            if (previous == null) customDamage.remove(player.uniqueId) else customDamage[player.uniqueId] = previous
        }
    }

    fun enable() {
        terrain.recover()
        Bukkit.getWorlds().forEach { w -> w.entities.forEach(::removeOrphan) }
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            tick += 2
            if (tick % 40L == 0L) discover()
            fights.values.toList().forEach { fight ->
                if (!fight.dragon.isValid || fight.dragon.isDead || fight.dragon.phase == EnderDragon.Phase.DYING) {
                    finish(fight)
                } else fight.tick(tick)
            }
            terrain.expire(tick)
            landings.entries.removeIf { (id, landing) ->
                val p = Bukkit.getPlayer(id)
                if (p == null || p.world.uid != landing.world || p.isDead || tick >= landing.expires) return@removeIf true
                if (!p.isOnGround && !p.isInWater) landing.airborne = true
                landing.airborne && (p.isOnGround || p.isInWater || p.location.block.type == Material.COBWEB)
            }
        }, 2L, 2L)
    }

    private fun allowed(world: World): Boolean = world.environment == World.Environment.THE_END &&
            (ConfigFile.state.worlds.isEmpty() || world.name in ConfigFile.state.worlds)

    private fun discover() {
        val config = ConfigFile.state
        if (!config.enabled || config.activation == Activation.ADMIN) return
        Bukkit.getWorlds().filter(::allowed).forEach { world ->
            if (config.activation == Activation.FIRST && (world.uid.toString() in history.completedWorlds || world.enderDragonBattle?.hasBeenPreviouslyKilled() == true)) return@forEach
            world.getEntitiesByClass(EnderDragon::class.java)
                .filter { it.isValid && !it.isDead && it.phase != EnderDragon.Phase.DYING }.forEach { dragon ->
                if (dragon.uniqueId !in fights && dragon.uniqueId !in stopped) start(dragon)
            }
        }
    }

    fun here(world: World): DragonFight? = fights.values.firstOrNull { it.world == world }

    fun startHere(world: World, preview: Boolean = false): DragonFight? {
        if (!ConfigFile.state.enabled || !allowed(world)) return null
        here(world)?.let { existing ->
            if (!preview && existing.preview) {
                existing.close(); fights.remove(existing.dragon.uniqueId)
            } else return existing
        }
        val dragon = world.getEntitiesByClass(EnderDragon::class.java)
            .firstOrNull { !it.isDead && it.isValid && it.phase != EnderDragon.Phase.DYING } ?: return null
        stopped.remove(dragon.uniqueId)
        return start(dragon, preview)
    }

    private fun start(dragon: EnderDragon, preview: Boolean = false): DragonFight {
        if (here(dragon.world) != null) return here(dragon.world)!!
        val fight = DragonFight(dragon, ConfigFile.state, terrain, this, preview)
        fights[dragon.uniqueId] = fight
        fight.tick(tick)
        Logger.info("[Remade Ender Dragon] Started ${if (preview) "preview" else "encounter"} in ${dragon.world.name}, dragon=${dragon.uniqueId}.")
        return fight
    }

    fun stop(world: World): Boolean {
        val fight = here(world) ?: return false
        stopped += fight.dragon.uniqueId
        fights.remove(fight.dragon.uniqueId)
        fight.close()
        Logger.info("[Remade Ender Dragon] Encounter stopped in ${world.name}.")
        return true
    }

    private fun finish(fight: DragonFight) {
        fights.remove(fight.dragon.uniqueId)
        if (fight.dragon.isDead || fight.dragon.phase == EnderDragon.Phase.DYING) markCompleted(fight.world)
        fight.close()
        rewardedCrystals.clear()
        Logger.info("[Remade Ender Dragon] Encounter finished in ${fight.world.name}.")
    }

    private fun markCompleted(world: World) {
        if (world.uid.toString() !in history.completedWorlds) {
            history.completedWorlds = history.completedWorlds + world.uid.toString()
            ObjectMapperFileUtils.save("/remade-ender-dragon/history.json", history)
        }
    }

    fun reload() {
        ConfigFile.load() // Validate before stopping existing encounters.
        resetEncounters()
    }

    fun applyConfiguration(next: DragonConfig) {
        ConfigFile.applyState(next)
        resetEncounters()
    }

    private fun resetEncounters() {
        fights.values.toList().forEach { it.close() }
        fights.clear()
        rewardedCrystals.clear()
    }

    fun close() {
        task?.cancel()
        fights.values.toList().forEach { it.close() }
        fights.clear()
        terrain.close()
        // Keep outstanding landing caps briefly on module disable; unregistering only this
        // listener would otherwise make the final descent unexpectedly lethal.
        if (landings.isNotEmpty() && Core.instance.isEnabled) {
            Bukkit.getScheduler().runTaskLater(Core.instance, Runnable { landings.clear() }, 200L)
        }
    }

    fun trackLanding(player: Player, cap: Double) {
        val landing = landings.getOrPut(player.uniqueId) { Landing(player.world.uid, cap, tick + 200) }
        landing.expires = tick + 200
    }

    private fun owner(entity: Entity) = fights.values.firstOrNull { it.owns(entity) }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun damage(event: EntityDamageEvent) {
        val p = event.entity as? Player ?: return
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            landings.remove(p.uniqueId)?.let { landing ->
                if (p.world.uid == landing.world && tick < landing.expires)
                    event.damage = FightMath.landingDamage(event.damage, landing.cap, p.health)
            }
        }
        here(p.world)?.let { fight ->
            if (p in fight.players() && event.cause != EntityDamageEvent.DamageCause.VOID)
                event.damage *= fight.protection(p)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun mountDamage(event: EntityDamageEvent) {
        val fight = owner(event.entity) ?: return
        if (!fight.isMount(event.entity)) return
        val accepted = !event.isCancelled && event.damage > 0.0
        val attacker = when (val damager = (event as? EntityDamageByEntityEvent)?.damager) {
            is Player -> damager
            is Projectile -> damager.shooter as? Player
            else -> null
        }
        // Carrier suffocation, dragon contact, fire and explosion chains must not
        // count as the requested one-hit player interaction.
        event.isCancelled = true
        if (accepted && attacker != null && attacker in fight.players()) fight.hitMount(event.entity, attacker)
    }

    @Suppress("DEPRECATION") // Bukkit's event modifiers preserve cancellation, armor wear and absorption.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun pierceProtection(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val piercing = customDamage[player.uniqueId] ?: return
        if (piercing <= 0.0) return
        val modifier = EntityDamageEvent.DamageModifier::class.java
        val armor = EntityDamageEvent.DamageModifier.ARMOR
        val magic = EntityDamageEvent.DamageModifier.MAGIC
        val resistance = EntityDamageEvent.DamageModifier.RESISTANCE
        val absorption = EntityDamageEvent.DamageModifier.ABSORPTION
        // Resistance retains its reduction fraction as armor damage increases.
        val oldResistance = if (event.isApplicable(resistance)) event.getDamage(resistance) else 0.0
        val oldBeforeResistance = event.damage + listOf(
            EntityDamageEvent.DamageModifier.HARD_HAT, EntityDamageEvent.DamageModifier.BLOCKING, armor,
        ).sumOf { if (event.isApplicable(it)) event.getDamage(it) else 0.0 }
        val oldMagic = if (event.isApplicable(magic)) event.getDamage(magic) else 0.0
        val oldBeforeMagic = oldBeforeResistance + oldResistance
        if (event.isApplicable(armor)) event.setDamage(
            armor,
            FightMath.piercedReduction(event.getDamage(armor), piercing)
        )
        if (event.isApplicable(resistance) && oldBeforeResistance > 0.0) {
            val beforeResistance = event.damage + listOf(
                EntityDamageEvent.DamageModifier.HARD_HAT, EntityDamageEvent.DamageModifier.BLOCKING, armor,
            ).sumOf { if (event.isApplicable(it)) event.getDamage(it) else 0.0 }
            event.setDamage(resistance, oldResistance * beforeResistance / oldBeforeResistance)
        }
        if (event.isApplicable(magic) && oldBeforeMagic > 0.0) {
            val beforeMagic = event.damage + listOf(
                EntityDamageEvent.DamageModifier.HARD_HAT, EntityDamageEvent.DamageModifier.BLOCKING, armor, resistance,
            ).sumOf { if (event.isApplicable(it)) event.getDamage(it) else 0.0 }
            event.setDamage(magic, FightMath.piercedReduction(oldMagic * beforeMagic / oldBeforeMagic, piercing))
        }
        if (event.isApplicable(absorption)) {
            val beforeAbsorption = modifier.enumConstants.filter { it != absorption }
                .sumOf { if (event.isApplicable(it)) event.getDamage(it) else 0.0 }.coerceAtLeast(0.0)
            event.setDamage(absorption, -minOf(player.absorptionAmount, beforeAbsorption))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun entityDamage(event: EntityDamageByEntityEvent) {
        val fight = owner(event.entity) ?: here(event.entity.world) ?: return
        if (fight.isFocus(event.entity)) {
            val attacker = when (val damager = event.damager) {
                is Player -> damager
                is Projectile -> damager.shooter as? Player
                else -> null
            }
            if (attacker == null || attacker !in fight.players()) event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun dragonDamage(event: EntityDamageEvent) {
        val fight = here(event.entity.world) ?: return
        if (event.entity.uniqueId == fight.dragon.uniqueId || (event.entity as? ComplexEntityPart)?.parent?.uniqueId == fight.dragon.uniqueId)
            event.damage *= fight.dragonDamageMultiplier()
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun dragonHeal(event: EntityRegainHealthEvent) {
        val fight = here(event.entity.world) ?: return
        if (event.entity.uniqueId == fight.dragon.uniqueId) event.amount *= fight.healthConversion()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun focusEnvironmentDamage(event: EntityDamageEvent) {
        val fight = owner(event.entity) ?: return
        if (fight.isFocus(event.entity) && event !is EntityDamageByEntityEvent) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun crystalDamage(event: EntityDamageByEntityEvent) {
        val crystal = event.entity as? EnderCrystal ?: return
        val player = when (val damager = event.damager) {
            is Player -> damager
            is Projectile -> damager.shooter as? Player
            else -> null
        } ?: return
        val fight = here(crystal.world) ?: return
        if (player !in fight.players()) return
        if (!rewardedCrystals.add(crystal.uniqueId)) return
        // Reward only a crystal that was actually destroyed by the accepted hit.
        val at = crystal.location.clone()
        Bukkit.getScheduler().runTask(Core.instance, Runnable {
            if (!crystal.isValid && here(crystal.world) === fight) fight.crystalReward(at)
            else rewardedCrystals.remove(crystal.uniqueId)
        })
    }

    @EventHandler(ignoreCancelled = true)
    fun explosion(event: EntityExplodeEvent) {
        event.blockList().removeIf { terrain.contains(it) || terrain.contains(it.getRelative(0, 1, 0)) }
        if (owner(event.entity) != null) {
            event.blockList().clear()
            event.yield = 0f
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun prime(event: ExplosionPrimeEvent) {
        owner(event.entity)?.let { fight ->
            event.fire = false
            if (fight.isControlledProjectile(event.entity) || fight.isMount(event.entity)) event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun projectileHit(event: ProjectileHitEvent) {
        owner(event.entity)?.let { fight ->
            if (fight.isControlledProjectile(event.entity)) {
                event.isCancelled = true
                fight.impactFireball(event.entity)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun fireballCloud(event: EnderDragonFireballHitEvent) {
        if (owner(event.entity)?.isControlledProjectile(event.entity) == true) {
            event.isCancelled = true
            event.areaEffectCloud.remove()
        }
    }

    @EventHandler
    fun died(event: EntityDeathEvent) {
        val dragon = event.entity as? EnderDragon
        if (dragon != null && allowed(dragon.world)) {
            markCompleted(dragon.world)
            fights[dragon.uniqueId]?.let(::finish)
            stopped.remove(dragon.uniqueId)
            return
        }
        val fight = owner(event.entity) ?: return
        event.drops.clear()
        event.droppedExp = 0
        fight.summonDied(event.entity, event.entity.killer)
    }

    @EventHandler(ignoreCancelled = true)
    fun cloud(event: AreaEffectCloudApplyEvent) {
        val dragon = event.entity.source as? EnderDragon ?: return
        val fight = fights[dragon.uniqueId] ?: return
        if (fight.config.replaceVanillaBreath) {
            event.isCancelled = true; event.entity.remove()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun changeBlock(event: EntityChangeBlockEvent) {
        if (owner(event.entity) != null || terrain.contains(event.block)) event.isCancelled = true
    }


    @EventHandler(ignoreCancelled = true)
    fun teleport(event: EntityTeleportEvent) {
        if (event.entity is Enderman && owner(event.entity)?.isBlue(event.entity) == true) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun slimeSplit(event: SlimeSplitEvent) {
        if (owner(event.entity)?.isFocus(event.entity) == true) event.isCancelled = true
    }

    @EventHandler
    fun chunkLoad(event: ChunkLoadEvent) {
        event.chunk.entities.forEach(::removeOrphan)
    }

    private fun removeOrphan(entity: Entity) {
        if (entity is EnderDragon && entity.uniqueId !in fights) {
            entity.persistentDataContainer.get(healthKey, PersistentDataType.DOUBLE)?.let { original ->
                val attribute = entity.getAttribute(Attribute.MAX_HEALTH) ?: return@let
                val ratio = entity.health / attribute.value
                attribute.baseValue = original
                if (!entity.isDead) entity.health = (original * ratio).coerceIn(0.01, attribute.value)
                entity.persistentDataContainer.remove(healthKey)
            }
        }
        if (entity.persistentDataContainer.has(key, PersistentDataType.STRING) && owner(entity) == null) entity.remove()
    }

    @EventHandler(ignoreCancelled = true)
    fun unload(event: WorldUnloadEvent) {
        fights.values.filter { it.world == event.world }.toList().forEach {
            fights.remove(it.dragon.uniqueId); it.close()
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        landings.remove(event.player.uniqueId)
    }

    @EventHandler
    fun changedWorld(event: PlayerChangedWorldEvent) {
        landings.remove(event.player.uniqueId)
    }
}
