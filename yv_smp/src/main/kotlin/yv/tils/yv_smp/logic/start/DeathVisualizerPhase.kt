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
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.utils.message.MessageUtils
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.utils.CinematicCamera
import yv.tils.yv_smp.utils.IslandScanner
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeGlow
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgePillarRise
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveInward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.islandRain
import java.time.Duration

class DeathVisualizerPhase : PhasePlugin {
    override fun getPhaseDuration(): Long {
        // Duration: 12L * 4 = 48 ticks for pulsing waves + 50L for final effect + title display (3000ms = 150 ticks) + buffer = 260 ticks
        return 260L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // ── Camera: pendulum — eerie oscillating sweep, never a full revolution ─
        players.forEach { it.gameMode = org.bukkit.GameMode.SPECTATOR }
        CinematicCamera.pendulum(
            players        = players,
            center         = spawnLoc,
            orbitRadius    = 38.0,
            height         = 14.0,
            durationTicks  = 255L,
            swingHalfAngle = kotlin.math.PI / 2.5,
            swingPeriod    = 90L,
        )
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            CinematicCamera.stopAll()
        }, 256L)

        // Eerie death ambiance
        players.forEach { player ->
            player.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 0.7f, 0.6f)
            player.playSound(player.location, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.5f, 0.7f)
            player.playSound(player.location, Sound.ENTITY_PHANTOM_AMBIENT, 0.5f, 0.5f)
        }

        // ── Scan island edge (cached) ─────────────────────────────────────────
        val edges = IslandScanner.scan(spawnLoc)
        val islandRadius = if (edges.isNotEmpty()) edges.maxOf { e ->
            val dx = e.x - spawnLoc.x; val dz = e.z - spawnLoc.z
            kotlin.math.sqrt(dx * dx + dz * dz)
        } else 30.0

        // ── 1. Dark soul edge glow — cliff wreathed in soul flame ────────────
        edgeGlow(players, edges,
            particleA = Particle.SOUL, particleB = Particle.SOUL_FIRE_FLAME,
            stackHeight = 0.8, duration = 250L)

        // ── 2. Soul flame pillars erupt from every edge block (t=8) ──────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.SOUL_FIRE_FLAME, riseHeight = 40, count = 2)
        }, 8L)

        // ── 3. Sculk soul pillars follow, shorter (t=25) ─────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.SCULK_SOUL, riseHeight = 28, count = 2)
        }, 25L)

        // ── 4. WARPED_SPORE pillars at mid-phase (t=60) ───────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.WARPED_SPORE, riseHeight = 22, count = 2)
        }, 60L)

        // ── 5. Soul shockwave converges inward — death closing in (t=12) ─────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.SOUL, Particle.SMOKE), waves = 4, waveDelay = 16L, steps = 22)
        }, 12L)

        // ── 6. Second sculk pulse (t=45) ─────────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.SCULK_SOUL, Particle.LARGE_SMOKE), waves = 3, waveDelay = 20L, steps = 24)
        }, 45L)

        // ── 7. Third dark pulse — squid ink (t=120) ───────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.SQUID_INK, Particle.ASH), waves = 2, waveDelay = 18L, steps = 20)
        }, 120L)

        // ── 8. Haunting rain — ash, smoke, obsidian tears ─────────────────────
        islandRain(players, spawnLoc, Particle.ASH, areaRadius = islandRadius, duration = 240L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.SMOKE, areaRadius = islandRadius * 0.73, duration = 160L)
        }, 20L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.DRIPPING_OBSIDIAN_TEAR, areaRadius = islandRadius * 0.5, duration = 120L)
        }, 50L)

        // ── 9. Central soul burst (t=55) ─────────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.SMOKE, spawnLoc.clone().add(0.0, 1.0, 0.0), 100, 4.0, 2.0, 4.0, 0.08)
                player.spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc.clone().add(0.0, 0.5, 0.0), 80, 3.5, 2.5, 3.5, 0.05)
                player.spawnParticle(Particle.LARGE_SMOKE, spawnLoc.clone().add(0.0, 0.5, 0.0), 60, 3.0, 1.5, 3.0, 0.03)
                player.spawnParticle(Particle.SQUID_INK, spawnLoc.clone().add(0.0, 1.0, 0.0), 40, 2.5, 1.5, 2.5, 0.05)
                player.playSound(player.location, Sound.PARTICLE_SOUL_ESCAPE, 0.8f, 0.4f)
                player.playSound(player.location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.6f, 0.5f)
            }
        }, 55L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val title = Title.title(
                LanguageHandler.getMessage(LangStrings.START_TITLE_DEATH, player),
                LanguageHandler.getMessage(LangStrings.START_DESC_DEATH, player),
                Title.Times.times(Duration.ofMillis(800), Duration.ofMillis(3000), Duration.ofMillis(900))
            )

            player.showTitle(title)
        }
    }

    override fun phaseActions(players: List<Player>) {
        createTracker()
        addVisualisation()
    }

    private fun createTracker() {
        val scoreboardManager = Bukkit.getScoreboardManager()
        val scoreboard = scoreboardManager.mainScoreboard

        // TODO: Test if this if statement is actually needed, or if the unregister calls can just be made without checking for null first
//        if (scoreboard.getObjective("deaths") != null || scoreboard.getObjective("deaths_two") != null) {
            scoreboard.getObjective("deaths")?.unregister()
            scoreboard.getObjective("deaths_two")?.unregister()
//        }

        scoreboard.registerNewObjective("deaths", Criteria.DEATH_COUNT, MessageUtils.convert("<red>☠"))
        scoreboard.registerNewObjective("deaths_two", Criteria.DEATH_COUNT, MessageUtils.convert("<red>☠"))
    }

    private fun addVisualisation() {
        val scoreboardManager = Bukkit.getScoreboardManager()
        val scoreboard = scoreboardManager.mainScoreboard
        val deathCount = scoreboard.getObjective("deaths")
        val deathCountTwo = scoreboard.getObjective("deaths_two")

        if (deathCount == null || deathCountTwo == null) {
            createTracker()
            return
        }

        deathCount.displaySlot = DisplaySlot.PLAYER_LIST
        deathCountTwo.displaySlot = DisplaySlot.BELOW_NAME
    }
}