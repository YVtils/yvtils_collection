/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.stats.data

/**
 * Defines the types of statistics that can be tracked.
 *
 * ## Stat Types
 * - **COUNTER**: A monotonically increasing value (e.g., total events, requests). Can only be incremented.
 * - **GAUGE**: A value that can go up or down (e.g., current player count, memory usage).
 * - **STRING**: A single string value (e.g., server version, active module name).
 * - **STRING_LIST**: A list of string values (e.g., loaded plugins, active modules).
 * - **TIMESTAMP**: A Unix timestamp in milliseconds (e.g., last webhook success time).
 * - **HISTOGRAM**: A distribution of values with observations (e.g., response times, chunk load times).
 *
 * ## Adding New Stat Types
 * To add a new stat type:
 * 1. Add a new enum value here with appropriate description
 * 2. Update [StatsRegistry] to handle the new type in registration and update methods
 * 3. Update [StatsService] to handle serialization/deserialization for the new type
 * 4. Update export methods (Prometheus/JSON) to format the new type appropriately
 */
enum class StatType {
    /**
     * A monotonically increasing counter.
     * Use for values that only go up (e.g., total requests, total errors).
     * Prometheus: Exported as counter type.
     */
    COUNTER,

    /**
     * A value that can increase or decrease.
     * Use for current state values (e.g., player count, queue size).
     * Prometheus: Exported as gauge type.
     */
    GAUGE,

    /**
     * A single string value.
     * Use for metadata (e.g., server version, module name).
     * Prometheus: Exported as info metric or JSON comment.
     */
    STRING,

    /**
     * A list of string values.
     * Use for collections of metadata (e.g., loaded plugins, active modules).
     * Prometheus: Exported as JSON comment block.
     */
    STRING_LIST,

    /**
     * A Unix timestamp in milliseconds.
     * Use for tracking when events occurred (e.g., last success, last failure).
     * Prometheus: Exported as gauge with Unix timestamp value.
     */
    TIMESTAMP,

    /**
     * A histogram for observing distributions.
     * Use for timing/duration measurements (e.g., response times, load times).
     * Tracks count, sum, and can compute percentiles.
     * Prometheus: Exported as histogram type with _count, _sum, and bucket suffixes.
     */
    HISTOGRAM;

    companion object {
        /**
         * Returns a list of numeric stat types that can be exported to Prometheus.
         */
        fun numericTypes(): List<StatType> = listOf(COUNTER, GAUGE, TIMESTAMP, HISTOGRAM)

        /**
         * Returns a list of string-based stat types.
         */
        fun stringTypes(): List<StatType> = listOf(STRING, STRING_LIST)
    }
}
