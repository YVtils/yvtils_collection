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
import yv.tils.utils.modules.Core
import yv.tils.yv_smp.utils.CinematicCamera.INTERPOLATION_INTERVAL
import kotlin.math.*

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
 * ### Research notes — is there a library for this?
 * There is no embeddable *library* for smooth Bukkit/Paper camera paths -
 * every tool that does this (Modrinth's "Cinematic Camera", "EtherCinematics",
 * "Zenith-Cinematics", "AnkiCamera", "NLibCutscene", ...) is a full standalone
 * plugin, not a Gradle/Maven dependency you can pull in. They all converge on
 * the exact same two techniques, which are implemented in-house below instead
 * of adding a dependency:
 *
 * 1. **The client only linearly interpolates** between two positions/rotations
 *    it's told about (confirmed by PaperMC's own docs on display entities) -
 *    there is no built-in easing or curvature on the client. Any "smoothness"
 *    beyond a straight line has to be produced *server-side* by sending many
 *    closely-spaced points that already lie on a curve.
 * 2. **Catmull-Rom splines** are the standard way every one of those plugins
 *    generates that curve from a handful of authored keyframes, because
 *    (unlike a Bezier curve) a Catmull-Rom spline passes exactly *through*
 *    each control point - see [MotionPath] and [path] below, which implement
 *    the same well-known formula.
 *
 * Our existing orbit/dolly/spiral/pendulum paths are already parametric
 * curves (circles, eased lerps) evaluated at a fine tick resolution, so they
 * were already "smooth" in that sense - the concrete bugs/gaps fixed here are:
 * - **Yaw wrap-around**: `atan2`-derived yaw jumps by -360 deg the instant an
 *   orbit angle crosses the +-180 deg seam. Since the client interpolates
 *   raw yaw values *linearly* (see above), that one frame would spin the
 *   camera the "long way round" almost instantly. Fixed via [unwrapYaw],
 *   using the same shortest-path-delta trick documented by every plugin above.
 * - **Missing easing**: [driveDollyIn] moved its radius/height/angle at a
 *   constant rate; a real dolly shot decelerates into its final framing.
 *   Now uses [Easing.smoothstep], same as the existing spiral/descent paths
 *   (which had the same formula hand-inlined twice - now shared).
 * - **No keyframe/spline path primitive**: [path] + [MotionPath] add a
 *   generic, reusable Catmull-Rom driver for future authored shots that
 *   aren't a simple circle/line, exactly like the dedicated plugins above.
 *
 * Call [stopAll] to release every player and clean up all entities / tasks.
 */
object CinematicCamera {

    /**
     * How often (in ticks) we send a new target position to the display entity.
     * The interpolation duration is set to this same value so each segment
     * blends directly into the next with no gap or snap.
     * Lower = more CPU but marginally smoother curves; 1 tick (updating every
     * tick, same cadence dedicated cinematic-camera plugins use) is cheap
     * enough here since only a handful of players are ever spectating at once.
     */
    private const val INTERPOLATION_INTERVAL = 1

    private val cameraEntities = mutableMapOf<java.util.UUID, ItemDisplay>()
    private val activeTasks = mutableListOf<Int>()

    /**
     * Last (unwrapped, may exceed +-180) yaw sent to each player's camera
     * display - see [unwrapYaw].
     */
    private val lastYaw = mutableMapOf<java.util.UUID, Float>()

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
        val surfaceY = center.world?.getHighestBlockYAt(
            center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE
        )?.toDouble() ?: center.y
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
        val surfaceY = center.world?.getHighestBlockYAt(
            center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE
        )?.toDouble() ?: center.y
        val sc = center.clone().apply { y = surfaceY }
        players.forEachIndexed { index, player ->
            val startAngle = (2 * PI * index) / players.size
            mountCamera(player, orbitPosition(sc, orbitRadius, height, startAngle, steepPitch = true))
            driveOrbit(
                player,
                sc,
                orbitRadius,
                height,
                startAngle,
                angularSpeedPerTick,
                durationTicks,
                steepPitch = true
            )
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
        val surfaceY = center.world?.getHighestBlockYAt(
            center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE
        )?.toDouble() ?: center.y
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
        val surfaceY = center.world?.getHighestBlockYAt(
            center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE
        )?.toDouble() ?: center.y
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
        val surfaceY = center.world?.getHighestBlockYAt(
            center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE
        )?.toDouble() ?: center.y
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
        val surfaceY = center.world?.getHighestBlockYAt(
            center.blockX, center.blockZ,
            org.bukkit.HeightMap.WORLD_SURFACE
        )?.toDouble() ?: center.y
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
                y = center.y + startHeight
                yaw = 0f
                pitch = 75f
            }
            mountCamera(player, startLoc)

            val taskIdHolder = IntArray(1)
            var elapsed = 0L

            taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.instance, Runnable {
                elapsed += INTERPOLATION_INTERVAL

                val t = (elapsed.toDouble() / durationTicks).coerceAtMost(1.0)
                val eased = Easing.smoothstep(t)
                val targetY = startLoc.y - (startHeight - 2.0) * eased

                val display = cameraEntities[player.uniqueId] ?: run {
                    Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                    activeTasks.remove(taskIdHolder[0])
                    return@Runnable
                }

                smoothMove(player, display, Location(center.world, center.x, targetY, center.z, 0f, 75f))

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

    /**
     * Drives the camera through an arbitrary, hand-authored list of
     * [keyframes] using a **Catmull-Rom spline** ([MotionPath]) - the same
     * technique used by every dedicated cinematic-camera plugin out there
     * (see the class-level KDoc's research notes). Unlike the fixed
     * orbit/dolly/spiral/pendulum shapes above, this lets a phase describe an
     * arbitrary flight path as a handful of `Location`s (position + yaw/pitch)
     * and get a smooth, curved camera move through all of them - the curve
     * passes exactly through each keyframe rather than merely being pulled
     * toward it (as a Bezier curve would).
     *
     * @param keyframes Ordered path control points, at least 2. The camera
     *                  starts exactly on the first and ends exactly on the last.
     * @param durationTicks Total ticks to traverse the whole path.
     * @param easing Applied to overall path progress (0..1) before sampling
     *               the spline - defaults to [Easing.smoothstep] so the shot
     *               eases in and out rather than moving at a constant rate.
     */
    fun path(
        players: List<Player>,
        keyframes: List<Location>,
        durationTicks: Long = 200L,
        easing: (Double) -> Double = Easing::smoothstep,
    ) {
        require(keyframes.size >= 2) { "CinematicCamera.path() needs at least 2 keyframes" }

        players.forEach { player ->
            mountCamera(player, keyframes.first())
            drivePath(player, keyframes, durationTicks, easing)
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
        lastYaw.clear()
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Smoothstep/smootherstep easing curves for path progress (0..1). The
     * client only linearly interpolates between the points we send it (see
     * the class-level KDoc), so any acceleration/deceleration ("ease in/out")
     * has to be baked in server-side by warping the progress value fed into
     * a parametric path *before* evaluating it - this is what every one of
     * these functions is for.
     */
    private object Easing {
        /** Zero velocity at both ends - gentle, cheap, good default. */
        fun smoothstep(t: Double): Double {
            val c = t.coerceIn(0.0, 1.0)
            return c * c * (3.0 - 2.0 * c)
        }

        /** Zero velocity *and* zero acceleration at both ends - even gentler. */
        fun smootherstep(t: Double): Double {
            val c = t.coerceIn(0.0, 1.0)
            return c * c * c * (c * (c * 6.0 - 15.0) + 10.0)
        }
    }

    /**
     * Spawns an invisible [ItemDisplay] at [startLoc], configures client-side
     * interpolation, and makes [player] spectate it.
     */
    private fun mountCamera(player: Player, startLoc: Location) {
        dismountCamera(player)

        val display = startLoc.world!!.spawnEntity(startLoc, EntityType.ITEM_DISPLAY) as ItemDisplay
        display.isInvisible = true
        display.isInvulnerable = true
        display.isSilent = true
        display.isPersistent = false
        // Interpolation duration matches the update interval so each segment
        // blends seamlessly into the next with no gap or snap.
        display.interpolationDuration = INTERPOLATION_INTERVAL
        display.teleportDuration = INTERPOLATION_INTERVAL

        player.spectatorTarget = display
        cameraEntities[player.uniqueId] = display
        // Seed the yaw-unwrap state to this camera's actual starting yaw so the
        // very first `smoothMove` call has a sane baseline to unwrap against.
        lastYaw[player.uniqueId] = startLoc.yaw
    }

    private fun dismountCamera(player: Player) {
        lastYaw.remove(player.uniqueId)
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
     *
     * [target]'s yaw is passed through [unwrapYaw] first: since the client
     * interpolates yaw *linearly* (no shortest-path awareness), a raw target
     * yaw that has wrapped around the +-180 deg seam (e.g. 179 deg -> -179 deg,
     * mathematically a 2 deg turn) would otherwise make the client spin
     * almost a full 360 deg the "long way round" in a single interpolation
     * window. This is the single biggest visible smoothness bug any of these
     * orbiting paths can hit, and it only shows up once per revolution -
     * easy to miss when eyeballing a short test but jarring in a real session.
     */
    private fun smoothMove(player: Player, display: ItemDisplay, target: Location) {
        target.yaw = unwrapYaw(player.uniqueId, target.yaw)
        display.interpolationDelay = 0
        display.teleport(target)
    }

    /**
     * Returns the yaw closest to this player's previous camera yaw that is
     * still equivalent (mod 360 deg) to [targetYaw] - i.e. the shortest-path
     * unwrap described in [smoothMove]. The result can (and, over a long
     * multi-revolution orbit, will) drift outside the usual -180..180 range;
     * that's fine, Minecraft normalizes yaw for rendering regardless of the
     * raw float value.
     */
    private fun unwrapYaw(uuid: java.util.UUID, targetYaw: Float): Float {
        val prev = lastYaw[uuid] ?: run {
            lastYaw[uuid] = targetYaw
            return targetYaw
        }

        var delta = (targetYaw - prev) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        val result = prev + delta
        lastYaw[uuid] = result
        return result
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
        var angle = startAngle
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }

            angle += speed * INTERPOLATION_INTERVAL
            val target = orbitPosition(center, radius, height, angle, steepPitch)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(player, display, target)
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

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }

            // Eased (not linear) progress: a real dolly-in shot decelerates as
            // it settles into its final framing instead of stopping abruptly.
            val progress = Easing.smoothstep(elapsed.toDouble() / durationTicks)
            val radius = startRadius - (startRadius - endRadius) * progress
            val angle = startAngle + progress * PI * 0.5
            val curHeight = height - progress * 8.0
            val target = orbitPosition(center, radius, curHeight, angle)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(player, display, target)
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
        var angle = startAngle
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }
            val t = elapsed.toDouble() / durationTicks
            val eased = Easing.smoothstep(t)
            val height = startHeight + (endHeight - startHeight) * eased
            angle += speed * INTERPOLATION_INTERVAL
            // Use steep pitch when high, flatten out as we approach end height
            val steep = endHeight < startHeight   // descending = steep at start
            val target = orbitPosition(center, radius, height, angle, steepPitch = steep && t < 0.5)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(player, display, target)
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

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }
            val swing = sin(2.0 * PI * elapsed.toDouble() / period) * halfAngle
            val angle = baseAngle + swing
            val target = orbitPosition(center, radius, height, angle)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(player, display, target)
        }, 0L, INTERPOLATION_INTERVAL.toLong())

        activeTasks.add(taskIdHolder[0])
    }

    /** Drives a single player's camera along [keyframes] via [MotionPath] - see [path]. */
    private fun drivePath(
        player: Player,
        keyframes: List<Location>,
        durationTicks: Long,
        easing: (Double) -> Double,
    ) {
        val taskIdHolder = IntArray(1)
        var elapsed = 0L

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.instance, Runnable {
            elapsed += INTERPOLATION_INTERVAL
            if (elapsed > durationTicks) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0])
                activeTasks.remove(taskIdHolder[0])
                return@Runnable
            }

            val progress = easing((elapsed.toDouble() / durationTicks).coerceIn(0.0, 1.0))
            val target = MotionPath.sample(keyframes, progress)
            val display = cameraEntities[player.uniqueId] ?: return@Runnable
            smoothMove(player, display, target)
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

        val dx = center.x - x
        val dz = center.z - z
        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
        val dist = sqrt(dx * dx + dz * dz)

        // Always look at the island surface (center.y) rather than center.y+height/2.
        // This gives correct downward pitch at every height — no more looking upward.
        val pitchTarget = if (steepPitch) center.y - 4.0 else center.y + 2.0
        val dy = pitchTarget - y
        val pitch = Math.toDegrees(atan2(-dy, dist)).toFloat().coerceIn(-85f, 5f)

        return Location(center.world, x, y, z, yaw, pitch)
    }
}

/**
 * Catmull-Rom spline sampling over an ordered list of [Location] keyframes -
 * position (x/y/z) *and* rotation (yaw/pitch) are all splined the same way,
 * giving a smoothly-curved flight path that passes exactly through every
 * keyframe. This is the exact technique documented by every dedicated
 * Bukkit/Paper cinematic-camera plugin (see [CinematicCamera]'s class KDoc);
 * there is no library to depend on for it, so it's a small, self-contained
 * implementation here instead.
 *
 * Endpoints are "clamped" (duplicated) so the sampled path starts and ends
 * precisely on the first/last keyframe rather than over/undershooting past
 * them, which is the standard fix for Catmull-Rom's usual "needs a point
 * before the start and after the end" requirement.
 */
private object MotionPath {

    /**
     * Samples the spline through [keyframes] at overall progress [t] (0..1),
     * returning the interpolated camera [Location].
     */
    fun sample(keyframes: List<Location>, t: Double): Location {
        val world = keyframes.first().world
        val n = keyframes.size
        val clampedT = t.coerceIn(0.0, 1.0)

        // Which segment [i, i+1] we're in, and how far across it (0..1).
        val scaled = clampedT * (n - 1)
        val i = scaled.toInt().coerceIn(0, n - 2)
        val localT = scaled - i

        fun at(index: Int) = keyframes[index.coerceIn(0, n - 1)]

        val p0 = at(i - 1)
        val p1 = at(i)
        val p2 = at(i + 1)
        val p3 = at(i + 2)

        val x = catmullRom(p0.x, p1.x, p2.x, p3.x, localT)
        val y = catmullRom(p0.y, p1.y, p2.y, p3.y, localT)
        val z = catmullRom(p0.z, p1.z, p2.z, p3.z, localT)

        // Yaw is unwrapped across the *whole* keyframe list first (shortest
        // path between each consecutive pair) before splining - otherwise the
        // spline would happily interpolate straight through a raw +-180 deg
        // seam between two keyframes, same underlying issue as `unwrapYaw`
        // in `CinematicCamera`.
        val yaws = unwrapSequence(keyframes.map { it.yaw })
        val yaw = catmullRom(
            yaws[(i - 1).coerceIn(0, n - 1)],
            yaws[i],
            yaws[(i + 1).coerceIn(0, n - 1)],
            yaws[(i + 2).coerceIn(0, n - 1)],
            localT,
        )
        val pitch = catmullRom(p0.pitch, p1.pitch, p2.pitch, p3.pitch, localT)

        return Location(world, x, y, z, yaw, pitch)
    }

    private fun catmullRom(p0: Double, p1: Double, p2: Double, p3: Double, t: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5 * (
            2.0 * p1 +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
            )
    }

    private fun catmullRom(p0: Float, p1: Float, p2: Float, p3: Float, t: Double): Float =
        catmullRom(p0.toDouble(), p1.toDouble(), p2.toDouble(), p3.toDouble(), t).toFloat()

    /**
     * Rewrites a sequence of angles (degrees) so each one is the closest
     * equivalent (mod 360) to the previous one, i.e. no consecutive pair ever
     * differs by more than 180 deg. The result is a continuous (possibly
     * outside -180..180) sequence safe to feed straight into [catmullRom].
     */
    private fun unwrapSequence(angles: List<Float>): List<Float> {
        val result = ArrayList<Float>(angles.size)
        var prev = angles.first()
        result.add(prev)

        for (idx in 1 until angles.size) {
            var delta = (angles[idx] - prev) % 360f
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            prev += delta
            result.add(prev)
        }

        return result
    }
}