/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.ambient

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AmbientIntervalTest {
    @Test
    fun `converts ticks seconds minutes and hours without rounding`() {
        mapOf(
            "40" to 40L, "40t" to 40L, "1tick" to 1L, "40ticks" to 40L,
            "10s" to 200L, "1second" to 20L, "10seconds" to 200L,
            "5m" to 6000L, "1minute" to 1200L, "5minutes" to 6000L,
            "1h" to 72000L, "1hour" to 72000L, "2HOURS" to 144000L,
            "0.05s" to 1L, "1.5m" to 1800L, "0.5h" to 36000L,
        ).forEach { (input, expected) -> assertEquals(expected, AmbientInterval.parse(input), input) }
    }

    @Test
    fun `rejects invalid zero negative and sub-tick durations`() {
        listOf("", "0", "0s", "-1t", "0.01s", "0.5t", "NaN", "Infinity", "10d", "ten", "1s trailing")
            .forEach { assertNull(AmbientInterval.parse(it), it) }
    }

    @Test
    fun `rejects overflow instead of wrapping into a shorter interval`() {
        assertEquals(Long.MAX_VALUE, AmbientInterval.parse(Long.MAX_VALUE.toString()))
        assertNull(AmbientInterval.parse("9223372036854775808t"))
        assertNull(AmbientInterval.parse("9223372036854775807h"))
    }
}
