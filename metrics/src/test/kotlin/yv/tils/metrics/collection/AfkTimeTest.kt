/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.collection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AfkTimeTest {
    @Test fun `threshold grace period is excluded`() {
        assertEquals(0L, AfkTime.millis(5000, 299_000, 300_000))
        assertEquals(0L, AfkTime.millis(5000, 300_000, 300_000))
        assertEquals(2000L, AfkTime.millis(5000, 302_000, 300_000))
    }

    @Test fun `already idle player cannot import time from before the sample`() {
        assertEquals(5000L, AfkTime.millis(5000, 3_600_000, 300_000))
        assertEquals(0L, AfkTime.millis(0, 3_600_000, 300_000))
    }

    @Test fun `resumed activity resets the sampled idle contribution`() {
        assertEquals(5000L, AfkTime.millis(5000, 600_000, 300_000))
        assertEquals(0L, AfkTime.millis(5000, 1000, 300_000))
        assertEquals(0L, AfkTime.millis(5000, -1, 300_000))
    }
}
