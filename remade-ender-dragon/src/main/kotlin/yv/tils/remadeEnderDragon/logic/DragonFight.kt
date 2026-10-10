/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.logic

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector
import org.bukkit.util.Transformation
import org.joml.Vector3f
import org.joml.Quaternionf
import yv.tils.remadeEnderDragon.configs.*
import yv.tils.remadeEnderDragon.language.Messages
import yv.tils.utils.modules.Core
import yv.tils.utils.logger.Logger
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.player.PlayerUtils.Companion.isSupported
import org.bukkit.command.CommandSender
import java.util.UUID
import kotlin.math.*
import kotlin.random.Random

private data class RunningEffect(val end: Long, val attack: Attack?, val tick: (Long) -> Unit, val cleanup: () -> Unit)
private data class Summon(val entity: LivingEntity, val expires: Long, val blue: Boolean, var nextWave: Long)
private data class Protection(val center: Location, val radius: Double, val end: Long)
private data class CrystalMount(
    val carrier: Silverfish,
    val crystal: EnderCrystal,
    val target: Location?,
    val end: Long
)

class DragonFight(
    val dragon: EnderDragon,
    val config: DragonConfig,
    private val terrain: TemporaryTerrain,
    private val manager: FightManager,
    val preview: Boolean = false,
) {
    val world: World = dragon.world
    private val healthKey = NamespacedKey(Core.instance, "remade_dragon_original_health")
    private val originalMaxHealth = dragon.persistentDataContainer.get(healthKey, PersistentDataType.DOUBLE)
        ?: dragon.getAttribute(Attribute.MAX_HEALTH)!!.baseValue
    private val effects = mutableListOf<RunningEffect>()
    private val summons = mutableMapOf<UUID, Summon>()
    private val crystals = mutableSetOf<UUID>()
    private val mounts = mutableListOf<CrystalMount>()
    private val visuals = mutableMapOf<UUID, Entity>()
    private val fireballImpacts = mutableMapOf<UUID, () -> Unit>()
    private val focuses = mutableSetOf<UUID>()
    private val anchors = mutableSetOf<UUID>()
    private val markedLastUse = mutableMapOf<UUID, Long>()
    private val lastHit = mutableMapOf<UUID, Long>()
    private var castingAttack: Attack? = null
    private var lastAttack: Attack? = null
    private var phase = 0
    private var recoveryUntil = 0L
    private var vulnerabilityUntil = 0L
    private var empowermentUntil = 0L
    private var empowerment = 0.0
    private var waveCasts = 0
    private var scaledHealth = originalMaxHealth
    private val blueTeams = mutableMapOf<org.bukkit.scoreboard.Scoreboard, Team>()
    private val protections = mutableListOf<Protection>()
    private val uses = mutableMapOf<Attack, Int>()
    private val lastUse = mutableMapOf<Attack, Long>()
    private val updraftUses = mutableMapOf<UUID, Int>()
    private val grounds = mutableMapOf<Pair<Int, Int>, Int>()
    private var scanIndex = 0
    private var nextAttack = 600L
    private var offensiveUses = 0
    private var supportCursor = 0
    private var poolCount = 0
    private var initialized = false
    var now = 0L
        private set
    private var closed = false
    private val key = NamespacedKey(Core.instance, "remade_dragon_entity")

    fun players(): List<Player> = world.players.filter {
        !it.isDead && (it.gameMode == GameMode.SURVIVAL || it.gameMode == GameMode.ADVENTURE) &&
                horizontal(it.location, center()) <= config.islandRadius && it.location.y >= world.minHeight
    }

    private fun center() = Location(world, config.centerX, 64.0, config.centerZ)
    private fun count(s: AttackSettings) =
        FightMath.count(players().size, config.maxScalingPlayers, s.baseCount, s.extraCountPerPlayer, s.maxCount)

    private fun seconds(value: Double) = ceil(value * 20).toLong()
    private fun fraction() = dragon.health / dragon.getAttribute(Attribute.MAX_HEALTH)!!.value

    fun tick(tick: Long) {
        if (closed) return
        now = tick
        if (!initialized) {
            initialized = true
            nextAttack = tick + 600
            if (!preview) dragon.persistentDataContainer.set(healthKey, PersistentDataType.DOUBLE, originalMaxHealth)
        }
        scanGround()
        mounts.toList().forEach { mount ->
            if (!mount.carrier.isValid || !mount.crystal.isValid) {
                removeMount(mount)
            } else if (tick >= mount.end) {
                removeMount(mount)
            } else if (mount.target != null && horizontal(
                    mount.carrier.location,
                    mount.target
                ) <= config.crystalConvergenceRadius
            ) {
                empowerment = min(
                    config.maxCrystalEmpowerment,
                    (if (now < empowermentUntil) empowerment else 0.0) + config.crystalEmpowermentPerArrival
                )
                empowermentUntil = now + seconds(config.crystalEmpowermentSeconds)
                cue(mount.target, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.7f)
                removeMount(mount)
            } else if (tick % 10L == 0L) {
                // Paper pathfinding follows the dragon's projected ground position.
                val projected = mount.target ?: surface(dragon.location.blockX, dragon.location.blockZ)
                ?: grounds.keys.minByOrNull { hypot(it.first - dragon.location.x, it.second - dragon.location.z) }
                    ?.let { ground(it.first, it.second) }
                projected?.let {
                    mount.carrier.pathfinder.moveTo(it, config.crystalCarrierSpeed)
                }
                mount.target?.let { circle(it, config.crystalConvergenceRadius, Color.PURPLE) }
            }
        }
        syncBlueTeams()
        // Expire before processing damage, so markers and effects have the same lifetime.
        effects.toList().forEach {
            if (tick >= it.end) {
                effects.remove(it); it.cleanup()
                if (it.attack != null && it.attack !in AttackDirector.support && it.attack != Attack.RIFT_ANCHORS)
                    recoveryUntil = max(recoveryUntil, tick + seconds(config.recoverySeconds))
            } else it.tick(tick)
        }
        protections.removeIf { tick >= it.end }
        summons.values.toList().forEach { summon ->
            if (tick % 20L == 0L && summon.entity is Mob) {
                summon.entity.target = players().minByOrNull { horizontal(it.location, summon.entity.location) }
            }
            if (!summon.entity.isValid || summon.entity.isDead || tick >= summon.expires) {
                removeBlueEntry(summon.entity)
                if (summon.entity.isValid) summon.entity.remove()
                summons.remove(summon.entity.uniqueId)
            } else if (summon.blue && tick >= summon.nextWave && players().isNotEmpty()) {
                summon.nextWave = tick + seconds(config.blueEndermanWaveCooldownSeconds)
                endermanWave(summon.entity as Enderman)
            }
        }
        val players = players()
        if (players.isEmpty()) {
            nextAttack = tick + 40; return
        }
        if (!preview && tick % 20L == 0L) rescale(players.size)
        if (preview) return
        val nextPhase = AttackDirector.phase(phase, fraction(), config.middlePhaseHealth, config.finalPhaseHealth)
        if (nextPhase != phase) {
            phase = nextPhase
            recoveryUntil = max(recoveryUntil, tick + seconds(config.phaseRecoverySeconds))
            cue(dragon.location, Sound.ENTITY_ENDER_DRAGON_GROWL, if (phase == 1) 0.8f else 0.6f)
            if (config.subtleCues) world.spawnParticle(Particle.PORTAL, dragon.location, 45, 2.0, 1.0, 2.0, 0.1)
        }
        if (tick < recoveryUntil) return
        if (fraction() < 0.5 && available(Attack.CRYSTALS) && suitable(Attack.CRYSTALS)) {
            trigger(Attack.CRYSTALS)
        }
        if (tick >= nextAttack && hostileEffects() < config.maxConcurrentAttacks) {
            val eligible = Attack.entries.filter {
                it !in AttackDirector.support && it != Attack.CRYSTALS && available(it) && suitable(it)
            }
            if (eligible.isNotEmpty()) {
                val attack = AttackDirector.choose(eligible, lastAttack, lastUse)!!
                if (trigger(attack)) {
                    lastAttack = attack
                    offensiveUses++
                    if (offensiveUses % config.supportEveryAttacks == 0) {
                        val support = if (supportCursor++ % 2 == 0) Attack.SANCTUARY else Attack.RESCUE_UPDRAFT
                        if (available(support)) trigger(support)
                    }
                }
            }
            val pace = when (phase) {
                1 -> config.middlePhaseIntervalMultiplier; 2 -> config.finalPhaseIntervalMultiplier; else -> 1.0
            }
            nextAttack = tick + seconds(
                max(
                    config.minimumAttackIntervalSeconds,
                    (config.attackIntervalSeconds - (players.size.coerceAtMost(config.maxScalingPlayers) - 1) * config.intervalReductionPerExtraPlayer) * pace
                )
            )
        }
    }

    private fun rescale(n: Int) {
        val attribute = dragon.getAttribute(Attribute.MAX_HEALTH) ?: return
        val hp = FightMath.health(
            n,
            config.maxScalingPlayers,
            config.baseHealth,
            config.healthPerExtraPlayer,
            config.maxHealth
        )
        scaledHealth = hp
        // Keep the real attribute below platform caps; damage conversion supplies effective HP.
        val visibleHealth = min(hp, 1024.0)
        if (attribute.baseValue != visibleHealth) {
            val ratio = fraction().coerceIn(0.0, 1.0)
            attribute.baseValue = visibleHealth
            dragon.health = (attribute.value * ratio).coerceIn(0.01, attribute.value)
        }
    }

    private fun available(attack: Attack): Boolean {
        val s = config.settings(attack)
        return s.enabled && AttackDirector.budgetAvailable(
            uses.getOrDefault(attack, 0),
            s.maxUses,
            s.sustainAfterBudget
        ) &&
                AttackDirector.phaseAllows(attack, phase) &&
                now - lastUse.getOrDefault(attack, -100000L) >= seconds(s.cooldownSeconds) &&
                fraction() in s.minHealthFraction..s.maxHealthFraction
    }

    private fun activeAttacks() = effects.mapNotNull { it.attack }.toSet()
    private fun hostileEffects() = effects.count { it.attack !in AttackDirector.support }

    private fun suitable(attack: Attack): Boolean {
        if (!AttackDirector.compatible(attack, activeAttacks())) return false
        if (attack == Attack.MAGNETISM && (dragon.phase != EnderDragon.Phase.CIRCLING || anchors.isNotEmpty() || mounts.isNotEmpty())) return false
        if (attack == Attack.DRAGON_WAVE && players().none {
                horizontal(it.location, dragon.location) < config.settings(
                    attack
                ).radius
            }) return false
        if (attack == Attack.BREATH_SWEEP && (surface(dragon.location.blockX, dragon.location.blockZ) == null ||
                    players().none { horizontal(it.location, dragon.location) in 5.0..config.settings(attack).radius })
        ) return false
        if (attack in setOf(Attack.MONSTERS, Attack.BLUE_ENDERMEN) && summons.size >= config.maxSummons) return false
        if (attack == Attack.CRYSTALS && mounts.isNotEmpty()) return false
        return true
    }

    /** Debug bypasses cooldown/phase/use budget, but retains caps and finite durations. */
    fun trigger(attack: Attack, debug: Boolean = false): Boolean {
        val support = attack in AttackDirector.support
        if (closed || players().isEmpty() || (!debug && (!available(attack) || !suitable(attack)))) return false
        if (support && effects.count { it.attack in AttackDirector.support && it.attack != Attack.HEALING_POOL } >= 2) return false
        if (!support && hostileEffects() >= config.maxConcurrentAttacks + (if (debug) 2 else 0)) return false
        if (attack == Attack.MAGNETISM && dragon.phase != EnderDragon.Phase.CIRCLING && !debug) return false
        if (grounds.isEmpty() && attack !in listOf(
                Attack.MAGNETISM,
                Attack.EFFECT_AREAS,
                Attack.EXPLOSIVES,
                Attack.ISLAND_WAVE,
                Attack.DRAGON_WAVE,
                Attack.SUMMON_AREAS
            )
        ) return false
        val s = config.settings(attack).copy()
        val crystalLocations = if (attack == Attack.CRYSTALS) crystalTargets(s) else emptyList()
        if (attack !in setOf(
                Attack.ISLAND_WAVE,
                Attack.DRAGON_WAVE,
                Attack.INFESTATION,
                Attack.MAGNETISM,
                Attack.BREATH_SWEEP
            ) && count(s) == 0
        ) return false
        if (attack in setOf(
                Attack.CRYSTALS,
                Attack.MONSTERS,
                Attack.BLUE_ENDERMEN,
                Attack.RIFT_ANCHORS,
                Attack.SANCTUARY,
                Attack.RESCUE_UPDRAFT,
                Attack.HEALING_POOL
            ) && groundTargets(1).isEmpty()
        ) return false
        if (attack == Attack.MARKED_HUNTERS && markCandidates().isEmpty()) return false
        if (attack == Attack.BREATH_SWEEP && sweepTarget(s) == null) return false
        if (attack == Attack.CRYSTALS && crystalLocations.isEmpty()) return false
        if (attack == Attack.MARKED_HUNTERS && markCandidates().none {
                surface(
                    it.location.blockX,
                    it.location.blockZ
                ) != null
            }) return false
        Logger.debug(
            "[Remade Ender Dragon] ${world.name}: ${attack.id}, fighters=${players().size}, preview=$debug",
            DEBUG_LEVEL.DETAILED
        )
        if (!debug) {
            uses[attack] = uses.getOrDefault(attack, 0) + 1; lastUse[attack] = now
        }
        announce(attack.id)
        cue(
            dragon.location, when (attack) {
                Attack.MAGNETISM -> Sound.BLOCK_BEACON_AMBIENT
                Attack.BREATH_SWEEP -> Sound.ENTITY_ENDER_DRAGON_SHOOT
                else -> Sound.ENTITY_ENDER_DRAGON_FLAP
            }, 0.8f
        )
        castingAttack = attack
        try {
            when (attack) {
                Attack.CRYSTALS -> renewCrystals(s, crystalLocations)
                Attack.ISLAND_WAVE -> dragonWave(
                    center(),
                    config.islandRadius + 2,
                    s,
                    config.waveSpeedBlocksPerSecond,
                    debug
                )

                Attack.DRAGON_WAVE -> dragonWave(
                    dragon.location.clone(),
                    s.radius,
                    s,
                    config.dragonWaveSpeedBlocksPerSecond,
                    debug
                )

                Attack.EFFECT_AREAS -> effectAreas(s)
                Attack.EXPLOSIVES, Attack.SUMMON_AREAS -> strikes(s, attack == Attack.SUMMON_AREAS)
                Attack.MONSTERS, Attack.BLUE_ENDERMEN -> summonWave(s, attack == Attack.BLUE_ENDERMEN)
                Attack.INFESTATION -> infestation(s)
                Attack.MAGNETISM -> magnetism(s)
                Attack.MARKED_HUNTERS -> markedHunters(s)
                Attack.RIFT_ANCHORS -> riftAnchors(s)
                Attack.BREATH_SWEEP -> breathSweep(s)
                Attack.SANCTUARY -> sanctuary(s)
                Attack.RESCUE_UPDRAFT -> updraft(s)
                Attack.HEALING_POOL -> groundTargets(1).forEach { healingPool(it) }
            }
        } finally {
            castingAttack = null
        }
        return true
    }

    fun sendStatus(sender: CommandSender) {
        Messages.send(
            sender, "status", mapOf(
                "players" to players().size, "health" to (fraction() * scaledHealth).roundToInt(),
                "maximum" to scaledHealth.roundToInt(), "hazards" to effects.size, "summons" to summons.size
            )
        )
        uses.forEach { (attack, amount) ->
            Messages.send(
                sender,
                "status-use",
                mapOf("attack" to Messages.plain("attack.${attack.id}", sender), "uses" to amount)
            )
        }
        Messages.send(
            sender,
            "status-phase",
            mapOf(
                "phase" to phase + 1,
                "empowerment" to (if (now < empowermentUntil) empowerment * 100 else 0.0).roundToInt()
            )
        )
    }

    private fun announce(id: String) {
        if (!config.announceAttacks) return
        players().forEach { p ->
            Messages.actionBar(p, "warning", mapOf("attack" to Messages.plain("attack.$id", p)))
        }
    }

    private fun effect(end: Long, cleanup: () -> Unit = {}, tick: (Long) -> Unit) {
        effects += RunningEffect(end, castingAttack, tick, cleanup)
    }

    private fun cue(at: Location, sound: Sound, pitch: Float = 1f) {
        if (config.subtleCues) world.playSound(at, sound, 0.7f, pitch)
    }

    private fun damage(player: Player, amount: Double) {
        if (player !in players()) return
        if (now - lastHit.getOrDefault(player.uniqueId, -100000L) < seconds(config.customHitRecoverySeconds)) return
        lastHit[player.uniqueId] = now
        val multiplier = min(
            config.maxDamageMultiplier,
            1 + (players().size.coerceIn(1, config.maxScalingPlayers) - 1) * config.damagePerExtraPlayer
        )
        manager.attackDamage(
            player,
            amount * multiplier * (1 + if (now < empowermentUntil) empowerment else 0.0),
            dragon,
            config.customAttackProtectionPiercing
        )
        Logger.debug(
            "[Remade Ender Dragon] ${player.name} custom hit: base=$amount, scaled=${amount * multiplier}, hp=${player.health}",
            DEBUG_LEVEL.DETAILED
        )
    }

    fun protection(player: Player): Double = if (protections.any {
            now < it.end && nearby(
                player,
                it.center,
                it.radius
            )
        }) config.sanctuaryDamageMultiplier else 1.0

    fun dragonDamageMultiplier(): Double {
        return healthConversion() * if (now < vulnerabilityUntil) config.anchorVulnerabilityMultiplier else 1.0
    }

    fun healthConversion() = if (preview) 1.0 else FightMath.healthConversion(
        dragon.getAttribute(Attribute.MAX_HEALTH)!!.value,
        scaledHealth
    )

    private fun horizontal(a: Location, b: Location) = hypot(a.x - b.x, a.z - b.z)
    private fun nearby(player: Player, location: Location, radius: Double) =
        player.world == world && horizontal(
            player.location,
            location
        ) <= radius && abs(player.location.y - location.y) < 3.0

    private fun ground(x: Int, z: Int): Location? {
        if (!world.isChunkLoaded(x shr 4, z shr 4)) return null
        val y = grounds[x to z] ?: return null
        if (!world.getBlockAt(x, y, z).type.isSolid) return null
        return Location(world, x + 0.5, y + 1.0, z + 0.5)
    }

    /** Resolve combat surfaces on demand, including towers, bedrock and player platforms.
     * The End-stone-only cache is for infestation, not a prerequisite for being hit. */
    private fun surface(x: Int, z: Int, ceiling: Int = config.groundMaxY): Location? {
        if (!world.isChunkLoaded(x shr 4, z shr 4)) return null
        for (y in min(
            ceiling,
            config.groundMaxY
        ).coerceAtMost(world.maxHeight - 2) downTo config.groundMinY.coerceAtLeast(world.minHeight)) {
            val b = world.getBlockAt(x, y, z)
            if (b.type.isSolid && b.getRelative(0, 1, 0).isPassable)
                return Location(world, x + 0.5, y + 1.0, z + 0.5)
        }
        return null
    }

    private fun playerTargets(amount: Int): List<Location> = players().shuffled().take(amount).map { p ->
        surface(p.location.blockX, p.location.blockZ) ?: p.location.clone()
    }

    private fun <T : Entity> visual(at: Location, type: Class<T>, role: String): T = world.spawn(at, type).also {
        it.isPersistent = false
        it.persistentDataContainer.set(key, PersistentDataType.STRING, role)
        visuals[it.uniqueId] = it
    }

    private fun removeVisual(entity: Entity) {
        visuals.remove(entity.uniqueId); entity.remove()
    }

    private fun display(at: Location, material: Material, width: Float = 1f, height: Float = 0.5f): BlockDisplay =
        visual(at, BlockDisplay::class.java, "display").also {
            it.block = material.createBlockData()
            it.brightness = Display.Brightness(15, 15)
            it.viewRange = 2f
            it.teleportDuration = 2
            it.transformation = Transformation(
                Vector3f(-width / 2, 0f, -width / 2),
                Quaternionf(),
                Vector3f(width, height, width),
                Quaternionf()
            )
        }

    /** Incrementally scan loaded terrain; never force-load the island or outer End chunks. */
    private fun scanGround() {
        val r = ceil(config.islandRadius).toInt()
        val width = r * 2 + 1
        repeat(160) {
            val i = scanIndex++ % (width * width)
            val x = config.centerX.toInt() - r + i % width
            val z = config.centerZ.toInt() - r + i / width
            if (hypot(x - config.centerX, z - config.centerZ) > config.islandRadius || !world.isChunkLoaded(
                    x shr 4,
                    z shr 4
                )
            ) return@repeat
            grounds.remove(x to z)
            for (y in config.groundMaxY.coerceAtMost(world.maxHeight - 2) downTo config.groundMinY.coerceAtLeast(world.minHeight)) {
                val b = world.getBlockAt(x, y, z)
                if (b.type == Material.END_STONE && (b.getRelative(
                        0,
                        1,
                        0
                    ).isPassable || terrain.contains(b.getRelative(0, 1, 0)))
                ) {
                    grounds[x to z] = y
                    break
                }
            }
        }
        // Seed around fighters so previews work before the full island scan completes.
        players().forEach { p ->
            val x = p.location.blockX;
            val z = p.location.blockZ
            if (x to z !in grounds) {
                for (y in min(config.groundMaxY, p.location.blockY) downTo config.groundMinY) {
                    if (world.getBlockAt(x, y, z).type == Material.END_STONE) {
                        grounds[x to z] = y; break
                    }
                }
            }
        }
    }

    private fun groundTargets(amount: Int): List<Location> {
        val candidates = grounds.keys.shuffled().mapNotNull { ground(it.first, it.second) }
        val result = mutableListOf<Location>()
        // Target some player locations, then spread the rest across the island.
        players().shuffled().take(amount / 2)
            .forEach { p -> ground(p.location.blockX, p.location.blockZ)?.let { result += it } }
        candidates.forEach { if (result.size < amount && result.all { old -> horizontal(old, it) > 7 }) result += it }
        return result.take(amount)
    }

    private fun dust(location: Location, color: Color, count: Int = 1) {
        if (world.isChunkLoaded(location.blockX shr 4, location.blockZ shr 4))
            world.spawnParticle(Particle.DUST, location, count, 0.1, 0.05, 0.1, 0.0, Particle.DustOptions(color, 1.3f))
    }

    private fun circle(at: Location, radius: Double, color: Color, cone: Vector? = null) {
        val points = (radius * 8).toInt().coerceIn(16, 160)
        repeat(points) { i ->
            val angle = i * 2 * PI / points
            val dx = cos(angle) * radius;
            val dz = sin(angle) * radius
            if (cone != null && !FightMath.inCone(dx, dz, cone.x, cone.z, radius + 0.01)) return@repeat
            val pos = surface(floor(at.x + dx).toInt(), floor(at.z + dz).toInt()) ?: return@repeat
            dust(pos.add(0.0, 0.15, 0.0), color)
        }
    }

    private fun dragonWave(origin: Location, radius: Double, s: AttackSettings, speed: Double, debug: Boolean) {
        val echo = config.echoWavesEnabled && (debug || phase == 2 && ++waveCasts % 2 == 0)
        wave(origin, radius, s, speed = speed)
        if (echo) wave(
            origin, radius, s.copy(damage = s.damage * config.echoWaveDamageMultiplier), speed = speed,
            delay = seconds(config.echoWaveGapSeconds)
        )
    }

    private fun wave(
        origin: Location,
        radius: Double,
        s: AttackSettings,
        cone: Vector? = null,
        speed: Double = config.waveSpeedBlocksPerSecond,
        delay: Long = 0L
    ) {
        val warningAt = now + delay
        val start = now + seconds(s.warningSeconds) + delay
        val end = start + seconds(radius / speed) + 10
        val hit = mutableSetOf<UUID>()
        val segments = if (cone == null) min(config.waveDisplaySegments, max(32, ceil(radius * 5).toInt())) else 0
        val displays = mutableListOf<BlockDisplay>()
        repeat(segments) { displays += display(origin, Material.ORANGE_CONCRETE) }
        var previousFront = 0.0
        effect(end, cleanup = { displays.forEach(::removeVisual) }) { tick ->
            if (tick < warningAt) {
                displays.forEach { it.teleport(origin.clone().add(0.0, -200.0, 0.0)) }
                return@effect
            }
            if (tick < start && tick % 20L == 0L) cue(
                origin,
                Sound.BLOCK_NOTE_BLOCK_BASEDRUM,
                if (delay > 0) 1.3f else 0.8f
            )
            val front = if (tick < start) 2.0 else min(radius, (tick - start) / 20.0 * speed)
            displays.forEachIndexed { i, block ->
                val angle = i * 2 * PI / segments
                val x = origin.x + cos(angle) * front;
                val z = origin.z + sin(angle) * front
                val at = surface(floor(x).toInt(), floor(z).toInt())
                if (at == null) {
                    block.teleport(origin.clone().add(0.0, -200.0, 0.0))
                } else {
                    at.x = x; at.z = z
                    block.teleport(at)
                    block.block =
                        (if (tick < start) Material.ORANGE_CONCRETE else Material.MAGMA_BLOCK).createBlockData()
                    val width = max(0.8, 2 * PI * front / segments * 1.1).toFloat()
                    block.transformation = Transformation(
                        Vector3f(-width / 2, 0f, -width / 2),
                        Quaternionf(),
                        Vector3f(width, 0.45f, width),
                        Quaternionf()
                    )
                }
            }
            if (tick < start) {
                if (cone != null) circle(origin, min(radius, 3.0), Color.ORANGE, cone)
            } else {
                if (cone != null) circle(origin, front, Color.RED, cone)
                players().forEach { p ->
                    val dx = p.location.x - origin.x;
                    val dz = p.location.z - origin.z
                    val distance = hypot(dx, dz)
                    val floor = surface(p.location.blockX, p.location.blockZ, p.location.blockY) ?: return@forEach
                    if (FightMath.crossedWave(distance, previousFront, front, radius) &&
                        p.location.y - floor.y < config.waveJumpHeight && p.location.y >= floor.y - 0.5 &&
                        (cone == null || FightMath.inCone(dx, dz, cone.x, cone.z, radius)) && hit.add(p.uniqueId)
                    ) damage(p, s.damage)
                }
                previousFront = front
            }
        }
    }

    private fun effectAreas(s: AttackSettings) {
        val targets = playerTargets(count(s))
        val launchAt = now + seconds(s.warningSeconds)
        val flightDeadline = launchAt + 200
        val cloudDuration = s.durationSeconds * if (anchors.isNotEmpty()) config.anchorCloudDurationMultiplier else 1.0
        val end = flightDeadline + seconds(cloudDuration)
        val type = potion(config.effectPool.random()) ?: PotionEffectType.SLOWNESS
        val fireballs = mutableListOf<DragonFireball>()
        val clouds = mutableListOf<AreaEffectCloud>()
        val active = mutableListOf<Pair<Location, Long>>()
        var launched = false
        effect(end, cleanup = {
            fireballs.forEach { fireballImpacts.remove(it.uniqueId); removeVisual(it) }
            clouds.forEach(::removeVisual)
        }) { tick ->
            if (tick >= launchAt && !launched) {
                launched = true
                val launch = dragon.location.clone()
                targets.forEach { target ->
                    val delta = target.toVector().subtract(launch.toVector())
                    val direction = if (delta.lengthSquared() > 0.001) delta.normalize() else Vector(0.0, -1.0, 0.0)
                    val ball = visual(
                        launch.clone().add(direction.clone().multiply(3)),
                        DragonFireball::class.java,
                        "fireball"
                    )
                    ball.shooter = dragon
                    ball.setGravity(false)
                    // Native acceleration/drag and client prediction: no teleport loop.
                    ball.acceleration = direction.clone().multiply(0.1)
                    ball.velocity = direction.clone().multiply(0.6)
                    fireballs += ball
                    fireballImpacts[ball.uniqueId] = {
                        val at = surface(ball.location.blockX, ball.location.blockZ) ?: ball.location.clone()
                        removeVisual(ball)
                        active += at to (now + seconds(cloudDuration))
                        clouds += visual(at, AreaEffectCloud::class.java, "effect-cloud").also {
                            it.radius = s.radius.toFloat()
                            it.duration = seconds(cloudDuration).toInt()
                            it.waitTime = 0
                            it.radiusPerTick = 0f
                            // Paper 26.1.2 requires Float data for DRAGON_BREATH.
                            it.setParticle(Particle.DRAGON_BREATH, 1.0f)
                        }
                        world.playSound(at, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5f, 1f)
                    }
                }
            }
            if (tick >= flightDeadline) fireballs.forEach {
                if (fireballImpacts.remove(it.uniqueId) != null) removeVisual(it)
            }
            if (tick < launchAt && tick % 4L == 0L) targets.forEach { circle(it, s.radius, Color.PURPLE) }
            active.removeIf { tick >= it.second }
            active.forEach { (target, _) ->
                if (tick % 10L == 0L) {
                    circle(target, s.radius, Color.PURPLE)
                }
                if (tick % 20L == 0L) players().filter { nearby(it, target, s.radius) }.forEach {
                    it.addPotionEffect(
                        PotionEffect(
                            type,
                            seconds(config.effectDurationSeconds).toInt(),
                            config.effectAmplifier
                        )
                    )
                    if (s.damage > 0) damage(it, s.damage)
                }
            }
        }
    }

    fun impactFireball(entity: Entity) {
        fireballImpacts.remove(entity.uniqueId)?.invoke()
    }

    private fun strikes(s: AttackSettings, interruptible: Boolean) {
        val targets = playerTargets(count(s))
        if (targets.isEmpty()) return
        val start = now + seconds(s.warningSeconds)
        val focus = if (interruptible && config.interruptObjectives) createFocus(targets.first()) else null
        val tnt = if (interruptible) emptyList() else targets.map { at ->
            visual(at, TNTPrimed::class.java, "tnt").also {
                it.fuseTicks = seconds(s.warningSeconds).toInt()
                it.source = dragon
                it.setGravity(false)
                it.velocity = Vector()
                it.isGlowing = true
            }
        }
        var interrupted = false
        var fired = false
        effect(
            start + 5,
            cleanup = { tnt.forEach(::removeVisual); focus?.let { focuses.remove(it.uniqueId); it.remove() } }) { tick ->
            if (focus != null && (focus.isDead || !focus.isValid)) {
                if (!interrupted && config.announceAttacks) players().forEach { Messages.send(it, "interrupted") }
                interrupted = true
            }
            if (!interrupted && tick < start) targets.forEach {
                circle(it, s.radius, if (interruptible) Color.FUCHSIA else Color.ORANGE)
                dust(it.clone().add(0.0, 0.5, 0.0), Color.RED, 5)
            }
            if (!interrupted && !fired && tick >= start) {
                fired = true
                tnt.forEach(::removeVisual)
                targets.forEach { target ->
                    // Visual explosion plus entity damage; no TNT or block-damaging explosion.
                    world.spawnParticle(Particle.EXPLOSION, target, 1)
                    world.playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                    players().filter { nearby(it, target, s.radius) }.forEach { damage(it, s.damage) }
                }
            }
        }
    }

    private fun createFocus(at: Location): Slime {
        val focus = world.spawn(at, Slime::class.java)
        focus.size = 1
        focus.setAI(false)
        focus.isGlowing = true
        focus.isPersistent = false
        focus.isCustomNameVisible = config.announceAttacks
        if (config.announceAttacks) focus.customName(Messages.text("focus-name", Core.instance.server.consoleSender))
        focus.getAttribute(Attribute.MAX_HEALTH)?.baseValue = config.focusHealth
        focus.health = config.focusHealth
        focus.persistentDataContainer.set(key, PersistentDataType.STRING, "focus")
        focuses += focus.uniqueId
        if (config.announceAttacks) players().forEach { Messages.send(it, "focus") }
        return focus
    }

    private fun crystalTargets(s: AttackSettings) = grounds.keys.shuffled().mapNotNull { ground(it.first, it.second) }
        .filter {
            horizontal(it, center()) in 16.0..min(48.0, config.islandRadius - 4) &&
                    world.getNearbyEntities(it, 3.0, 3.0, 3.0).none { e -> e is EnderCrystal }
        }.let { candidates ->
            val selected = mutableListOf<Location>()
            for (at in candidates) {
                if (selected.all { horizontal(it, at) > 7 }) selected += at
                if (selected.size >= count(s)) break
            }
            selected
        }

    private fun renewCrystals(s: AttackSettings, targets: List<Location>) {
        val convergence = if (config.crystalConvergenceEnabled) surface(config.centerX.toInt(), config.centerZ.toInt())
            ?: grounds.keys.minByOrNull { hypot(it.first - config.centerX, it.second - config.centerZ) }
                ?.let { ground(it.first, it.second) } else null
        val start = now + seconds(s.warningSeconds)
        var fired = false
        effect(start + 5) { tick ->
            if (tick < start) targets.forEach { circle(it, 2.0, Color.PURPLE) }
            else if (!fired) {
                fired = true
                targets.forEach { at ->
                    if (world.getNearbyEntities(at, 3.0, 3.0, 3.0).any { it is EnderCrystal }) return@forEach
                    val crystal = world.spawn(at, EnderCrystal::class.java)
                    crystal.isShowingBottom = false
                    crystal.isGlowing = true
                    crystal.persistentDataContainer.set(key, PersistentDataType.STRING, "crystal")
                    crystals += crystal.uniqueId
                    val carrier = world.spawn(at, Silverfish::class.java)
                    carrier.isPersistent = false
                    carrier.persistentDataContainer.set(key, PersistentDataType.STRING, "crystal-carrier")
                    carrier.health = 1.0
                    carrier.addPassenger(crystal)
                    // A beam shows the destination without naming or explaining the objective.
                    crystal.beamTarget = convergence
                    mounts += CrystalMount(
                        carrier,
                        crystal,
                        convergence,
                        now + seconds(config.crystalConvergenceLifetimeSeconds)
                    )
                }
            }
        }
    }

    private fun summonWave(s: AttackSettings, blue: Boolean) {
        val targets = groundTargets(min(count(s), (config.maxSummons - summons.size).coerceAtLeast(0)))
        val start = now + seconds(s.warningSeconds)
        var fired = false
        effect(start + 5) { tick ->
            if (tick < start) targets.forEach { circle(it, 2.0, Color.BLUE) }
            else if (!fired) {
                fired = true
                targets.forEachIndexed { index, at ->
                    if (summons.size >= config.maxSummons) return@forEachIndexed
                    val special = blue || index % 3 == 1
                    val mob: LivingEntity =
                        if (!blue && index % 3 == 2) world.spawn(at, Shulker::class.java) else world.spawn(
                            at,
                            Enderman::class.java
                        )
                    mob.isPersistent = false
                    mob.persistentDataContainer.set(key, PersistentDataType.STRING, if (special) "blue" else "monster")
                    if (mob is Mob) mob.target = players().minByOrNull { horizontal(it.location, at) }
                    if (mob is Enderman) mob.isScreaming = true
                    if (special) {
                        mob.isGlowing = true
                        mob.isCustomNameVisible = config.announceAttacks
                        if (config.announceAttacks) mob.customName(
                            Messages.text(
                                "blue-name",
                                Core.instance.server.consoleSender
                            )
                        )
                        mob.getAttribute(Attribute.MAX_HEALTH)?.baseValue = config.blueEndermanHealth
                        mob.health = config.blueEndermanHealth
                    }
                    summons[mob.uniqueId] = Summon(mob, now + seconds(config.summonLifetimeSeconds), special, now + 60)
                }
                syncBlueTeams()
            }
        }
    }

    private fun syncBlueTeams() {
        val boards = world.players.map { it.scoreboard }.distinct()
        boards.forEach { board ->
            val name = "red" + dragon.uniqueId.toString().replace("-", "").take(12)
            val team = blueTeams.getOrPut(board) {
                (board.getTeam(name) ?: board.registerNewTeam(name)).also {
                    it.color(NamedTextColor.BLUE)
                }
            }
            summons.values.filter { it.blue }.forEach { team.addEntry(it.entity.uniqueId.toString()) }
        }
    }

    private fun removeBlueEntry(entity: Entity) {
        blueTeams.values.forEach { it.removeEntry(entity.uniqueId.toString()) }
    }

    private fun endermanWave(mob: Enderman) {
        if (hostileEffects() >= config.maxConcurrentAttacks || now < recoveryUntil ||
            !AttackDirector.compatible(Attack.DRAGON_WAVE, activeAttacks())
        ) return
        val origin = mob.location.clone()
        val direction = origin.direction.setY(0)
        if (direction.lengthSquared() < 0.01) return
        direction.normalize()
        announce("enderman-wave")
        castingAttack = Attack.BLUE_ENDERMEN
        try {
            wave(
                origin, config.blueEndermanWaveRadius, AttackSettings(
                    warningSeconds = config.blueEndermanWaveWarningSeconds, damage = config.blueEndermanWaveDamage
                ), direction
            )
        } finally {
            castingAttack = null
        }
    }

    private fun infestation(s: AttackSettings) {
        // Resolve full fighter-adjacent patches immediately, even during the initial scan.
        players().forEach { p ->
            for (dx in -config.infestationPatchRadius..config.infestationPatchRadius)
                for (dz in -config.infestationPatchRadius..config.infestationPatchRadius) {
                    val x = p.location.blockX + dx;
                    val z = p.location.blockZ + dz
                    surface(x, z)?.let { at ->
                        if (at.block.getRelative(0, -1, 0).type == Material.END_STONE) grounds[x to z] = at.blockY - 1
                    }
                }
        }
        val start = now + seconds(s.warningSeconds)
        val end = start + seconds(s.durationSeconds)
        val coverage = min(
            0.6,
            config.infestationCoverage + (players().size.coerceIn(
                1,
                config.maxScalingPlayers
            ) - 1) * config.infestationCoveragePerExtraPlayer
        )
        val selectedColumns = linkedSetOf<Pair<Int, Int>>()
        val spacing = config.infestationSpacing + config.infestationPatchRadius * 2
        val anchors = grounds.keys.filter { (x, z) ->
            Math.floorMod(x, spacing) == 0 && Math.floorMod(z, spacing) == 0 && Random.nextDouble() < coverage * 2
        }.shuffled().toMutableList()
        // Seed a large patch beneath each fighter, then distribute remaining patches island-wide.
        anchors.addAll(0, players().map { it.location.blockX to it.location.blockZ })
        for ((x, z) in anchors) {
            for (dx in -config.infestationPatchRadius..config.infestationPatchRadius)
                for (dz in -config.infestationPatchRadius..config.infestationPatchRadius) {
                    if (dx * dx + dz * dz <= config.infestationPatchRadius * config.infestationPatchRadius &&
                        selectedColumns.size < config.maxInfestedBlocks && (x + dx to z + dz) in grounds
                    )
                        selectedColumns += x + dx to z + dz
                }
        }
        val selected = selectedColumns.mapNotNull { ground(it.first, it.second)?.block }
        val lastHit = mutableMapOf<UUID, Long>()
        var placed = false
        effect(end, cleanup = { if (placed) terrain.restoreBlocks(selected) }) { tick ->
            if (tick < start) selected.filterIndexed { i, _ -> i % 8 == 0 }
                .forEach { dust(it.location.add(0.5, 0.2, 0.5), Color.OLIVE) }
            else {
                if (!placed) {
                    terrain.place(selected, end); placed = true
                }
                players().forEach { p ->
                    if (terrain.contains(p.location.block) && p.location.y - p.location.blockY < 0.65) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 30, config.infestationSlowAmplifier))
                        if (tick - lastHit.getOrDefault(
                                p.uniqueId,
                                -100000L
                            ) >= seconds(config.infestationPulseSeconds)
                        ) {
                            lastHit[p.uniqueId] = tick
                            damage(p, s.damage)
                            world.spawnParticle(
                                Particle.DAMAGE_INDICATOR,
                                p.location.add(0.0, 0.5, 0.0),
                                4,
                                0.2,
                                0.2,
                                0.2,
                                0.0
                            )
                        }
                    }
                }
            }
        }
    }

    private fun magnetism(s: AttackSettings) {
        val floors = mutableMapOf<UUID, Double>()
        val start = now + seconds(s.warningSeconds)
        val end = start + seconds(s.durationSeconds)
        effect(end) { tick ->
            if (tick < start) {
                if (config.subtleCues && tick % 10L == 0L) players().forEach {
                    dust(
                        it.location.clone().add(0.0, 1.0, 0.0), Color.AQUA, 3
                    )
                }
                return@effect
            }
            players().forEach { p ->
                val under = surface(p.location.blockX, p.location.blockZ, p.location.blockY) ?: return@forEach
                val floorY = floors.getOrPut(p.uniqueId) { under.y }
                if (abs(under.y - floorY) > 2.0) return@forEach
                manager.trackLanding(p, config.magnetFallDamageCap)
                val target = dragon.location.clone()
                target.y = min(floorY, under.y) + config.magnetLiftBlocks
                // Do not pull off the island or over an unscanned/void column.
                val delta = target.toVector().subtract(p.location.toVector())
                delta.y = (target.y - p.location.y).coerceIn(-0.2, 0.4)
                val horizontal = Vector(delta.x, 0.0, delta.z)
                if (horizontal.lengthSquared() > 0.01) horizontal.normalize().multiply(config.magnetPullSpeed)
                val next = p.location.clone().add(horizontal)
                val nextGround = surface(next.blockX, next.blockZ)
                if (nextGround == null || abs(nextGround.y - floorY) > 2.0 || horizontal(
                        next,
                        center()
                    ) > config.islandRadius - 4
                ) horizontal.zero()
                if (p.location.y > floorY + config.magnetLiftBlocks) delta.y = -0.2
                p.velocity = horizontal.setY(delta.y)
                dust(p.location, Color.AQUA, 2)
            }
        }
    }

    private fun markCandidates() = players().filter {
        now - markedLastUse.getOrDefault(it.uniqueId, -100000L) >= seconds(config.markedRetargetSeconds)
    }.sortedBy { markedLastUse[it.uniqueId] ?: Long.MIN_VALUE }

    private fun markedHunters(s: AttackSettings) {
        val fighters =
            markCandidates().filter { surface(it.location.blockX, it.location.blockZ) != null }.take(count(s))
        val locks = mutableMapOf<UUID, Location>()
        val cancelled = mutableSetOf<UUID>()
        val marks = fighters.associate { p ->
            markedLastUse[p.uniqueId] = now
            p.uniqueId to display(p.location.clone().add(0.0, 2.4, 0.0), Material.PURPLE_STAINED_GLASS, 0.45f, 0.45f)
        }
        val lockAt = now + seconds(s.warningSeconds)
        val start = lockAt + seconds(config.markedLockWarningSeconds)
        val end = start + seconds(s.durationSeconds)
        fighters.forEach { cue(it.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f) }
        effect(end, cleanup = { marks.values.forEach(::removeVisual) }) { tick ->
            fighters.forEach { p ->
                if (p.uniqueId in cancelled) return@forEach
                if (p !in players()) {
                    cancelled += p.uniqueId
                    locks.remove(p.uniqueId)
                    marks[p.uniqueId]?.let { if (it.isValid) removeVisual(it) }
                    return@forEach
                }
                if (tick < lockAt) {
                    marks[p.uniqueId]?.teleport(p.location.clone().add(0.0, 2.4, 0.0))
                    if (config.subtleCues) dust(p.location.clone().add(0.0, 1.2, 0.0), Color.PURPLE, 2)
                } else if (p.uniqueId !in locks) {
                    val at = surface(p.location.blockX, p.location.blockZ)
                    if (at == null) {
                        cancelled += p.uniqueId
                        marks[p.uniqueId]?.let { if (it.isValid) removeVisual(it) }
                        return@forEach
                    }
                    if (horizontal(at, center()) > config.islandRadius - s.radius - 4 ||
                        protections.any { horizontal(at, it.center) < it.radius + s.radius } ||
                        anchors.mapNotNull(Bukkit::getEntity).any { horizontal(at, it.location) < s.radius + 4 } ||
                        mounts.any { it.target?.let { target -> horizontal(at, target) < s.radius + 5 } == true }
                    ) {
                        cancelled += p.uniqueId
                        marks[p.uniqueId]?.let { if (it.isValid) removeVisual(it) }
                        return@forEach
                    }
                    locks[p.uniqueId] = at
                    marks[p.uniqueId]?.teleport(at.clone().add(0.0, 0.2, 0.0))
                    cue(at, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.3f)
                }
            }
            locks.values.forEach { at ->
                circle(at, s.radius, if (tick < start) Color.PURPLE else Color.FUCHSIA)
                if (tick >= start && tick % 20L == 0L) {
                    world.spawnParticle(Particle.DRAGON_BREATH, at, 12, s.radius / 2, 0.2, s.radius / 2, 0.0, 1.0f)
                    players().filter { nearby(it, at, s.radius) }.forEach { damage(it, s.damage) }
                }
            }
        }
    }

    private fun riftAnchors(s: AttackSettings) {
        val targets = groundTargets(count(s)).filter { horizontal(it, center()) < config.islandRadius - 8 }
        val start = now + seconds(s.warningSeconds)
        val end = start + seconds(s.durationSeconds)
        val spawned = mutableListOf<Slime>()
        var fired = false
        effect(end, cleanup = {
            spawned.forEach { anchors.remove(it.uniqueId); focuses.remove(it.uniqueId); it.remove() }
        }) { tick ->
            if (tick < start) targets.forEach { circle(it, 2.0, Color.PURPLE) }
            else {
                if (!fired) {
                    fired = true
                    targets.forEach { at ->
                        val anchor = createFocus(at)
                        anchor.size = 2
                        anchor.isCustomNameVisible = config.announceAttacks
                        if (config.announceAttacks) anchor.customName(
                            Messages.text(
                                "anchor-name",
                                Core.instance.server.consoleSender
                            )
                        )
                        anchor.getAttribute(Attribute.MAX_HEALTH)?.baseValue = config.anchorHealth
                        anchor.health = config.anchorHealth
                        anchors += anchor.uniqueId
                        spawned += anchor
                    }
                }
                spawned.filter { it.isValid && !it.isDead }.forEach {
                    if (tick % 10L == 0L) {
                        circle(it.location, 2.0, Color.PURPLE)
                        dust(it.location.clone().add(0.0, 2.0, 0.0), Color.FUCHSIA, 4)
                    }
                }
            }
        }
    }

    private fun sweepTarget(s: AttackSettings) = players().filter {
        horizontal(it.location, dragon.location) in 5.0..s.radius
    }.minByOrNull { horizontal(it.location, dragon.location) }

    /** Fixed ground projection and direction: visuals and hit tests use the same snapshot. */
    private fun breathSweep(s: AttackSettings) {
        val target = sweepTarget(s) ?: return
        val origin = surface(dragon.location.blockX, dragon.location.blockZ) ?: return
        val direction = target.location.toVector().subtract(origin.toVector()).setY(0).normalize()
        val bearing = atan2(direction.z, direction.x)
        val arc = Math.toRadians(config.breathSweepDegrees)
        val start = now + seconds(s.warningSeconds)
        val end = start + seconds(s.durationSeconds)
        val markers = mutableListOf<Pair<BlockDisplay, Location>>()
        // Bounded display grid, all resolved on loaded ground; the rest of the island is safe.
        for (distance in 4..s.radius.toInt() step 3) {
            for (segment in 0..8) {
                val angle = bearing - arc / 2 + arc * segment / 8
                val at = surface(
                    floor(origin.x + cos(angle) * distance).toInt(),
                    floor(origin.z + sin(angle) * distance).toInt()
                ) ?: continue
                markers += display(at, Material.PURPLE_STAINED_GLASS, 0.8f, 0.15f) to at
            }
        }
        effect(end, cleanup = { markers.forEach { removeVisual(it.first) } }) { tick ->
            if (tick < start) {
                if (tick % 20L == 0L) cue(origin, Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.6f)
            } else {
                val progress = ((tick - start).toDouble() / seconds(s.durationSeconds)).coerceIn(0.0, 1.0)
                val front = bearing - arc / 2 + progress * arc
                markers.forEach { (block, at) ->
                    val angle = atan2(at.z - origin.z, at.x - origin.x)
                    val active = FightMath.angleDistance(angle, front) <= Math.toRadians(12.0)
                    block.block =
                        (if (active) Material.MAGMA_BLOCK else Material.PURPLE_STAINED_GLASS).createBlockData()
                    if (active && tick % 4L == 0L) world.spawnParticle(
                        Particle.DRAGON_BREATH,
                        at,
                        3,
                        0.3,
                        0.2,
                        0.3,
                        0.0,
                        1.0f
                    )
                }
                if (tick % 10L == 0L) players().forEach { p ->
                    val dx = p.location.x - origin.x;
                    val dz = p.location.z - origin.z
                    val under = surface(p.location.blockX, p.location.blockZ, p.location.blockY) ?: return@forEach
                    if (FightMath.angleDistance(atan2(dz, dx), bearing) <= arc / 2 &&
                        FightMath.inSweep(dx, dz, front, s.radius, 12.0) && abs(p.location.y - under.y) < 2.5
                    )
                        damage(p, s.damage)
                }
            }
        }
    }

    private fun sanctuary(s: AttackSettings) {
        val targets = groundTargets(count(s))
        val end = now + seconds(s.durationSeconds)
        targets.forEach { protections += Protection(it, s.radius, end) }
        effect(end) { tick ->
            targets.forEach { at ->
                circle(at, s.radius, Color.LIME)
                if (tick % 20L == 0L) players().filter { nearby(it, at, s.radius) }.forEach {
                    it.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 30, 0))
                }
            }
        }
    }

    private fun updraft(s: AttackSettings) {
        val targets = groundTargets(count(s))
        effect(now + seconds(s.durationSeconds)) {
            targets.forEach { at ->
                circle(at, s.radius, Color.AQUA)
                players().filter { nearby(it, at, s.radius) && isSupported(it) }.forEach { p ->
                    if (updraftUses.getOrDefault(p.uniqueId, 0) >= config.updraftMaxUsesPerPlayer) return@forEach
                    updraftUses[p.uniqueId] = updraftUses.getOrDefault(p.uniqueId, 0) + 1
                    p.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.SLOW_FALLING,
                            seconds(config.updraftSlowFallingSeconds).toInt(),
                            0
                        )
                    )
                    p.velocity = p.velocity.setY(0.65)
                }
            }
        }
    }

    fun healingPool(at: Location) {
        if (poolCount >= config.maxHealingPools) return
        poolCount++
        if (config.announceAttacks) players().forEach { Messages.send(it, "pool") }
        world.playSound(at, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.5f)
        val previousCast = castingAttack
        castingAttack = Attack.HEALING_POOL
        effect(now + seconds(config.healingPoolLifetimeSeconds), cleanup = { poolCount-- }) { tick ->
            circle(at, config.healingPoolRadius, Color.LIME)
            world.spawnParticle(
                Particle.HAPPY_VILLAGER, at.clone().add(0.0, 0.6, 0.0), 24,
                config.healingPoolRadius / 2, 0.5, config.healingPoolRadius / 2, 0.0
            )
            world.spawnParticle(
                Particle.DUST,
                at.clone().add(0.0, 0.4, 0.0),
                35,
                config.healingPoolRadius / 2,
                0.15,
                config.healingPoolRadius / 2,
                0.0,
                Particle.DustOptions(Color.LIME, 2f)
            )
            repeat(8) { dust(at.clone().add(0.0, it * 0.5, 0.0), Color.LIME, 3) }
            if (tick % 20L == 0L) players().filter { nearby(it, at, config.healingPoolRadius) }.forEach { p ->
                p.health = min(p.getAttribute(Attribute.MAX_HEALTH)!!.value, p.health + config.healingPerSecond)
                p.foodLevel = min(20, p.foodLevel + config.foodPerSecond)
                p.saturation = min(p.foodLevel.toFloat(), p.saturation + config.foodPerSecond * 0.5f)
                p.inventory.contents.filterNotNull().forEach { item ->
                    val meta = item.itemMeta
                    if (meta is Damageable && meta.hasDamage()) {
                        meta.damage = max(0, meta.damage - config.durabilityPerSecond)
                        item.itemMeta = meta
                    }
                }
                if (config.cleansingEnabled) config.cleanseEffects.mapNotNull(::potion)
                    .forEach { p.removePotionEffect(it) }
            }
        }
        castingAttack = previousCast
    }

    fun summonDied(entity: LivingEntity, killer: Player?) {
        if (anchors.remove(entity.uniqueId)) {
            focuses.remove(entity.uniqueId)
            if (killer != null && killer in players()) {
                vulnerabilityUntil = max(vulnerabilityUntil, now + seconds(config.anchorVulnerabilitySeconds))
                cue(entity.location, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.3f)
                if (config.subtleCues) world.spawnParticle(Particle.CRIT, dragon.location, 30, 2.0, 1.0, 2.0, 0.1)
            }
            return
        }
        val summon = summons.remove(entity.uniqueId) ?: return
        removeBlueEntry(entity)
        if (killer == null || killer !in players()) return
        if (summon.blue) healingPool(entity.location.clone())
        if (config.rallyEnabled) players().filter { horizontal(it.location, entity.location) <= config.rallyRadius }
            .forEach {
                it.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, seconds(config.rallySeconds).toInt(), 0))
                it.addPotionEffect(PotionEffect(PotionEffectType.SPEED, seconds(config.rallySeconds).toInt(), 0))
            }
    }

    fun crystalReward(at: Location) {
        if (!config.crystalRewardsEnabled) return
        players().filter { horizontal(it.location, at) <= config.crystalRewardRadius }.forEach {
            it.addPotionEffect(
                PotionEffect(
                    PotionEffectType.REGENERATION,
                    seconds(config.crystalRewardSeconds).toInt(),
                    0
                )
            )
            it.addPotionEffect(
                PotionEffect(
                    PotionEffectType.ABSORPTION,
                    seconds(config.crystalRewardSeconds).toInt(),
                    0
                )
            )
        }
    }

    private fun removeMount(mount: CrystalMount) {
        mounts.remove(mount)
        crystals.remove(mount.crystal.uniqueId)
        mount.carrier.remove(); mount.crystal.remove()
    }

    /** Any accepted hit to either half destroys the pair exactly once. */
    fun hitMount(entity: Entity, attacker: Player?): Boolean {
        val mount =
            mounts.firstOrNull { it.carrier.uniqueId == entity.uniqueId || it.crystal.uniqueId == entity.uniqueId }
                ?: return false
        val at = mount.crystal.location.clone()
        removeMount(mount)
        world.spawnParticle(Particle.EXPLOSION, at, 1)
        world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f)
        if (attacker != null && attacker in players()) crystalReward(at)
        return true
    }

    fun isMount(entity: Entity) =
        mounts.any { it.carrier.uniqueId == entity.uniqueId || it.crystal.uniqueId == entity.uniqueId }

    fun isControlledProjectile(entity: Entity) =
        visuals[entity.uniqueId]?.let { it is DragonFireball || it is TNTPrimed } == true

    fun owns(entity: Entity) =
        entity.uniqueId in summons || entity.uniqueId in focuses || entity.uniqueId in crystals || entity.uniqueId in visuals || isMount(
            entity
        )

    fun isFocus(entity: Entity) = entity.uniqueId in focuses
    fun isBlue(entity: Entity) = summons[entity.uniqueId]?.blue == true

    fun close() {
        if (closed) return
        closed = true
        effects.toList().forEach { it.cleanup() }
        effects.clear()
        mounts.toList().forEach(::removeMount)
        visuals.values.toList().forEach(::removeVisual)
        (summons.keys + focuses + crystals).forEach { Bukkit.getEntity(it)?.remove() }
        summons.clear(); focuses.clear(); crystals.clear()
        anchors.clear(); markedLastUse.clear(); lastHit.clear()
        blueTeams.values.forEach { it.unregister() }; blueTeams.clear()
        terrain.restoreWorld(world.uid.toString())
        if (dragon.isValid && !dragon.isDead && !preview) {
            val ratio = fraction().coerceIn(0.0, 1.0)
            dragon.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = originalMaxHealth
            dragon.health = max(0.01, min(dragon.getAttribute(Attribute.MAX_HEALTH)!!.value, originalMaxHealth * ratio))
        }
        if (!preview) dragon.persistentDataContainer.remove(healthKey)
    }
}

fun potion(name: String): PotionEffectType? {
    val key = NamespacedKey.fromString(name.lowercase()) ?: return null
    return Registry.EFFECT.get(key)
}
