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
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.utils.CinematicCamera
import yv.tils.yv_smp.utils.IslandScanner
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeGlow
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgePillarRise
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveInward
import yv.tils.yv_smp.utils.ParticleEffects.Companion.islandRain
import java.time.Duration

class ItemClearPhase : PhasePlugin {
    override fun getPhaseDuration(): Long {
        // Duration: 4L * 8 = 32 ticks for expanding circles + 10L for explosion + title display (2500ms = 125 ticks) + buffer = 180 ticks
        return 180L
    }

    override fun cinematicEffects(players: List<Player>) {
        val world = players.firstOrNull()?.world ?: return
        val spawnLoc = world.spawnLocation

        // ── Camera: spiral descent — overhead crane dropping through the smoke ─
        players.forEach { it.gameMode = org.bukkit.GameMode.SPECTATOR }
        CinematicCamera.spiralDown(
            players             = players,
            center              = spawnLoc,
            orbitRadius         = 40.0,
            startHeight         = 55.0,
            endHeight           = 8.0,
            durationTicks       = 175L,
            angularSpeedPerTick = 0.007,
        )
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            CinematicCamera.stopAll()
        }, 176L)

        // Dramatic purge sounds
        players.forEach { player ->
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f)
            player.playSound(player.location, Sound.BLOCK_CHEST_CLOSE, 0.9f, 0.6f)
        }

        // ── Scan island edge (cached) ─────────────────────────────────────────
        val edges = IslandScanner.scan(spawnLoc)
        val islandRadius = if (edges.isNotEmpty()) edges.maxOf { e ->
            val dx = e.x - spawnLoc.x; val dz = e.z - spawnLoc.z
            kotlin.math.sqrt(dx * dx + dz * dz)
        } else 28.0

        // ── 1. Electric edge glow — items being incinerated at the cliff ──────
        edgeGlow(players, edges,
            particleA = Particle.ELECTRIC_SPARK, particleB = Particle.FLAME,
            stackHeight = 0.7, duration = 170L)

        // ── 2. Flame pillars erupt from every edge block (t=6) ────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.FLAME, riseHeight = 30, count = 2)
        }, 6L)

        // ── 3. Smoke follows (t=18) ───────────────────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.CLOUD, riseHeight = 20, count = 2)
        }, 18L)

        // ── 4. Campfire signal columns erupt from center outward (t=25) ───────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgePillarRise(players, edges, Particle.CAMPFIRE_SIGNAL_SMOKE, riseHeight = 40, count = 1)
        }, 25L)

        // ── 5. Electric shockwave converges inward (t=10) ─────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.ELECTRIC_SPARK, Particle.CLOUD), waves = 4, waveDelay = 12L, steps = 20)
        }, 10L)

        // ── 6. Second tighter shockwave — SCRAPE + SMOKE (t=45) ──────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            edgeShockwaveInward(players, edges, spawnLoc,
                Pair(Particle.SCRAPE, Particle.SMOKE), waves = 3, waveDelay = 10L, steps = 16)
        }, 45L)

        // ── 7. Interior smoke + dripping lava rain ────────────────────────────
        islandRain(players, spawnLoc, Particle.CLOUD, areaRadius = islandRadius, duration = 160L)
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            islandRain(players, spawnLoc, Particle.DRIPPING_LAVA, areaRadius = islandRadius * 0.6, duration = 100L)
        }, 20L)

        // ── 8. Central detonation burst (t=35) ───────────────────────────────
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            players.forEach { player ->
                player.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc.clone().add(0.0, 1.0, 0.0), 4, 1.0, 0.0, 1.0, 0.0)
                player.spawnParticle(Particle.ELECTRIC_SPARK, spawnLoc.clone().add(0.0, 1.5, 0.0), 120, 4.0, 2.0, 4.0, 0.15)
                player.spawnParticle(Particle.CLOUD, spawnLoc.clone().add(0.0, 1.0, 0.0), 80, 3.5, 2.0, 3.5, 0.05)
                player.spawnParticle(Particle.FLAME, spawnLoc.clone().add(0.0, 0.5, 0.0), 60, 3.0, 1.5, 3.0, 0.1)
                player.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, spawnLoc.clone().add(0.0, 1.0, 0.0), 30, 2.0, 1.0, 2.0, 0.05)
                player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f)
                player.playSound(player.location, Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 0.7f)
            }
        }, 35L)
    }

    override fun titleEffects(players: List<Player>) {
        players.forEach { player ->
            val title = Title.title(
                LanguageHandler.getMessage(LangStrings.START_TITLE_ITEMCLEAR, player),
                LanguageHandler.getMessage(LangStrings.START_DESC_ITEMCLEAR, player),
                Title.Times.times(Duration.ofMillis(600), Duration.ofMillis(2500), Duration.ofMillis(700))
            )

            player.showTitle(title)
        }
    }

    override fun phaseActions(players: List<Player>) {
        clearGroundItems()
        clearPlayerInventories(players)
    }

    private fun clearPlayerInventories(players: List<Player>) {
        players.forEach { player ->
            player.inventory.clear()
            player.inventory.armorContents = emptyArray()
            player.enderChest.clear()

            player.exp = 0.0f
            player.level = 0
        }
    }

    private fun clearGroundItems() {
        val worlds = Data.instance.server.worlds
        worlds.forEach { world ->
            world.entities.filterIsInstance<Item>().forEach {
                val itemPos = it.location
                world.spawnParticle(Particle.EXPLOSION, itemPos, 5, 0.3, 0.3, 0.3, 0.0)
                world.spawnParticle(Particle.SMOKE, itemPos.add(0.0, 0.5, 0.0), 10, 0.3, 0.5, 0.3, 0.02)
                it.remove()
            }
        }
    }
}