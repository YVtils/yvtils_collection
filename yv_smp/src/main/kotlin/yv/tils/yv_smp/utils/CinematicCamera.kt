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
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import yv.tils.utils.data.Data
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Smooth cinematic camera system for the start sequence.
 *
 * Each player spectates a dedicated invisible [ItemDisplay] entity.
 * [ItemDisplay] has native client-side interpolation: by setting
 * [ItemDisplay.setInterpolationDuration] to N ticks and then teleporting
 * the entity, the client smoothly blends from the old position to the new
 * one over N ticks — no snapping, no velocity hacks needed.
 *
 * We update the display position every [INTERPOLATION_INTERVAL] ticks and
 * set the interpolation duration to match, so each update hands off to the
 * next in a seamless chain.
 *
 * Call [stopAll] to release every player and clean up all entities / tasks.
 */
object CinematicCamera {

    /**
     * How often (in ticks) we send a new target position to the display entity.
     * The interpolation duration is set to this same value so each segment
     * blends directly into the next with no gap or snap.
     * Lower = more CPU but marginally smoother curves; 2 is a good balance.
     */
    private const val INTERPOLATION_INTERVAL = 2

    private val cameraEntities = mutableMapOf<java.util.UUID, ItemDisplay>()
    private val activeTasks    = mutableListOf<Int>()

    // ── Public API ────────────────────────────────────────────────────────────

    /** Slow low-altitude orbit — ground-level sweep looking inward. */
    fun orbitLow(
        players: List<Player>,
        center: Location,
        orbitRadius: Double = 40.0,
        height: Double = 12.0,
        durationTicks: Long = 140L,
        angularSpeedPerTick: Double = 0.012,
    ) {
        val surfaceY = center.world?.getHighestBlockYAt(center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE)?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val startAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, orbitRadius, height, startAngle))
            driveOrbit(player, sc, orbitRadius, height, startAngle, angularSpeedPerTick, durationTicks)
        }
    }

    /** High altitude downward orbit — dramatic wide sweep over the whole island. */
    fun orbitHigh(
        players: List<Player>,
        center: Location,
        orbitRadius: Double = 35.0,
        height: Double = 50.0,
        durationTicks: Long = 200L,
        angularSpeedPerTick: Double = 0.008,
    ) {
        val surfaceY = center.world?.getHighestBlockYAt(center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE)?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val startAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, orbitRadius, height, startAngle, steepPitch = true))
            driveOrbit(player, sc, orbitRadius, height, startAngle, angularSpeedPerTick, durationTicks, steepPitch = true)
        }
    }

    /** Cinematic dolly-in — starts far away and pushes toward the island. */
    fun dollyIn(
        players: List<Player>,
        center: Location,
        startRadius: Double = 60.0,
        endRadius: Double = 20.0,
        height: Double = 20.0,
        durationTicks: Long = 200L,
    ) {
        val surfaceY = center.world?.getHighestBlockYAt(center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE)?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val startAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, startRadius, height, startAngle))
            driveDollyIn(player, sc, startRadius, endRadius, height, startAngle, durationTicks)
        }
    }

    /**
     * Spiral descent — orbits the island while dropping from [startHeight] to
     * [endHeight] over [durationTicks]. Combines the drama of a high reveal
     * with a slow closing-in motion.
     */
    fun spiralDown(
        players: List<Player>,
        center: Location,
        orbitRadius: Double = 45.0,
        startHeight: Double = 60.0,
        endHeight: Double = 10.0,
        durationTicks: Long = 200L,
        angularSpeedPerTick: Double = 0.006,
    ) {
        // Heights are above the actual island surface, not the raw spawn Y
        val surfaceY = center.world?.getHighestBlockYAt(center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE)?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val startAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, orbitRadius, startHeight, startAngle))
            driveSpiral(player, sc, orbitRadius, startHeight, endHeight, startAngle, angularSpeedPerTick, durationTicks)
        }
    }

    /**
     * Rising orbit — starts at ground level and slowly ascends to [endHeight]
     * while orbiting. Creates a sense of growing anticipation / revelation.
     */
    fun orbitRising(
        players: List<Player>,
        center: Location,
        orbitRadius: Double = 36.0,
        startHeight: Double = 6.0,
        endHeight: Double = 45.0,
        durationTicks: Long = 200L,
        angularSpeedPerTick: Double = 0.006,
    ) {
        val surfaceY = center.world?.getHighestBlockYAt(center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE)?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val startAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, orbitRadius, startHeight, startAngle))
            driveSpiral(player, sc, orbitRadius, startHeight, endHeight, startAngle, angularSpeedPerTick, durationTicks)
        }
    }

    /**
     * Pendulum — sweeps the camera back and forth across the island on a
     * constant bearing (no full revolution). Creates an eerie oscillation
     * that suits dark or tense phases.
     *
     * @param swingHalfAngle Half-width of the swing arc in radians (default PI/3 = 60°).
     * @param swingPeriod    Ticks for one full back-and-forth cycle.
     */
    fun pendulum(
        players: List<Player>,
        center: Location,
        orbitRadius: Double = 38.0,
        height: Double = 18.0,
        durationTicks: Long = 200L,
        swingHalfAngle: Double = PI / 3.0,
        swingPeriod: Long = 80L,
    ) {
        val surfaceY = center.world?.getHighestBlockYAt(center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE)?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val baseAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, orbitRadius, height, baseAngle))
            drivePendulum(player, sc, orbitRadius, height, baseAngle, swingHalfAngle, swingPeriod, durationTicks)
        }
    }

    /**
     * Cinematic descent — starts high above [center] and smoothly descends
     * to [landingLoc] (defaults to [center] if omitted) over [durationTicks].
     * On arrival the display is removed and the player is teleported to the
     * landing location.
     *
     * @param startHeight   Blocks above [center.y] where the camera begins.
     * @param durationTicks Total ticks for the descent (20 = 1 s).
     * @param landingLoc    Where the player ends up after landing.
     * @param restoreGameMode If true, sets the player to SURVIVAL on landing.
     */
    fun descentToSpawn(
        players: List<Player>,
        center: Location,
        startHeight: Double = 120.0,
        durationTicks: Long = 160L,
        landingLoc: Location? = null,
        restoreGameMode: Boolean = true,
    ) {
        val landing = landingLoc ?: center.clone()

        players.forEach { player ->
            val startLoc = center.clone().apply {
                y     = center.y + startHeight
                yaw   = 0f
                pitch = 75f
            }
            mountCamera(player, startLoc)

            val taskIdHolder = IntArray(1)
            var elapsed = 0L

            taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, Runnable {
                elapsed += INTERPOLATION_INTERVAL

                val t       = (elapsed.toDouble() / durationTicks).coerceAtMost(1.0)
                val eased   = t * t * (3.0 - 2.0 * t)          // smoothstep
                val targetY = startLoc.y - (startHeight - 2.0) * eased

                val display = cameraEntities[player.uniqueId] ?: run {
                    Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                    activeTasks.remove(taskIdHolder[0])
                    return@Runnable
                }

                smoothMove(display, Location(center.world, center.x, targetY, center.z, 0f, 75f))

                if (elapsed >= durationTicks) {
                    Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                    activeTasks.remove(taskIdHolder[0])
                    dismountCamera(player)
                    if (restoreGameMode) player.gameMode = org.bukkit.GameMode.SURVIVAL
                    player.teleport(landing)
                }
            }, 0L, INTERPOLATION_INTERVAL.toLong())

            activeTasks.add(taskIdHolder[0])
        }
    }

    /** Releases all players, removes all display entities and cancels all tasks. */
    fun stopAll() {
        activeTasks.forEach { Bukkit.getScheduler().cancelTask(it) }
        activeTasks.clear()
        cameraEntities.forEach { (uuid, display) ->
            val player = Bukkit.getPlayer(uuid)
            if (player != null) {
                player.spectatorTarget = null
                player.teleport(display.world.spawnLocation)
            }
            display.remove()
        }
        cameraEntities.clear()
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Spawns an invisible [ItemDisplay] at [startLoc], configures client-side
     * interpolation, and makes [player] spectate it.
     */
    private fun mountCamera(player: Player, startLoc: Location) {
        dismountCamera(player)

        val display = startLoc.world!!.spawnEntity(startLoc, EntityType.ITEM_DISPLAY) as ItemDisplay
        display.isInvisible     = true
        display.isInvulnerable  = true
        display.isSilent        = true
        display.isPersistent    = false
        // Interpolation duration matches the update interval so each segment
        // blends seamlessly into the next with no gap or snap.
        display.interpolationDuration = INTERPOLATION_INTERVAL
        display.teleportDuration      = INTERPOLATION_INTERVAL

        player.spectatorTarget = display
        cameraEntities[player.uniqueId] = display
    }

    private fun dismountCamera(player: Player) {
        cameraEntities.remove(player.uniqueId)?.let { display ->
            player.spectatorTarget = null
            display.remove()
        }
    }

    /**
     * Sends a new target location to [display]. Setting [interpolationDelay]
     * to 0 tells the client to start interpolating immediately on this tick.
     * The client blends from the current rendered position to [target] over
     * [interpolationDuration] ticks — exactly matching our update interval.
     */
    private fun smoothMove(display: ItemDisplay, target: Location) {
        display.interpolationDelay = 0
        display.teleport(target)
    }

    private fun driveOrbit(
        player: Player,
        center: Location,
        radius: Double,
        height: Double,
        startAngle: Double,
        speed: Double,
        durationTicks: Long,
        steepPitch: Boolean = false,
    ) {
        val taskIdHolder = IntArray(1)
        var angle   = startAngle
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }

            angle += speed * INTERPOLATION_INTERVAL
            val target = orbitPosition(center, radius, height, angle, steepPitch)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(display, target)
        }, 0L, INTERPOLATION_INTERVAL.toLong())

        activeTasks.add(taskIdHolder[0])
    }

    private fun driveDollyIn(
        player: Player,
        center: Location,
        startRadius: Double,
        endRadius: Double,
        height: Double,
        startAngle: Double,
        durationTicks: Long,
    ) {
        val taskIdHolder = IntArray(1)
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }

            val progress  = elapsed.toDouble() / durationTicks
            val radius    = startRadius - (startRadius - endRadius) * progress
            val angle     = startAngle + progress * PI * 0.5
            val curHeight = height - progress * 8.0
            val target    = orbitPosition(center, radius, curHeight, angle)
            val display   = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(display, target)
        }, 0L, INTERPOLATION_INTERVAL.toLong())

        activeTasks.add(taskIdHolder[0])
    }

    /** Orbits while linearly interpolating height from [startHeight] to [endHeight]. */
    private fun driveSpiral(
        player: Player,
        center: Location,
        radius: Double,
        startHeight: Double,
        endHeight: Double,
        startAngle: Double,
        speed: Double,
        durationTicks: Long,
    ) {
        val taskIdHolder = IntArray(1)
        var angle   = startAngle
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }
            val t      = elapsed.toDouble() / durationTicks
            val eased  = t * t * (3.0 - 2.0 * t)   // smoothstep
            val height = startHeight + (endHeight - startHeight) * eased
            angle += speed * INTERPOLATION_INTERVAL
            // Use steep pitch when high, flatten out as we approach end height
            val steep = endHeight < startHeight   // descending = steep at start
            val target = orbitPosition(center, radius, height, angle, steepPitch = steep && t < 0.5)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(display, target)
        }, 0L, INTERPOLATION_INTERVAL.toLong())

        activeTasks.add(taskIdHolder[0])
    }

    /** Swings the camera back and forth across the island using a sine wave. */
    private fun drivePendulum(
        player: Player,
        center: Location,
        radius: Double,
        height: Double,
        baseAngle: Double,
        halfAngle: Double,
        period: Long,
        durationTicks: Long,
    ) {
        val taskIdHolder = IntArray(1)
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Data.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }
            val swing  = sin(2.0 * PI * elapsed.toDouble() / period) * halfAngle
            val angle  = baseAngle + swing
            val target = orbitPosition(center, radius, height, angle)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(display, target)
        }, 0L, INTERPOLATION_INTERVAL.toLong())

        activeTasks.add(taskIdHolder[0])
    }

    /** Computes a camera [Location] on the orbit circle, yaw and pitch facing the center surface. */
    private fun orbitPosition(
        center: Location,
        radius: Double,
        height: Double,
        angle: Double,
        steepPitch: Boolean = false,
    ): Location {
        val x = center.x + radius * cos(angle)
        val z = center.z + radius * sin(angle)
        val y = center.y + height

        val dx   = center.x - x
        val dz   = center.z - z
        val yaw  = Math.toDegrees(atan2(-dx, dz)).toFloat()
        val dist = sqrt(dx * dx + dz * dz)

        // Always look at the island surface (center.y) rather than center.y+height/2.
        // This gives correct downward pitch at every height — no more looking upward.
        val pitchTarget = if (steepPitch) center.y - 4.0 else center.y + 2.0
        val dy = pitchTarget - y
        val pitch = Math.toDegrees(atan2(-dy, dist)).toFloat().coerceIn(-85f, 5f)

        return Location(center.world, x, y, z, yaw, pitch)
    }
}