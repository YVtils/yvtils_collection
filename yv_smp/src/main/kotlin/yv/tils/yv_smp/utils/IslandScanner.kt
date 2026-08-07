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
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * IslandScanner — detects the shoreline of a floating island by casting rays
 * outward from a center point and finding where land meets non-land.
 */
object IslandScanner {

    /** Block types that are NOT considered solid island surface. */
    private val nonLandMaterials = setOf(
        Material.AIR, Material.VOID_AIR, Material.CAVE_AIR,
        Material.WATER, Material.LAVA,
        Material.TALL_GRASS, Material.SHORT_GRASS,
        Material.FERN, Material.LARGE_FERN,
        Material.DEAD_BUSH, Material.SNOW, Material.VINE,
    )

    // ── Cache ─────────────────────────────────────────────────────────────────

    private val cache = mutableMapOf<String, List<Location>>()

    fun clearCache() = cache.clear()

    // ── Core scan ─────────────────────────────────────────────────────────────

    /**
     * Scans outward from [center] and returns one [Location] per detected
     * edge block (the last solid land block before the island drops into
     * water / air / void).
     *
     * The returned Y is one block above the surface so particles appear
     * visibly on top of the edge block.
     *
     * @param center      The island's center point — typically world spawn.
     * @param scanRadius  How many blocks outward each ray travels at most.
     * @param angleSteps  How many rays to cast (more = denser outline).
     * @param forceRescan Ignore cached result and scan fresh.
     */
    fun scan(
        center: Location,
        scanRadius: Int = 80,
        angleSteps: Int = 360,
        forceRescan: Boolean = false,
        verticalTolerance: Int = 20,
    ): List<Location> {

        val key = "${center.world?.name},${center.blockX},${center.blockZ}"
        if (!forceRescan) cache[key]?.let { return it }

        val world = center.world ?: return emptyList()
        val cx = center.blockX
        val cz = center.blockZ
        // Determine the island's surface Y from the center column so we can
        // reject any block that is far below the island (i.e. regular world ground).
        val centerSurfaceY = world.getHighestBlockYAt(cx, cz, HeightMap.WORLD_SURFACE)

        val edgeMap = LinkedHashMap<Long, Location>()

        for (step in 0 until angleSteps) {

            val angle = (2.0 * PI * step) / angleSteps
            val dirX  = cos(angle)
            val dirZ  = sin(angle)

            var edgeBx = Int.MIN_VALUE
            var edgeBz = Int.MIN_VALUE
            var hitLand = false

            for (dist in 1..scanRadius) {
                val bx = (cx + dist * dirX).roundToInt()
                val bz = (cz + dist * dirZ).roundToInt()

                if (isLand(world, bx, bz, centerSurfaceY, verticalTolerance)) {
                    hitLand = true
                    edgeBx = bx
                    edgeBz = bz
                }
            }

            if (hitLand) {
                recordEdge(world, edgeBx, edgeBz, edgeMap)
            }
        }

        val result = edgeMap.values.toList()
        Logger.info("IslandScanner: scanned ${angleSteps} rays → ${result.size} unique edge blocks " +
                "(center=${cx},${cz}  islandSurfaceY=${centerSurfaceY}  radius=${scanRadius})")
        cache[key] = result
        return result
    }

    // ── Particle effects ──────────────────────────────────────────────────────

    /**
     * Runs a scan and repeatedly emits glowing particles at every detected
     * edge (cliff-top) block. Two layers are drawn:
     *
     *   • [GLOW]    — a bright glow dot exactly on the cliff-top face.
     *   • [END_ROD] — a thin rod of light 0.5 blocks above the glow, giving
     *                 height to the outline so it's visible from further away.
     *
     * Particles are sent directly to each [players] client so they are visible
     * regardless of server-side particle render distance.
     */
    fun outlineGlowing(
        center: Location,
        players: List<Player>,
        particle: Particle = Particle.END_ROD,
        scanRadius: Int = 80,
        angleSteps: Int = 360,
        duration: Long = 200L,
        repeatInterval: Long = 2L,
        forceRescan: Boolean = false,
        verticalTolerance: Int = 20,
    ) {
        val edgePoints = scan(center, scanRadius, angleSteps, forceRescan, verticalTolerance)
        if (edgePoints.isEmpty()) {
            Logger.warn("IslandScanner.outlineGlowing: no edge points — center=${center.blockX},${center.blockZ}")
            return
        }

        // Pre-compute the two layers so we avoid allocating inside the hot loop.
        // Layer 0 — GLOW exactly on the cliff-top face (stored Y already = topY + 0.5)
        // Layer 1 — END_ROD / chosen particle 0.6 higher so it floats above the block
        val glowLayer = edgePoints
        val rodLayer  = edgePoints.map { it.clone().add(0.0, 0.6, 0.0) }

        val taskHolder = IntArray(1)
        taskHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, {
            glowLayer.forEach { loc ->
                players.forEach { p ->
                    p.spawnParticle(Particle.GLOW, loc, 1, 0.0, 0.0, 0.0, 0.0)
                }
            }
            rodLayer.forEach { loc ->
                players.forEach { p ->
                    p.spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, 0.0)
                }
            }
        }, 0L, repeatInterval)

        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            Bukkit.getScheduler().cancelTask(taskHolder[0])
        }, duration)
    }

    /**
     * Fires a rising wall of particles from each edge block upward — looks like
     * the island is charging up or igniting along its own shoreline.
     */
    fun edgePulse(
        center: Location,
        players: List<Player>,
        particle: Particle = Particle.ELECTRIC_SPARK,
        scanRadius: Int = 80,
        angleSteps: Int = 360,
        pulseHeight: Double = 20.0,
        forceRescan: Boolean = false,
        verticalTolerance: Int = 20,
    ) {
        val edgePoints = scan(center, scanRadius, angleSteps, forceRescan, verticalTolerance)
        if (edgePoints.isEmpty()) return

        for (h in 0 until pulseHeight.toInt()) {
            Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                edgePoints.forEach { base ->
                    val loc = base.clone().add(0.0, h.toDouble(), 0.0)
                    players.forEach { p -> p.spawnParticle(particle, loc, 2, 0.1, 0.0, 0.1, 0.0) }
                }
            }, h.toLong())
        }
    }

    // ── Debug ─────────────────────────────────────────────────────────────────

    /**
     * Visual debug scan for in-game diagnosis.
     *
     * Runs a fresh scan, then starts two long-lived repeating tasks (30 s):
     *   🟢 Green  DUST — blocks the scan considers solid land
     *   🔴 Red    DUST — first non-land block each ray hits (just outside the island)
     *   🟡 Yellow DUST — the stored edge block (last land before going off-island)
     *   ⚪ END_ROD     — the final outline (same source as [outlineGlowing])
     *
     * Also prints the first 5 edge /tp coordinates to chat for manual verification.
     */
    fun debugScan(
        center: Location,
        player: Player,
        scanRadius: Int = 80,
        angleSteps: Int = 360,
        verticalTolerance: Int = 20,
    ) {
        val world = center.world ?: return
        val cx = center.blockX
        val cz = center.blockZ
        val centerSurfaceY = world.getHighestBlockYAt(cx, cz, HeightMap.WORLD_SURFACE)

        // Fresh scan — overwrites cache
        val edgePoints = scan(center, scanRadius, angleSteps, forceRescan = true, verticalTolerance = verticalTolerance)
        edgePoints.map { packXZ(it.blockX, it.blockZ) }.toHashSet()

        Logger.info("=== IslandScanner debugScan ===")
        Logger.info("  Center: $cx, ${center.blockY}, $cz  world=${world.name}")
        Logger.info("  islandSurfaceY=$centerSurfaceY  verticalTolerance=$verticalTolerance")
        Logger.info("  Radius=$scanRadius  Rays=$angleSteps  Edges=${edgePoints.size}")
        edgePoints.take(5).forEach {
            Logger.info("    edge @ ${it.blockX}, ${it.blockY}, ${it.blockZ}")
        }

        val greenDust  = Particle.DustOptions(org.bukkit.Color.fromRGB(0,   220,  60), 1.0f)
        val redDust    = Particle.DustOptions(org.bukkit.Color.fromRGB(255,  30,  30), 1.0f)
        val yellowDust = Particle.DustOptions(org.bukkit.Color.fromRGB(255, 220,   0), 1.8f)

        data class Dot(val x: Double, val y: Double, val z: Double, val dust: Particle.DustOptions)
        val dots = mutableListOf<Dot>()

        for (step in 0 until angleSteps) {
            val angle = (2.0 * PI * step) / angleSteps
            val dirX  = cos(angle)
            val dirZ  = sin(angle)

            var edgeBx = Int.MIN_VALUE
            var edgeBz = Int.MIN_VALUE
            var hitLand = false

            data class Column(val bx: Int, val bz: Int, val topY: Double)
            val columns = mutableListOf<Column>()

            for (dist in 1..scanRadius) {
                val bx   = (cx + dist * dirX).roundToInt()
                val bz   = (cz + dist * dirZ).roundToInt()
                val topY = world.getHighestBlockYAt(bx, bz, HeightMap.WORLD_SURFACE).toDouble()

                if (isLand(world, bx, bz, centerSurfaceY, verticalTolerance)) {
                    hitLand = true
                    edgeBx = bx
                    edgeBz = bz
                    columns.add(Column(bx, bz, topY))
                }
            }

            if (!hitLand) continue

            // Paint all land columns: yellow if it is the outermost (= stored edge), green otherwise.
            columns.forEach { col ->
                val isEdge = col.bx == edgeBx && col.bz == edgeBz
                dots.add(Dot(col.bx + 0.5, col.topY + 1.3, col.bz + 0.5,
                    if (isEdge) yellowDust else greenDust))
            }

            // Red dot one block beyond the outermost land block in ray direction,
            // so it's clear where each ray stopped finding land.
            val nextBx   = (cx + (scanRadius + 1) * dirX).roundToInt().let { edgeBx + (dirX.roundToInt()) }
            val nextBz   = (cz + (scanRadius + 1) * dirZ).roundToInt().let { edgeBz + (dirZ.roundToInt()) }
            val nextTopY = world.getHighestBlockYAt(nextBx, nextBz, HeightMap.WORLD_SURFACE).toDouble()
            dots.add(Dot(nextBx + 0.5, nextTopY + 1.3, nextBz + 0.5, redDust))
        }

        // ── Repeating paint task (30 s) ───────────────────────────────────────
        val paintTask = IntArray(1)
        paintTask[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, {
            dots.forEach { d ->
                player.spawnParticle(Particle.DUST, d.x, d.y, d.z, 1, 0.0, 0.0, 0.0, 0.0, d.dust)
            }
        }, 0L, 3L)

        // ── Repeating END_ROD outline (30 s) ──────────────────────────────────
        val outlineTask = IntArray(1)
        outlineTask[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, {
            edgePoints.forEach { loc ->
                player.spawnParticle(Particle.END_ROD, loc, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }, 0L, 2L)

        // Cancel both after 30 s
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            Bukkit.getScheduler().cancelTask(paintTask[0])
            Bukkit.getScheduler().cancelTask(outlineTask[0])
            val mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
            player.sendMessage(mm.deserialize("<gray>[Scanner] Debug finished."))
        }, 600L)

        // ── Chat summary ──────────────────────────────────────────────────────
        val mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
        player.sendMessage(mm.deserialize(
            "<yellow>[Scanner] Rays: $angleSteps | Edges: <white>${edgePoints.size}</white> | Center: <white>$cx,$cz</white>"))
        player.sendMessage(mm.deserialize(
            "<aqua>[Scanner] Center surface block: <white>" +
            world.getBlockAt(cx, world.getHighestBlockYAt(cx, cz, HeightMap.WORLD_SURFACE), cz).type))

        if (edgePoints.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>[Scanner] No edges found — is the center on the island?"))
            return
        }

        player.sendMessage(mm.deserialize("<gold>First edge blocks (click to tp):"))
        edgePoints.take(5).forEach { loc ->
            player.sendMessage(mm.deserialize("  <white>/tp ${loc.blockX} ${loc.blockY} ${loc.blockZ}"))
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Returns true when the highest surface block at (bx, bz) is solid land
     * AND is within [verticalTolerance] blocks of [centerSurfaceY].
     *
     * The Y gate is the critical fix for floating-island worlds: without it,
     * rays that travel past the island and hit the regular ground level below
     * would be accepted as "land", causing the scanner to include far-away
     * ground blocks as island edges.
     */
    private fun isLand(
        world: World,
        bx: Int,
        bz: Int,
        centerSurfaceY: Int,
        verticalTolerance: Int,
    ): Boolean {
        val y     = world.getHighestBlockYAt(bx, bz, HeightMap.WORLD_SURFACE)
        // Reject anything that sits more than [verticalTolerance] blocks below
        // the island surface — that is ground level, not the island.
        if (y < centerSurfaceY - verticalTolerance) return false
        val block = world.getBlockAt(bx, y, bz)
        return block.type !in nonLandMaterials
    }

    /** Packs a block X/Z pair into a single Long for use as a map key. */
    private fun packXZ(bx: Int, bz: Int): Long =
        bx.toLong().shl(32).or(bz.toLong() and 0xFFFFFFFFL)

    /**
     * Records (bx, bz) as an edge block in [edgeMap] if not already present.
     * (bx, bz) is the LAST solid land block before the island drops away.
     * Y is placed 0.5 above its surface so particles sit visibly on the top
     * face of the cliff block.
     */
    private fun recordEdge(world: World, bx: Int, bz: Int, edgeMap: LinkedHashMap<Long, Location>) {
        val col = packXZ(bx, bz)
        if (!edgeMap.containsKey(col)) {
            val topY = world.getHighestBlockYAt(bx, bz, HeightMap.WORLD_SURFACE).toDouble()
            edgeMap[col] = Location(world, bx + 0.5, topY + 0.5, bz + 0.5)
        }
    }
}
