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
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
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

class PreparationPhase : PhasePlugin {
    companion object {
        /** Game modes to restore after spectator cinematic ends (set in phaseActions, read in FinalPhasePartTwo). */
        val savedGameModes = mutableMapOf<java.util.UUID, GameMode>()
    }

    override fun getPhaseDuration(): Long {
        // Duration: 12L * 6 = 72 ticks for title loop + island cinematic runs for 100 ticks + buffer = 140 ticks
        return 140L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // ── Cinematic camera: descend from sky to island surface ─────────────
        // Starts 100 blocks above the spawn surface, eases down over the full
        // phase duration (140 ticks = 7 s) so the player watches the edge
        // pillars and glow effects below them as they drop toward the island.
        val surfaceY = world.getHighestBlockYAt(
            spawnLoc.blockX, spawnLoc.blockZ, org.bukkit.HeightMap.WORLD_SURFACE
        ).toDouble() + 1.0
        val landingLoc = spawnLoc.clone().apply { y = surfaceY }
        CinematicCamera.descentToSpawn(
            players         = players,
            center          = spawnLoc,
            startHeight     = 100.0,
            durationTicks   = 140L,
            landingLoc      = landingLoc,
            restoreGameMode = false,
        )

        // Dramatic boot sounds
        players.forEach { player ->
            player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 0.9f, 0.6f)
            player.playSound(player.location, Sound.BLOCK_CONDUIT_ACTIVATE, 0.7f, 0.8f)
            player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, 0.4f, 1.3f)
        }

        // ── Scan the island edge (cached after first run) ─────────────────────
        val edges = IslandScanner.scan(spawnLoc)

        // Derive the island radius from the actual edge scan so all radius-
        // dependent effects scale with the real island shape.
        val islandRadius = if (edges.isNotEmpty()) {
            edges.maxOf { e ->
                val dx = e.x - spawnLoc.x; val dz = e.z - spawnLoc.z
                kotlin.math.sqrt(dx * dx + dz * dz)
            }
        } else 30.0

        // ── 1. Persistent glowing outline for the whole phase ─────────────────
        edgeGlow(players, edges, Particle.GLOW, Particle.END_ROD, stackHeight = 0.7, duration = 135L)

        // ── 2. Dramatic reveal: pillars rise from every edge block (t=0) ──────
        edgePillarRise(players, edges, Particle.END_ROD, riseHeight = 45, count = 2)

        // ── 3. Second pillar wave with ELECTRIC_SPARK at t=20 ─────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.ELECTRIC_SPARK, riseHeight = 30, count = 2)
        }, 20L)

        // ── 4. Shockwave rings converging inward from the edge (t=10) ─────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.GLOW, Particle.SCRAPE), waves = 4, waveDelay = 18L, steps = 22)
        }, 10L)

        // ── 5. Second inward shockwave, tighter, later (t=50) ─────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.END_ROD, Particle.WAX_ON), waves = 3, waveDelay = 15L, steps = 18)
        }, 50L)

        // ── 6. Rain over the island interior scaled to actual island size ──────
        islandRain(players, spawnLoc, Particle.GLOW, areaRadius = islandRadius, duration = 120L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.ELECTRIC_SPARK,
                areaRadius = islandRadius * 0.65, duration = 80L)
        }, 30L)

        // ── 7. Central climactic burst at t=60 ────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc, 3, 0.5, 0.0, 0.5, 0.0)
                player.spawnParticle(Particle.TOTEM_OF_UNDYING, spawnLoc.clone().add(0.0, 1.0, 0.0), 200, 3.0, 2.0, 3.0, 0.4)
                player.spawnParticle(Particle.WAX_ON, spawnLoc.clone().add(0.0, 1.0, 0.0), 150, 4.0, 3.0, 4.0, 0.3)
                player.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 0.8f, 0.7f)
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.3f)
            }
        }, 60L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val bootTexts = listOf(LangStrings.START_PLACEHOLDER_BOOTING, LangStrings.START_PLACEHOLDER_BOOTING, LangStrings.START_PLACEHOLDER_BOOTING, LangStrings.START_PLACEHOLDER_BOOTING, LangStrings.START_PLACEHOLDER_ONLINE, LangStrings.START_PLACEHOLDER_READY)
            val colors = listOf("<#00ff00>", "<#00ff44>", "<#00ff88>", "<#00ffcc>", "<#00ffff>", "<#00ffff>")
            val subtitles = listOf(
                LangStrings.START_DESC_PREPARE_1,
                LangStrings.START_DESC_PREPARE_2,
                LangStrings.START_DESC_PREPARE_3,
                LangStrings.START_DESC_PREPARE_4,
                LangStrings.START_DESC_PREPARE_5,
                LangStrings.START_DESC_PREPARE_6,
            )

            var delay = 0L
            bootTexts.forEachIndexed { index, text ->
                Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                    val title = Title.title(
                        MessageUtils.convert("${colors[index]}${LanguageHandler.getRawMessage(text, player)}${".".repeat(index.coerceIn(1, 3))}"),
                        MessageUtils.convert("<gradient:#666666:#aaaaaa>${LanguageHandler.getRawMessage(subtitles[index], player)}</gradient>"),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(600), Duration.ofMillis(200))
                    )
                    player.showTitle(title)

                    timedSounds(player, index)
                }, delay)
                delay += 12L
            }
        }
    }

    private fun timedSounds(player: Player, index: Int) {
        val pitch = 1.2f + (index * 0.15f)
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.4f, pitch)
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.2f, pitch)
        if (index >= 4) {
            // ONLINE/Ready chime
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f)
        }
    }

    override fun phaseActions(players: List<Player>) {
    }
}