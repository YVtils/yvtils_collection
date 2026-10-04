/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable
import yv.tils.configv2.data.annotations.ConfigIcon
import yv.tils.configv2.data.annotations.BooleanIcon
import org.bukkit.Material
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.configv2.files.ConfigurateFileUtils
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.util.NamingSchemes
import io.leangen.geantyref.GenericTypeReflector
import org.spongepowered.configurate.BasicConfigurationNode
import yv.tils.utils.logger.Logger

enum class Activation { EVERY, ADMIN, FIRST }

enum class Attack {
    CRYSTALS, ISLAND_WAVE, EFFECT_AREAS, EXPLOSIVES, MONSTERS, DRAGON_WAVE,
    INFESTATION, MAGNETISM, BLUE_ENDERMEN, SUMMON_AREAS,
    SANCTUARY, RESCUE_UPDRAFT, HEALING_POOL;

    val id: String get() = name.lowercase().replace('_', '-')
}

data class AttackSettings(
    @ConfigDescription("Allow this attack in normal encounters; previews can still trigger it")
    @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.RED_DYE)
    var enabled: Boolean = true,
    @ConfigDescription("Minimum seconds between uses of this attack")
    @ConfigIcon(Material.CLOCK)
    var cooldownSeconds: Double = 40.0,
    @ConfigDescription("Uses per encounter; -1 means unlimited; 0 disables")
    var maxUses: Int = 4,
    @ConfigDescription("Minimum dragon health fraction, 0..1")
    var minHealthFraction: Double = 0.0,
    @ConfigDescription("Maximum dragon health fraction, 0..1")
    var maxHealthFraction: Double = 1.0,
    @ConfigDescription("Seconds of visible warning before activation")
    @ConfigIcon(Material.BELL)
    var warningSeconds: Double = 3.0,
    @ConfigDescription("Active lifetime in seconds; instant casts and waves use their own lifetime")
    @ConfigIcon(Material.CLOCK)
    var durationSeconds: Double = 8.0,
    @ConfigDescription("Damage in health points, before armor; two points equal one heart")
    var damage: Double = 4.0,
    @ConfigDescription("Area radius in blocks; island waves use the global island radius")
    var radius: Double = 4.0,
    @ConfigDescription("Targets, zones or entities for one fighter")
    var baseCount: Int = 2,
    @ConfigDescription("Additional count per extra fighter, rounded up")
    var extraCountPerPlayer: Double = 0.5,
    @ConfigDescription("Hard cap on targets, zones or entities per cast")
    var maxCount: Int = 12,
)

data class DragonConfig(
    @NotGuiEditable
    var schemaVersion: Int = 2,
    @ConfigDescription("Master switch for custom encounters")
    @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.RED_DYE)
    var enabled: Boolean = true,
    @ConfigDescription("EVERY: all fights; ADMIN: command only; FIRST: first dragon kill per world")
    @NotGuiEditable
    var activation: Activation = Activation.EVERY,
    @ConfigDescription("Empty allows all End worlds; otherwise use exact world names")
    var worlds: List<String> = emptyList(),
    var centerX: Double = 0.0,
    var centerZ: Double = 0.0,
    @ConfigDescription("Main island boundary in blocks, 16..160; also controls full-island waves")
    @ConfigIcon(Material.END_STONE)
    var islandRadius: Double = 96.0,
    var groundMinY: Int = 40,
    var groundMaxY: Int = 90,
    var maxScalingPlayers: Int = 20,
    @ConfigDescription("Dragon health points for one fighter; two points equal one heart")
    @ConfigIcon(Material.DRAGON_HEAD)
    var baseHealth: Double = 260.0,
    var healthPerExtraPlayer: Double = 100.0,
    @ConfigDescription("Hard cap on scaled dragon health")
    var maxHealth: Double = 2200.0,
    var damagePerExtraPlayer: Double = 0.025,
    var maxDamageMultiplier: Double = 1.4,
    @ConfigDescription("Fraction of armor/enchantment damage reduction ignored by scripted attacks, 0..1; absorption, resistance and sanctuary still work")
    var customAttackProtectionPiercing: Double = 0.6,
    var attackIntervalSeconds: Double = 12.0,
    var minimumAttackIntervalSeconds: Double = 7.0,
    var intervalReductionPerExtraPlayer: Double = 0.25,
    @ConfigDescription("Concurrent normal casts, 1..6; support has two reserved slots")
    var maxConcurrentAttacks: Int = 2,
    var maxSummons: Int = 24,
    var summonLifetimeSeconds: Double = 50.0,
    var replaceVanillaBreath: Boolean = true,
    @ConfigDescription("Announce attacks and combat hints in chat/action bars; visuals remain when disabled")
    var announceAttacks: Boolean = false,
    @ConfigDescription("Maximum block-display segments per wave")
    var waveDisplaySegments: Int = 160,
    var dragonWaveSpeedBlocksPerSecond: Double = 18.0,
    var crystalCarrierSpeed: Double = 1.2,
    var infestationPatchRadius: Int = 3,
    var infestationSlowAmplifier: Int = 2,
    var infestationPulseSeconds: Double = 0.75,
    var waveSpeedBlocksPerSecond: Double = 10.0,
    var waveJumpHeight: Double = 0.65,
    var infestationSpacing: Int = 4,
    var infestationCoverage: Double = 0.22,
    var infestationCoveragePerExtraPlayer: Double = 0.01,
    var maxInfestedBlocks: Int = 1800,
    @ConfigDescription("Minecraft effect IDs randomly selected for hostile effect areas")
    @ConfigIcon(Material.LINGERING_POTION)
    var effectPool: List<String> = listOf("slowness", "weakness", "poison", "blindness"),
    var effectDurationSeconds: Double = 4.0,
    var effectAmplifier: Int = 0,
    var magnetLiftBlocks: Double = 6.0,
    var magnetPullSpeed: Double = 0.35,
    @ConfigDescription("Maximum landing damage from magnetism, also bounded to leave one health point")
    var magnetFallDamageCap: Double = 3.0,
    var blueEndermanHealth: Double = 32.0,
    var blueEndermanWaveCooldownSeconds: Double = 9.0,
    var blueEndermanWaveWarningSeconds: Double = 1.5,
    var blueEndermanWaveRadius: Double = 6.0,
    var blueEndermanWaveDamage: Double = 4.0,
    var healingPoolLifetimeSeconds: Double = 12.0,
    var healingPoolRadius: Double = 3.5,
    @ConfigDescription("Health points restored per healing-pool pulse; zero disables healing")
    @ConfigIcon(Material.GOLDEN_APPLE)
    var healingPerSecond: Double = 1.0,
    var foodPerSecond: Int = 1,
    @ConfigDescription("Durability repaired per damaged inventory item per pool pulse")
    @ConfigIcon(Material.ANVIL)
    var durabilityPerSecond: Int = 3,
    var maxHealingPools: Int = 3,
    var cleanseEffects: List<String> = listOf("poison", "wither", "slowness", "weakness", "blindness"),
    var crystalRewardRadius: Double = 16.0,
    var crystalRewardsEnabled: Boolean = true,
    var crystalRewardSeconds: Double = 6.0,
    var rallyRadius: Double = 16.0,
    var rallyEnabled: Boolean = true,
    var cleansingEnabled: Boolean = true,
    var rallySeconds: Double = 6.0,
    var interruptObjectives: Boolean = true,
    var focusHealth: Double = 12.0,
    var sanctuaryDamageMultiplier: Double = 0.25,
    var updraftMaxUsesPerPlayer: Int = 2,
    var updraftSlowFallingSeconds: Double = 8.0,
    var supportEveryAttacks: Int = 3,
    @NotGuiEditable
    var attacks: Map<String, AttackSettings> = defaultAttacks(),
) {
    fun settings(attack: Attack): AttackSettings = attacks[attack.id] ?: defaultAttacks().getValue(attack.id)

    fun validate() {
        require(islandRadius.isFinite() && islandRadius in 16.0..160.0) { "islandRadius must be 16..160" }
        require(centerX.isFinite() && centerZ.isFinite()) { "Center must be finite" }
        require(groundMinY in -64..319 && groundMaxY in -63..320 && groundMinY < groundMaxY) { "Invalid ground scan height bounds" }
        require(maxScalingPlayers in 1..100 && maxConcurrentAttacks in 1..6) { "Invalid scaling/concurrency cap" }
        require(baseHealth.isFinite() && baseHealth >= 1 && maxHealth.isFinite() && maxHealth >= baseHealth)
        require(healthPerExtraPlayer.isFinite() && healthPerExtraPlayer >= 0)
        require(damagePerExtraPlayer.isFinite() && damagePerExtraPlayer >= 0 && maxDamageMultiplier in 1.0..3.0)
        require(customAttackProtectionPiercing in 0.0..1.0)
        require(attackIntervalSeconds in 1.0..600.0 && minimumAttackIntervalSeconds in 1.0..600.0)
        require(intervalReductionPerExtraPlayer in 0.0..10.0)
        require(maxSummons in 0..100 && summonLifetimeSeconds in 1.0..300.0)
        require(waveSpeedBlocksPerSecond in 2.0..30.0 && waveJumpHeight in 0.1..1.2)
        require(waveDisplaySegments in 32..256 && dragonWaveSpeedBlocksPerSecond in 2.0..30.0)
        require(crystalCarrierSpeed in 0.2..3.0 && infestationPatchRadius in 2..6)
        require(infestationSlowAmplifier in 0..4 && infestationPulseSeconds in 0.5..3.0)
        require(infestationSpacing in 2..12 && infestationCoverage in 0.0..0.6)
        require(infestationCoveragePerExtraPlayer in 0.0..0.1 && maxInfestedBlocks in 0..2000)
        require(effectPool.isNotEmpty() && effectDurationSeconds in 1.0..30.0 && effectAmplifier in 0..2)
        require(magnetLiftBlocks in 1.0..7.0 && magnetPullSpeed in 0.05..0.5 && magnetFallDamageCap in 0.0..6.0)
        require(blueEndermanHealth in 1.0..200.0 && blueEndermanWaveCooldownSeconds in 3.0..120.0)
        require(blueEndermanWaveWarningSeconds in 0.5..10.0 && blueEndermanWaveRadius in 1.0..12.0)
        require(blueEndermanWaveDamage in 0.0..20.0)
        require(healingPoolLifetimeSeconds in 1.0..60.0 && healingPoolRadius in 1.0..10.0)
        require(healingPerSecond in 0.0..10.0 && foodPerSecond in 0..5 && durabilityPerSecond in 0..50)
        require(maxHealingPools in 0..10 && crystalRewardRadius in 1.0..32.0 && rallyRadius in 1.0..32.0)
        require(crystalRewardSeconds in 1.0..30.0 && rallySeconds in 1.0..30.0 && focusHealth in 1.0..100.0)
        require(sanctuaryDamageMultiplier in 0.0..1.0 && updraftMaxUsesPerPlayer in 0..10)
        require(updraftSlowFallingSeconds in 1.0..30.0 && supportEveryAttacks in 1..20)
        attacks.forEach { (id, s) ->
            require(Attack.entries.any { it.id == id }) { "Unknown attack: $id" }
            require(s.cooldownSeconds in 0.0..3600.0 && s.maxUses >= -1)
            require(s.minHealthFraction in 0.0..1.0 && s.maxHealthFraction in s.minHealthFraction..1.0)
            require(s.warningSeconds in 0.5..30.0 && s.durationSeconds in 0.5..60.0)
            require(s.damage in 0.0..40.0 && s.radius in 1.0..32.0)
            require(s.baseCount in 0..50 && s.extraCountPerPlayer in 0.0..5.0 && s.maxCount in 0..100)
        }
    }
}

fun defaultAttacks(): Map<String, AttackSettings> = mapOf(
    "crystals" to AttackSettings(cooldownSeconds = 120.0, maxUses = 1, maxHealthFraction = 0.5, baseCount = 1, maxCount = 5),
    "island-wave" to AttackSettings(cooldownSeconds = 55.0, maxUses = 4, damage = 7.0),
    "effect-areas" to AttackSettings(cooldownSeconds = 30.0, maxUses = 6, damage = 3.0),
    "explosives" to AttackSettings(cooldownSeconds = 35.0, maxUses = 5, damage = 7.0, durationSeconds = 1.0),
    "monsters" to AttackSettings(cooldownSeconds = 45.0, maxUses = 4, baseCount = 3, maxCount = 10),
    "dragon-wave" to AttackSettings(cooldownSeconds = 20.0, maxUses = 6, warningSeconds = 1.0, radius = 9.0, damage = 7.0),
    "infestation" to AttackSettings(cooldownSeconds = 60.0, maxUses = 3, damage = 3.0, durationSeconds = 7.0),
    "magnetism" to AttackSettings(cooldownSeconds = 55.0, maxUses = 3, maxHealthFraction = 0.75, durationSeconds = 3.0, maxCount = 8),
    "blue-endermen" to AttackSettings(cooldownSeconds = 50.0, maxUses = 5, baseCount = 1, maxCount = 6),
    "summon-areas" to AttackSettings(cooldownSeconds = 50.0, maxUses = 4, maxHealthFraction = 0.75, warningSeconds = 4.0, damage = 12.0, radius = 5.0, durationSeconds = 1.0),
    "sanctuary" to AttackSettings(cooldownSeconds = 50.0, maxUses = 4, durationSeconds = 12.0, baseCount = 1, maxCount = 3),
    "rescue-updraft" to AttackSettings(cooldownSeconds = 50.0, maxUses = 4, durationSeconds = 15.0, baseCount = 1, maxCount = 3, radius = 2.0),
    "healing-pool" to AttackSettings(cooldownSeconds = 30.0, maxUses = 0, baseCount = 1, maxCount = 1),
)

class ConfigFile {
    companion object {
        private const val PATH = "/remade-ender-dragon/config.yml"
        var state = DragonConfig()
            private set

        fun load() {
            // Unlike the general convenience loader, fail on malformed existing files.
            // A typo must not replace an administrator's configuration with defaults.
            val file = ConfigurateFileUtils.create(PATH, emptyMap(), ConfigFormat.YAML).file
            val factory = ObjectMapper.factoryBuilder().addDiscoverer(dataClassFieldDiscoverer())
                .defaultNamingScheme(NamingSchemes.PASSTHROUGH).build()
            val serializers = TypeSerializerCollection.defaults().childBuilder()
                .register({ type -> GenericTypeReflector.erase(type).kotlin.isData }, factory.asTypeSerializer()).build()
            val node = BasicConfigurationNode.root(ConfigurationOptions.defaults().serializers(serializers))
            if (file.exists()) node.from(ConfigurateFileUtils.load(PATH, ConfigFormat.YAML).node)
            val next = if (file.exists()) factory.get(DragonConfig::class.java).load(node) else DragonConfig()
            if (file.exists() && node.node("schemaVersion").getInt(1) < 2) migrateCombatDefaults(next)
            applyState(next)
        }

        /** Upgrade untouched beta defaults while retaining administrator custom values. */
        private fun migrateCombatDefaults(next: DragonConfig) {
            val updated = next.attacks.mapValues { (_, s) -> s.copy() }.toMutableMap()
            fun update(id: String, change: (AttackSettings) -> Unit) { updated[id]?.let(change) }
            update("island-wave") { if (it.damage == 5.0) it.damage = 7.0 }
            update("effect-areas") { if (it.damage == 0.0) it.damage = 3.0 }
            update("monsters") { if (it.baseCount == 2) it.baseCount = 3 }
            update("dragon-wave") {
                if (it.cooldownSeconds == 30.0) it.cooldownSeconds = 20.0
                if (it.warningSeconds == 3.0) it.warningSeconds = 1.0
                if (it.damage == 4.0) it.damage = 7.0
            }
            update("infestation") { if (it.damage == 1.0) it.damage = 3.0 }
            if (next.maxInfestedBlocks == 900) next.maxInfestedBlocks = 1800
            next.attacks = updated
            next.schemaVersion = 2
            Logger.info("[Remade Ender Dragon] Upgraded untouched combat defaults to schema 2; custom values retained.")
        }

        /** Save before publishing a validated state; GUI edits use a detached draft. */
        fun applyState(next: DragonConfig) {
            next.validate()
            require((next.effectPool + next.cleanseEffects).all { yv.tils.remadeEnderDragon.logic.potion(it) != null }) { "Unknown potion effect in effectPool or cleanseEffects" }
            ObjectMapperFileUtils.save(PATH, next, format = ConfigFormat.YAML)
            state = next
        }
    }
}
