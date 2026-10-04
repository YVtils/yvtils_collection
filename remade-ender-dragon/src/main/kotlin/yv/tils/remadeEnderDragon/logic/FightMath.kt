/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.logic

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/** Pure rules shared by runtime and boundary tests. */
object FightMath {
    fun piercedReduction(reduction: Double, piercing: Double): Double =
        if (reduction < 0.0) reduction * (1.0 - piercing.coerceIn(0.0, 1.0)) else reduction

    /** Swept front prevents a fast ring skipping players between two-tick updates. */
    fun crossedWave(distance: Double, previous: Double, current: Double, radius: Double): Boolean =
        distance <= radius && distance >= previous - 0.6 && distance <= current + 0.6

    fun health(players: Int, cap: Int, base: Double, extra: Double, maximum: Double): Double =
        min(maximum, base + (players.coerceIn(1, cap) - 1) * extra)

    fun count(players: Int, cap: Int, base: Int, extra: Double, maximum: Int): Int =
        min(maximum, base + ceil((players.coerceIn(1, cap) - 1) * extra).toInt())

    fun landingDamage(raw: Double, cap: Double, health: Double): Double =
        max(0.0, min(min(raw, cap), health - 1.0))

    fun inCone(dx: Double, dz: Double, forwardX: Double, forwardZ: Double, radius: Double): Boolean {
        val distanceSquared = dx * dx + dz * dz
        if (distanceSquared > radius * radius) return false
        if (distanceSquared < 0.0001) return true
        val forwardLength = kotlin.math.sqrt(forwardX * forwardX + forwardZ * forwardZ)
        if (forwardLength < 0.0001) return false
        return (dx * forwardX + dz * forwardZ) / (kotlin.math.sqrt(distanceSquared) * forwardLength) >= kotlin.math.sqrt(0.5)
    }
}
