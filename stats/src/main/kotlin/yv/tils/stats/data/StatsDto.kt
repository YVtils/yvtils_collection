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

package yv.tils.stats.data

import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects for stats export.
 * These classes are designed to be JSON-serializable for export and consumption.
 */

/**
 * Represents metadata about the stats collection environment.
 *
 * @property serverName Optional human-readable name for the server
 * @property serverVersion The Paper/Bukkit server version
 * @property collectTimestamp Unix timestamp when stats were collected
 * @property region Whether region tracking is enabled (no actual IP stored by default for privacy)
 */
@Serializable
data class StatsMetadata(
    val serverName: String = "",
    val serverVersion: String = "",
    val collectTimestamp: Long = System.currentTimeMillis(),
    val region: Boolean = false
)

/**
 * Represents a single stat entry for export.
 *
 * @property key The unique identifier for the stat
 * @property type The stat type (COUNTER, GAUGE, STRING, etc.)
 * @property help Description of what this stat measures
 * @property value The current value (type depends on StatType)
 * @property lastUpdated Unix timestamp of last update
 */
@Serializable
data class StatEntry(
    val key: String,
    val type: String,
    val help: String,
    val value: StatValue,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * A polymorphic value holder for different stat types.
 * Only one field should be non-null based on the stat type.
 */
@Serializable
data class StatValue(
    val longValue: Long? = null,
    val doubleValue: Double? = null,
    val stringValue: String? = null,
    val listValue: List<String>? = null,
    val histogramValue: HistogramData? = null
) {
    companion object {
        fun ofLong(value: Long) = StatValue(longValue = value)
        fun ofDouble(value: Double) = StatValue(doubleValue = value)
        fun ofString(value: String) = StatValue(stringValue = value)
        fun ofList(value: List<String>) = StatValue(listValue = value)
        fun ofHistogram(data: HistogramData) = StatValue(histogramValue = data)
    }
}

/**
 * Histogram data for distribution metrics.
 *
 * @property count Total number of observations
 * @property sum Sum of all observed values
 * @property buckets Bucket boundaries and their cumulative counts (optional)
 */
@Serializable
data class HistogramData(
    val count: Long = 0,
    val sum: Double = 0.0,
    val buckets: Map<String, Long> = emptyMap()
)

/**
 * Complete stats export payload.
 *
 * @property metadata Collection metadata
 * @property stats List of all stat entries
 */
@Serializable
data class StatsExport(
    val metadata: StatsMetadata,
    val stats: List<StatEntry>
)

/**
 * Persistence format for stats-store.json.
 * Simpler than export format, focused on restoring values on startup.
 *
 * @property version Schema version for migration support
 * @property lastSaved Unix timestamp of last save
 * @property stats Map of stat key to persisted data
 */
@Serializable
data class StatsPersistence(
    val version: Int = 1,
    val lastSaved: Long = System.currentTimeMillis(),
    val stats: Map<String, PersistedStat>
)

/**
 * Individual stat persistence data.
 *
 * @property type The stat type for validation on load
 * @property value The persisted value
 * @property help The help text (persisted for reference)
 */
@Serializable
data class PersistedStat(
    val type: String,
    val value: StatValue,
    val help: String
)
