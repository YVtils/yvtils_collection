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
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.utils.CinematicCamera
import yv.tils.yv_smp.utils.IslandScanner
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeGlow
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgePillarRise
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveInward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveOutward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.islandRain
import yv.tils.yv_smp.utils.ParticleEffects.Companion.particleCountdown
import java.time.Duration

class FinalPhasePartTwo : PhasePlugin {
    override fun getPhaseDuration(): Long {
        // Duration: 5L * 10 = 50 ticks for shockwave rings + 60L for final burst + title display (2800ms = 140 ticks) + buffer = 280 ticks
        return 280L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // Dolly in close and fast — the grand finale reveal
        CinematicCamera.dollyIn(players, spawnLoc, startRadius = 45.0, endRadius = 10.0, height = 15.0, durationTicks = 280L)

        // ── Scan island edge (cached) ─────────────────────────────────────────
        val edges = IslandScanner.scan(spawnLoc)
        val islandRadius = if (edges.isNotEmpty()) edges.maxOf { e ->
            val dx = e.x - spawnLoc.x; val dz = e.z - spawnLoc.z
            kotlin.math.sqrt(dx * dx + dz * dz)
        } else 32.0
        players.forEach { player ->
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f)
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_DEATH, 0.6f, 1.5f)
            player.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 0.8f, 1.2f)
        }

        // ── 1. Grand golden edge glow for the full phase ──────────────────────
        edgeGlow(players, edges,
            particleA = Particle.GLOW, particleB = Particle.WAX_ON,
            stackHeight = 0.8, duration = 270L)

        // ── 2. Edge pulse reveals (existing, kept) ────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            IslandScanner.edgePulse(spawnLoc, players, Particle.TOTEM_OF_UNDYING, pulseHeight = 40.0)
        }, 8L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            IslandScanner.edgePulse(spawnLoc, players, Particle.WAX_ON, pulseHeight = 50.0)
        }, 20L)

        // ── 3. Monumental totem pillars from every edge block ─────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.TOTEM_OF_UNDYING, riseHeight = 60, count = 3)
        }, 5L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.WAX_ON, riseHeight = 45, count = 2)
        }, 20L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.GLOW, riseHeight = 50, count = 2)
        }, 38L)

        // ── 4. Expanding outward shockwave crescendo ──────────────────────────
        for (i in 0..10) {
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                edgeShockwaveOutward(players, edges, spawnLoc,
                    Pair(Particle.GLOW, Particle.WAX_ON),
                    waves = 1, waveDelay = 8L, steps = 16, overshoot = 1.3 + i * 0.05)
                players.forEach { p -> p.playSound(p.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 1.0f + (i * 0.1f)) }
            }, i * 5L)
        }

        // ── 5. Inward convergence — all glory focused to the center (t=30) ────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.TOTEM_OF_UNDYING, Particle.ENCHANT),
                waves = 3, waveDelay = 15L, steps = 24)
        }, 30L)

        // ── 6. Interior glory rain ────────────────────────────────────────────
        islandRain(players, spawnLoc, Particle.END_ROD, areaRadius = islandRadius, duration = 260L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.WAX_ON, areaRadius = islandRadius * 0.75, duration = 200L)
        }, 15L)

        // ── 7. ULTIMATE central detonation (t=60) ────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc.clone().add(0.0, 1.0, 0.0), 8, 3.0, 0.0, 3.0, 0.0)
                player.spawnParticle(Particle.WAX_ON, spawnLoc.clone().add(0.0, 2.0, 0.0), 300, 6.0, 4.0, 6.0, 0.4)
                player.spawnParticle(Particle.GLOW, spawnLoc.clone().add(0.0, 2.0, 0.0), 250, 5.5, 4.0, 5.5, 0.3)
                player.spawnParticle(Particle.TOTEM_OF_UNDYING, spawnLoc.clone().add(0.0, 1.5, 0.0), 200, 5.0, 4.0, 5.0, 0.3)
                player.spawnParticle(Particle.ENCHANT, spawnLoc.clone().add(0.0, 1.0, 0.0), 150, 4.5, 3.0, 4.5, 1.5)
                player.spawnParticle(Particle.END_ROD, spawnLoc.clone().add(0.0, 2.0, 0.0), 120, 4.0, 4.0, 4.0, 0.2)
                player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.2f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 2.0f)
            }
        }, 60L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val readyTitle = Title.title(
                LanguageHandler.getMessage(LangStrings.START_TITLE_FINAL_2, player),
                LanguageHandler.getMessage(LangStrings.START_DESC_FINAL_2, player),
                Title.Times.times(Duration.ofMillis(1200), Duration.ofMillis(2800), Duration.ofMillis(1600))
            )

            player.showTitle(readyTitle)
        }
    }

    override fun phaseActions(players: List<Player>) {
        val world    = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation
        val edges    = IslandScanner.scan(spawnLoc)

        // ── Particle countdown: 3 → 2 → 1 → GO ───────────────────────────────
        // Each digit shows for 15 ticks, 5-tick gap, then the next.
        // digitShowTicks=15 + digitGapTicks=5 = 20 ticks per step.
        // Digit "3" starts at t=0  → visible t=0..15
        // Digit "2" starts at t=20 → visible t=20..35
        // Digit "1" starts at t=40 → visible t=40..55
        // "GO"       starts at t=60 → visible t=60..75
        val surfaceY = world.getHighestBlockYAt(spawnLoc.blockX, spawnLoc.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE).toDouble()
        val countdownCenter = spawnLoc.clone().apply { y = surfaceY }

        particleCountdown(
            players        = players,
            center         = countdownCenter,
            startDelay     = 0L,
            digitShowTicks = 15L,
            digitGapTicks  = 5L,
            baseHeight     = 12.0,
            scale          = 1.4,
        )

        // ── "3" — amber inward shockwave + low bell chord (t=0) ──────────────
        players.forEach { p ->
            p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_BELL,  0.9f, 0.8f)
            p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.0f)
        }
        edgeShockwaveInward(players, edges, spawnLoc,
            Pair(Particle.ELECTRIC_SPARK, Particle.WAX_ON),
            waves = 1, waveDelay = 0L, steps = 18)

        // ── "2" — orange outward burst + rising bell (t=20) ──────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { p ->
                p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_BELL,  0.95f, 1.2f)
                p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f,  1.4f)
            }
            edgeShockwaveOutward(players, edges, spawnLoc,
                Pair(Particle.FLAME, Particle.ELECTRIC_SPARK),
                waves = 1, waveDelay = 0L, steps = 18, overshoot = 1.5)
        }, 20L)

        // ── "1" — red inward implosion + sharp bell (t=40) ───────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { p ->
                p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_BELL,  1.0f, 1.6f)
                p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.8f)
            }
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.CRIT, Particle.ENCHANTED_HIT),
                waves = 1, waveDelay = 0L, steps = 16)
        }, 40L)

        // ── "GO" — green outward explosion + triumphal chord (t=60) ─────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc.clone().add(0.0, 1.0, 0.0), 10, 4.0, 0.0, 4.0, 0.0)
                player.spawnParticle(Particle.WAX_ON,          spawnLoc.clone().add(0.0, 1.5, 0.0), 200, 6.0, 4.0, 6.0, 0.3)
                player.spawnParticle(Particle.GLOW,            spawnLoc.clone().add(0.0, 2.0, 0.0), 150, 5.5, 4.0, 5.5, 0.2)
                player.spawnParticle(Particle.TOTEM_OF_UNDYING, spawnLoc.clone().add(0.0, 1.5, 0.0), 120, 5.0, 4.0, 5.0, 0.2)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL,  0.9f, 2.0f)
                player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.2f)
            }
            edgeShockwaveOutward(players, edges, spawnLoc,
                Pair(Particle.END_ROD, Particle.GLOW), waves = 3, waveDelay = 12L, steps = 22, overshoot = 1.8)
        }, 60L)

        // Stop camera and restore players after the GO burst has landed
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            CinematicCamera.stopAll()
            players.forEach { player ->
                player.clearActivePotionEffects()
                players.forEach { other -> if (other != player) player.showPlayer(Data.instance, other) }
                val restored = PreparationPhase.savedGameModes.remove(player.uniqueId)
                if (restored != null) player.gameMode = restored
            }
        }, 75L)
    }
}