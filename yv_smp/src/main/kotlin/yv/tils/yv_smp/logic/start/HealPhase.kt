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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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

class HealPhase : PhasePlugin {
    override fun getPhaseDuration(): Long {
        return 200L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world    = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // ── Camera: rising orbit — ground level lifts to sky as health returns ─
        players.forEach { player -> player.gameMode = org.bukkit.GameMode.SPECTATOR }
        CinematicCamera.orbitRising(
            players             = players,
            center              = spawnLoc,
            orbitRadius         = 38.0,
            startHeight         = 6.0,
            endHeight           = 42.0,
            durationTicks       = 195L,
            angularSpeedPerTick = 0.005,
        )
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            CinematicCamera.stopAll()
        }, 196L)

        // Healing ambiance sounds
        players.forEach { player ->
            player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f)
            player.playSound(player.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.5f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f)
        }

        // ── Scan island edge (cached) ─────────────────────────────────────────
        val edges = IslandScanner.scan(spawnLoc)
        val islandRadius = if (edges.isNotEmpty()) edges.maxOf { e ->
            val dx = e.x - spawnLoc.x; val dz = e.z - spawnLoc.z
            kotlin.math.sqrt(dx * dx + dz * dz)
        } else 28.0

        // ── 1. Soft golden-green edge glow for the whole phase (t=0) ─────────
        // WAX_ON on the cliff face + GLOW floating above — warm healing colour.
        edgeGlow(players, edges,
            particleA    = Particle.WAX_ON,
            particleB    = Particle.GLOW,
            stackHeight  = 0.8,
            duration     = 195L,
            repeatInterval = 2L,
        )

        // ── 2. Healing pillars erupt from the edge simultaneously (t=5) ──────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.WAX_ON, riseHeight = 35, count = 2)
        }, 5L)

        // ── 3. TOTEM pillars follow, shorter, inner warmth (t=20) ─────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.TOTEM_OF_UNDYING, riseHeight = 25, count = 2)
        }, 20L)

        // ── 4. Heal pulse converges inward from the edge (t=15) ───────────────
        // Mimics health flowing in from the shoreline toward the center.
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.WAX_ON, Particle.GLOW), waves = 3, waveDelay = 20L, steps = 24)
        }, 15L)

        // ── 5. Second slower pulse, nectar-themed (t=60) ──────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.FALLING_NECTAR, Particle.GLOW), waves = 2, waveDelay = 25L, steps = 28)
        }, 60L)

        // ── 6. Nectar rain falls over the whole island interior (t=0 & t=30) ──
        islandRain(players, spawnLoc, Particle.FALLING_NECTAR, areaRadius = islandRadius, duration = 180L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.WAX_ON, areaRadius = islandRadius * 0.65, duration = 120L)
        }, 30L)

        // ── 7. Central healing burst (t=50) ───────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    spawnLoc.clone().add(0.0, 1.0, 0.0), 150, 3.0, 2.0, 3.0, 0.3)
                player.spawnParticle(Particle.WAX_ON,
                    spawnLoc.clone().add(0.0, 2.0, 0.0), 100, 4.0, 3.0, 4.0, 0.2)
                player.spawnParticle(Particle.GLOW,
                    spawnLoc.clone().add(0.0, 1.5, 0.0), 80, 3.5, 2.5, 3.5, 0.1)
                player.spawnParticle(Particle.HEART,
                    spawnLoc.clone().add(0.0, 2.5, 0.0), 40, 3.0, 1.5, 3.0, 0.1)
                player.playSound(player.location, Sound.BLOCK_CONDUIT_ACTIVATE, 0.8f, 1.5f)
            }
        }, 50L)

        // ── 8. Cherry blossom rain (t=0 & t=40) — lush, organic healing ───────
        islandRain(players, spawnLoc, Particle.CHERRY_LEAVES, areaRadius = islandRadius * 0.8, duration = 195L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.COMPOSTER, Particle.FALLING_NECTAR), waves = 2, waveDelay = 22L, steps = 26)
        }, 100L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val title = Title.title(
                LanguageHandler.getMessage(LangStrings.START_TITLE_HEAL, player),
                LanguageHandler.getMessage(LangStrings.START_DESC_HEAL, player),
                Title.Times.times(Duration.ofMillis(800), Duration.ofMillis(2800), Duration.ofMillis(900))
            )
            player.showTitle(title)
        }
    }

    override fun phaseActions(players: List<Player>) {
        players.forEach { player ->
            player.addPotionEffect(
                PotionEffect(PotionEffectType.INSTANT_HEALTH, 3, 20,
                    false, false, false)
            )
            player.foodLevel = 20
            player.saturation = 5f
        }
    }
}