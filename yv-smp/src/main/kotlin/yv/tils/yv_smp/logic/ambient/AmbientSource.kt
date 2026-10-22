/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.ambient

/** Only portable values are persisted: worlds/chunks need not be loaded at startup. */
data class AmbientSource(
    val name: String = "",
    val world: String = "",
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val sound: String = "minecraft:ambient.cave",
    val radius: Double = 16.0,
    val looping: Boolean = true,
    val intervalTicks: Long = 200,
    val particle: String? = null,
    /** Null preserves existing Minecraft sound sources; otherwise a server file path or HTTP(S) URL. */
    val audio: String? = null,
)

object AmbientInterval {
    /** Bare numbers are ticks. Fractional units are accepted only for whole-tick results. */
    fun parse(input: String): Long? {
        val match = Regex("([0-9]+(?:\\.[0-9]+)?)(t|ticks?|s|seconds?|m|minutes?|h|hours?)?", RegexOption.IGNORE_CASE)
            .matchEntire(input) ?: return null
        val factor = when (match.groupValues[2].lowercase().firstOrNull()) {
            's' -> 20L
            'm' -> 1200L
            'h' -> 72000L
            else -> 1L
        }
        return runCatching {
            match.groupValues[1].toBigDecimal().multiply(factor.toBigDecimal()).longValueExact()
        }.getOrNull()?.takeIf { it > 0 }
    }
}
