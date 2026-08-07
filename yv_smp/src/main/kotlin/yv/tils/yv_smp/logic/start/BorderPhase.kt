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
import yv.tils.yv_smp.logic.BorderHandler
import yv.tils.yv_smp.utils.CinematicCamera
import yv.tils.yv_smp.utils.IslandScanner
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeBurstOutward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeFlyOut
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeGlow
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveInward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveOutward
import java.time.Duration

class BorderPhase : PhasePlugin {
    override fun getPhaseDuration(): Long {
        return 240L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world    = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // ── Camera: overhead dolly — starts high and wide, slowly pushes toward
        // the edge so the outward expansion fills the whole frame ─────────────
        players.forEach { it.gameMode = org.bukkit.GameMode.SPECTATOR }
        CinematicCamera.spiralDown(
            players             = players,
            center              = spawnLoc,
            orbitRadius         = 52.0,
            startHeight         = 48.0,
            endHeight           = 20.0,
            durationTicks       = 235L,
            angularSpeedPerTick = 0.005,
        )
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            CinematicCamera.stopAll()
        }, 236L)

        // Thunderous border-expansion sounds
        players.forEach { player ->
            player.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 1.0f, 0.7f)
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 0.4f)
        }

        val edges = IslandScanner.scan(spawnLoc)

        // ── 1. Edge glow — border crackles alive at the cliff ─────────────────
        edgeGlow(players, edges,
            particleA = Particle.END_ROD, particleB = Particle.ELECTRIC_SPARK,
            stackHeight = 0.7, duration = 230L)

        // ── 2. Instant outward burst — the border snaps open (t=0) ───────────
        edgeBurstOutward(players, edges, spawnLoc, Particle.END_ROD, speed = 0.5, count = 5)
        edgeBurstOutward(players, edges, spawnLoc, Particle.ELECTRIC_SPARK, speed = 0.3, count = 4)

        // ── 3. Border wall flying outward — END_ROD stream (t=5) ─────────────
        // Particles travel horizontally away from the island like the border wall
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeFlyOut(players, edges, spawnLoc,
                particle    = Particle.END_ROD,
                travelBlocks = 35,
                waves       = 3,
                waveDelay   = 18L,
                count       = 2)
        }, 5L)

        // ── 4. GLOW stream follows the END_ROD wall (t=20) ───────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeFlyOut(players, edges, spawnLoc,
                particle    = Particle.GLOW,
                travelBlocks = 28,
                waves       = 2,
                waveDelay   = 22L,
                count       = 2)
        }, 20L)

        // ── 5. ELECTRIC_SPARK stream — sparks trail the border (t=35) ────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeFlyOut(players, edges, spawnLoc,
                particle    = Particle.ELECTRIC_SPARK,
                travelBlocks = 22,
                waves       = 2,
                waveDelay   = 20L,
                count       = 1)
        }, 35L)

        // ── 6. Second outward burst snap (t=60) ───────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeBurstOutward(players, edges, spawnLoc, Particle.CRIT, speed = 0.5, count = 5)
            edgeShockwaveOutward(players, edges, spawnLoc,
                Pair(Particle.END_ROD, Particle.GLOW), waves = 2, waveDelay = 12L, steps = 16, overshoot = 1.5)
        }, 60L)

        // ── 7. Inward compression — shockwave returns to center (t=90) ────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.GLOW, Particle.END_ROD), waves = 2, waveDelay = 14L, steps = 20)
        }, 90L)

        // ── 8. Final GLOW_SQUID_INK inward pulse (t=130) ─────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.GLOW_SQUID_INK, Particle.ELECTRIC_SPARK), waves = 2, waveDelay = 12L, steps = 18)
        }, 130L)

        // ── 9. Central burst (t=40) ───────────────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.END_ROD,       spawnLoc.clone().add(0.0, 1.0, 0.0), 200, 5.0, 3.0, 5.0, 0.2)
                player.spawnParticle(Particle.ELECTRIC_SPARK, spawnLoc.clone().add(0.0, 2.0, 0.0), 150, 4.0, 3.0, 4.0, 0.15)
                player.spawnParticle(Particle.GLOW,          spawnLoc.clone().add(0.0, 1.5, 0.0), 100, 4.5, 2.5, 4.5, 0.1)
                player.spawnParticle(Particle.CRIT,          spawnLoc.clone().add(0.0, 2.0, 0.0),  80, 3.5, 2.5, 3.5, 0.12)
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f)
            }
        }, 40L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val title = Title.title(
                LanguageHandler.getMessage(LangStrings.START_TITLE_BORDER, player),
                LanguageHandler.getMessage(LangStrings.START_DESC_BORDER, player),
                Title.Times.times(Duration.ofMillis(800), Duration.ofMillis(3000), Duration.ofMillis(900))
            )

            player.showTitle(title)
        }
    }

    override fun phaseActions(players: List<Player>) {
        BorderHandler().changeBorderSize(20000000.0, 10)
    }
}