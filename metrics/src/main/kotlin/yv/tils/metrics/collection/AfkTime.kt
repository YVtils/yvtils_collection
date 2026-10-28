/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.collection

/** Conservative sampled estimate. Never count the threshold grace period or time before collection. */
object AfkTime {
    fun millis(elapsedMillis: Long, idleMillis: Long, thresholdMillis: Long): Long {
        require(elapsedMillis >= 0 && thresholdMillis > 0)
        return (idleMillis.coerceAtLeast(0) - thresholdMillis).coerceIn(0, elapsedMillis)
    }
}
