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

package yv.tils.yv_smp.utils

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import yv.tils.utils.data.Data
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ParticleEffects {
    companion object {

        // ── 5×3 pixel font (rows top→bottom, cols left→right) ─────────────────
        // Each glyph is 5 rows of 3 booleans (true = filled pixel).
        private val GLYPHS = mapOf(
            '3' to arrayOf(
                booleanArrayOf(true,  true,  true),
                booleanArrayOf(false, false, true),
                booleanArrayOf(true,  true,  true),
                booleanArrayOf(false, false, true),
                booleanArrayOf(true,  true,  true),
            ),
            '2' to arrayOf(
                booleanArrayOf(true,  true,  true),
                booleanArrayOf(false, false, true),
                booleanArrayOf(true,  true,  true),
                booleanArrayOf(true,  false, false),
                booleanArrayOf(true,  true,  true),
            ),
            '1' to arrayOf(
                booleanArrayOf(false, true,  false),
                booleanArrayOf(true,  true,  false),
                booleanArrayOf(false, true,  false),
                booleanArrayOf(false, true,  false),
                booleanArrayOf(true,  true,  true),
            ),
            'G' to arrayOf(
                booleanArrayOf(true,  true,  true),
                booleanArrayOf(true,  false, false),
                booleanArrayOf(true,  false, true),
                booleanArrayOf(true,  false, true),
                booleanArrayOf(true,  true,  true),
            ),
            'O' to arrayOf(
                booleanArrayOf(true,  true,  true),
                booleanArrayOf(true,  false, true),
                booleanArrayOf(true,  false, true),
                booleanArrayOf(true,  false, true),
                booleanArrayOf(true,  true,  true),
            ),
        )

        /**
         * Draws a single [glyph] as DUST particles floating [baseHeight] blocks
         * above [center]. The digit is [scale] blocks per pixel wide/tall,
         * faces horizontally in the XZ plane and repeats every [repeatEvery] ticks
         * for [displayTicks] ticks so it stays visible without blinking.
         *
         * [color] — the DUST colour for this digit.
         */
        fun particleGlyph(
            players: List<Player>,
            center: org.bukkit.Location,
            glyph: Char,
            color: Color = Color.WHITE,
            baseHeight: Double = 14.0,
            scale: Double = 1.2,
            displayTicks: Long = 16L,
            repeatEvery: Long = 2L,
            xOffset: Double = 0.0,
        ) {
            val rows = GLYPHS[glyph.uppercaseChar()] ?: return
            val dust = Particle.DustOptions(color, scale.toFloat().coerceIn(0.5f, 4f))
            // Build pixel positions once
            val cols = rows[0].size
            val glyphOffsetX = xOffset - (cols - 1) * scale * 0.5
            val positions = mutableListOf<Triple<Double, Double, Double>>()
            for (row in rows.indices) {
                for (col in rows[row].indices) {
                    if (rows[row][col]) {
                        val px = center.x + glyphOffsetX + col * scale
                        val py = center.y + baseHeight + (rows.size - 1 - row) * scale
                        val pz = center.z
                        positions.add(Triple(px, py, pz))
                    }
                }
            }
            val taskHolder = IntArray(1)
            taskHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, Runnable {
                positions.forEach { (px, py, pz) ->
                    players.forEach { p -> p.spawnParticle(Particle.DUST, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0, dust) }
                }
            }, 0L, repeatEvery)
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                Bukkit.getScheduler().cancelTask(taskHolder[0])
            }, displayTicks)
        }

        /**
         * Fires a 3 → 2 → 1 → GO particle countdown above the island.
         *
         * Each digit is shown for [digitShowTicks] ticks then cleared for
         * [digitGapTicks] ticks before the next appears.
         * The first digit fires at [startDelay] ticks after the call.
         *
         * Returns the tick offset at which "GO" finishes, so the caller can
         * schedule the game-start actions directly after it.
         *
         * Colours escalate: 3 = amber → 2 = orange → 1 = red → GO = green.
         */
        fun particleCountdown(
            players: List<Player>,
            center: org.bukkit.Location,
            startDelay: Long = 0L,
            digitShowTicks: Long = 15L,
            digitGapTicks: Long = 5L,
            baseHeight: Double = 14.0,
            scale: Double = 1.3,
        ) {
            val step = digitShowTicks + digitGapTicks

            // ── 3 ──
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                particleGlyph(players, center, '3',
                    color = Color.fromRGB(255, 180, 0),
                    baseHeight = baseHeight, scale = scale,
                    displayTicks = digitShowTicks)
            }, startDelay)

            // ── 2 ──
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                particleGlyph(players, center, '2',
                    color = Color.fromRGB(255, 90, 0),
                    baseHeight = baseHeight, scale = scale,
                    displayTicks = digitShowTicks)
            }, startDelay + step)

            // ── 1 ──
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                particleGlyph(players, center, '1',
                    color = Color.fromRGB(255, 20, 20),
                    baseHeight = baseHeight, scale = scale,
                    displayTicks = digitShowTicks)
            }, startDelay + step * 2)

            // ── GO — two letters side by side ──
            val goGap = 4.0 * scale   // horizontal gap between G and O
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                particleGlyph(players, center, 'G',
                    color = Color.fromRGB(0, 255, 80),
                    baseHeight = baseHeight, scale = scale,
                    displayTicks = digitShowTicks,
                    xOffset = -goGap * 0.5)
                particleGlyph(players, center, 'O',
                    color = Color.fromRGB(0, 255, 80),
                    baseHeight = baseHeight, scale = scale,
                    displayTicks = digitShowTicks,
                    xOffset = goGap * 0.5)
            }, startDelay + step * 3)
        }

        fun islandRain(
            players: List<Player>,
            center: org.bukkit.Location,
            particle: Particle,
            areaRadius: Double = 35.0,
            duration: Long = 80L,
        ) {
            val world = center.world ?: return
            runCatching {
                val taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, {
                    repeat(30) {
                        val ox = (Math.random() - 0.5) * areaRadius * 2
                        val oz = (Math.random() - 0.5) * areaRadius * 2
                        val px = center.x + ox
                        val pz = center.z + oz
                        // Use actual surface Y at the rain drop position so rain
                        // always starts above the island, not from spawn Y.
                        val surfaceY = world.getHighestBlockYAt(px.toInt(), pz.toInt(),
                            org.bukkit.HeightMap.WORLD_SURFACE).toDouble()
                        val py = surfaceY + 8.0 + Math.random() * 4
                        players.forEach { player ->
                            player.spawnParticle(particle, px, py, pz, 1, 0.0, -0.5, 0.0, 0.0)
                        }
                    }
                }, 0L, 1L)

                Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                    Bukkit.getScheduler().cancelTask(taskId)
                }, duration)
            }
        }

        /**
         * Continuously emits two stacked particle layers directly on each
         * scanned edge block for the given [duration].
         *
         *   • [particleA] — sits exactly on the cliff-top face (base layer).
         *   • [particleB] — floats [stackHeight] blocks above, giving the
         *                   outline visible height from the cinematic camera.
         */
        fun edgeGlow(
            players: List<Player>,
            edges: List<org.bukkit.Location>,
            particleA: Particle = Particle.GLOW,
            particleB: Particle = Particle.END_ROD,
            stackHeight: Double = 0.7,
            duration: Long = 130L,
            repeatInterval: Long = 2L,
        ) {
            if (edges.isEmpty()) return
            val upper = edges.map { it.clone().add(0.0, stackHeight, 0.0) }
            val taskHolder = IntArray(1)
            taskHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, {
                edges.forEach { loc ->
                    players.forEach { p -> p.spawnParticle(particleA, loc, 1, 0.0, 0.0, 0.0, 0.0) }
                }
                upper.forEach { loc ->
                    players.forEach { p -> p.spawnParticle(particleB, loc, 1, 0.0, 0.0, 0.0, 0.0) }
                }
            }, 0L, repeatInterval)
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                Bukkit.getScheduler().cancelTask(taskHolder[0])
            }, duration)
        }

        /**
         * Fires a wall of [particle] rising from every edge block upward.
         * Each height step is delayed by one tick so the wall visibly ascends.
         * Perfect as a dramatic reveal right at the start of a phase.
         */
        fun edgePillarRise(
            players: List<Player>,
            edges: List<org.bukkit.Location>,
            particle: Particle = Particle.END_ROD,
            riseHeight: Int = 40,
            count: Int = 3,
        ) {
            if (edges.isEmpty()) return
            for (h in 0 until riseHeight) {
                Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                    edges.forEach { base ->
                        val loc = base.clone().add(0.0, h.toDouble(), 0.0)
                        players.forEach { p -> p.spawnParticle(particle, loc, count, 0.1, 0.05, 0.1, 0.0) }
                    }
                }, h.toLong())
            }
        }

        /**
         * Launches successive shockwave rings that start at the island edge
         * and travel inward toward [center], using lerp between each edge
         * point and the center.
         *
         * Each [waves] ring starts one [waveDelay] ticks after the previous.
         * Within a ring, each tick moves [steps] closer to center.
         */
        fun edgeShockwaveInward(
            players: List<Player>,
            edges: List<org.bukkit.Location>,
            center: org.bukkit.Location,
            particles: Pair<Particle, Particle> = Pair(Particle.ELECTRIC_SPARK, Particle.GLOW),
            waves: Int = 3,
            waveDelay: Long = 18L,
            steps: Int = 20,
        ) {
            if (edges.isEmpty()) return
            for (wave in 0 until waves) {
                val startTick = wave * waveDelay
                for (step in 0..steps) {
                    val t = step.toDouble() / steps          // 0.0 (edge) → 1.0 (center)
                    Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                        edges.forEach { edge ->
                            // Lerp only X/Z — Y stays at the island surface so the
                            // wave travels horizontally and never dives underground.
                            val x = edge.x + (center.x - edge.x) * t
                            val z = edge.z + (center.z - edge.z) * t
                            val p = if (step % 2 == 0) particles.first else particles.second
                            players.forEach { pl -> pl.spawnParticle(p, x, edge.y + 1.0, z, 1, 0.0, 0.2, 0.0, 0.0) }
                        }
                    }, startTick + step.toLong())
                }
            }
        }
        /**
         * Launches successive shockwave rings that start at the island edge
         * and travel **outward** away from [center].
         *
         * Each point lerps from the edge position to [overshoot] times its
         * distance from center, giving the impression of the island border
         * blasting outward.
         */
        fun edgeShockwaveOutward(
            players: List<Player>,
            edges: List<org.bukkit.Location>,
            center: org.bukkit.Location,
            particles: Pair<Particle, Particle> = Pair(Particle.END_ROD, Particle.GLOW),
            waves: Int = 3,
            waveDelay: Long = 18L,
            steps: Int = 20,
            overshoot: Double = 1.6,
        ) {
            if (edges.isEmpty()) return
            for (wave in 0 until waves) {
                val startTick = wave * waveDelay
                for (step in 0..steps) {
                    val t = step.toDouble() / steps
                    Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                        edges.forEach { edge ->
                            val dx = edge.x - center.x
                            val dz = edge.z - center.z
                            val x = edge.x + dx * t * (overshoot - 1.0)
                            val z = edge.z + dz * t * (overshoot - 1.0)
                            val p = if (step % 2 == 0) particles.first else particles.second
                            players.forEach { pl -> pl.spawnParticle(p, x, edge.y + 1.0, z, 1, 0.0, 0.2, 0.0, 0.0) }
                        }
                    }, startTick + step.toLong())
                }
            }
        }

        /**
         * Fires particles from each edge block horizontally outward at island
         * surface height, traveling a total of [travelBlocks] blocks away from
         * the edge. Each step is separated by one tick, producing a smooth
         * horizontal streak that looks like the border wall pushing outward.
         *
         * Unlike [edgeShockwaveOutward] which uses a fixed overshoot fraction,
         * this travels an absolute number of blocks so the effect scales the
         * same regardless of island size.
         */
        fun edgeFlyOut(
            players: List<Player>,
            edges: List<org.bukkit.Location>,
            center: org.bukkit.Location,
            particle: Particle = Particle.END_ROD,
            travelBlocks: Int = 30,
            waves: Int = 3,
            waveDelay: Long = 20L,
            count: Int = 2,
        ) {
            if (edges.isEmpty()) return
            // Pre-compute the outward unit direction per edge so we don't repeat it every tick
            data class EdgeDir(val loc: org.bukkit.Location, val ndx: Double, val ndz: Double)
            val dirs = edges.map { edge ->
                val dx = edge.x - center.x
                val dz = edge.z - center.z
                val len = kotlin.math.sqrt(dx * dx + dz * dz).coerceAtLeast(0.001)
                EdgeDir(edge, dx / len, dz / len)
            }
            for (wave in 0 until waves) {
                val startTick = wave * waveDelay
                for (step in 1..travelBlocks) {
                    Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                        dirs.forEach { (edge, ndx, ndz) ->
                            val x = edge.x + ndx * step
                            val z = edge.z + ndz * step
                            players.forEach { pl ->
                                pl.spawnParticle(particle, x, edge.y + 0.8, z, count, 0.0, 0.15, 0.0, 0.0)
                            }
                        }
                    }, startTick + step.toLong())
                }
            }
        }

        /**
         * Instantly spawns a dense horizontal burst of particles flying away from
         * each edge block outward — a sharp single-frame "snap" like an explosion
         * ring. [speed] controls particle velocity in the outward direction.
         */
        fun edgeBurstOutward(
            players: List<Player>,
            edges: List<org.bukkit.Location>,
            center: org.bukkit.Location,
            particle: Particle = Particle.ELECTRIC_SPARK,
            speed: Double = 0.4,
            count: Int = 6,
        ) {
            if (edges.isEmpty()) return
            edges.forEach { edge ->
                val dx = edge.x - center.x
                val dz = edge.z - center.z
                val len = kotlin.math.sqrt(dx * dx + dz * dz).coerceAtLeast(0.001)
                val vx = (dx / len) * speed
                val vz = (dz / len) * speed
                players.forEach { pl ->
                    pl.spawnParticle(particle, edge.x, edge.y + 1.0, edge.z, count, 0.1, 0.2, 0.1, 0.0)
                    // second burst half a block outward already
                    pl.spawnParticle(particle, edge.x + vx * 2, edge.y + 1.0, edge.z + vz * 2, count, 0.0, 0.15, 0.0, 0.0)
                }
            }
        }
    }
}

