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

package yv.tils.yv_smp.logic.start

import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.utils.CinematicCamera
import yv.tils.yv_smp.utils.IslandScanner
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeGlow
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgePillarRise
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveInward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.islandRain
import java.time.Duration

class FinalPhase : PhasePlugin {
    override fun getPhaseDuration(): Long {
        // Duration: 2L * 40 = 80 ticks for accelerating particles + title display (3200ms = 160 ticks) + buffer = 280 ticks
        return 280L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // ── Camera: rising orbit — camera ascends as tension builds to peak ───
        players.forEach { it.gameMode = org.bukkit.GameMode.SPECTATOR }
        CinematicCamera.orbitRising(
            players             = players,
            center              = spawnLoc,
            orbitRadius         = 34.0,
            startHeight         = 8.0,
            endHeight           = 60.0,
            durationTicks       = 275L,
            angularSpeedPerTick = 0.008,
        )
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            CinematicCamera.stopAll()
        }, 276L)

        // Max-tension build-up sounds
        players.forEach { player ->
            player.playSound(player.location, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.5f)
            player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 0.9f)
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.4f)
        }

        // ── Scan island edge (cached) ─────────────────────────────────────────
        val edges = IslandScanner.scan(spawnLoc)
        val islandRadius = if (edges.isNotEmpty()) edges.maxOf { e ->
            val dx = e.x - spawnLoc.x; val dz = e.z - spawnLoc.z
            kotlin.math.sqrt(dx * dx + dz * dz)
        } else 28.0

        // ── 1. Hellfire edge glow — cliff ablaze ──────────────────────────────
        edgeGlow(players, edges,
            particleA = Particle.FLAME, particleB = Particle.LAVA,
            stackHeight = 0.8, duration = 270L)

        // ── 2. Flame pillars erupt from every edge block (t=5) ───────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.FLAME, riseHeight = 45, count = 2)
        }, 5L)

        // ── 3. Enchanted hit pillars follow (t=30) ────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.ENCHANTED_HIT, riseHeight = 35, count = 2)
        }, 30L)

        // ── 4. Crit pillars, tallest wave (t=55) ─────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.CRIT, riseHeight = 55, count = 2)
        }, 55L)

        // ── 5. MAGMA pillar wave (t=80) — hellfire fully erupts ───────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.LAVA, riseHeight = 25, count = 3)
        }, 80L)

        // ── 6. Accelerating inward shockwaves ────────────────────────────────
        for (i in 0..7) {
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                edgeShockwaveInward(players, edges, spawnLoc,
                    Pair(Particle.ENCHANTED_HIT, Particle.CRIT),
                    waves = 1, waveDelay = 10L, steps = 16 + i)
            }, i * 6L)
        }

        // ── 7. CAMPFIRE_COSY_SMOKE inward — dark suffocation (t=100) ─────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.CAMPFIRE_COSY_SMOKE, Particle.FLAME),
                waves = 3, waveDelay = 14L, steps = 18)
        }, 100L)

        // ── 8. Lava/flame interior rain with dripping layer ───────────────────
        islandRain(players, spawnLoc, Particle.LAVA, areaRadius = islandRadius, duration = 260L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.FLAME, areaRadius = islandRadius * 0.71, duration = 200L)
        }, 20L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.DRIPPING_LAVA, areaRadius = islandRadius * 0.5, duration = 150L)
        }, 40L)

        // ── 9. Climactic central burst (t=75) ────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc.clone().add(0.0, 1.0, 0.0), 5, 2.0, 0.0, 2.0, 0.0)
                player.spawnParticle(Particle.FLAME, spawnLoc.clone().add(0.0, 1.0, 0.0), 200, 5.0, 3.0, 5.0, 0.3)
                player.spawnParticle(Particle.ENCHANTED_HIT, spawnLoc.clone().add(0.0, 2.0, 0.0), 150, 4.0, 4.0, 4.0, 0.2)
                player.spawnParticle(Particle.LAVA, spawnLoc.clone().add(0.0, 1.5, 0.0), 100, 3.5, 2.5, 3.5, 0.0)
                player.spawnParticle(Particle.CRIT, spawnLoc.clone().add(0.0, 2.5, 0.0), 80, 3.0, 3.0, 3.0, 0.25)
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_DEATH, 0.7f, 1.3f)
                player.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.1f)
            }
        }, 75L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val title = Title.title(
                LanguageHandler.getMessage(LangStrings.START_TITLE_FINAL, player),
                LanguageHandler.getMessage(LangStrings.START_DESC_FINAL, player),
                Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(3200), Duration.ofMillis(1100))
            )

            player.showTitle(title)
        }
    }

    override fun phaseActions(players: List<Player>) {

    }
}