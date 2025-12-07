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

package yv.tils.stats.registry

import yv.tils.stats.data.HistogramData
import yv.tils.stats.data.StatType
import yv.tils.stats.data.StatValue
import yv.tils.utils.logger.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe registry for stats metrics.
 *
 * ## Thread Safety
 * All public methods are thread-safe and can be called from any thread.
 * - Counter and gauge operations use atomic primitives for lock-free updates
 * - String and list operations are synchronized per-stat
 * - Registration methods use ConcurrentHashMap for safe concurrent access
 *
 * ## Concurrency Expectations
 * - `increment()`, `set()` for numeric types: Lock-free, O(1)
 * - `set()` for strings: Synchronized on the stat entry, O(1)
 * - `appendToList()`: Synchronized on the stat entry, O(1) amortized
 * - `exportAsJson()`, `exportAsPrometheusText()`: Read-only snapshot, may briefly block writes
 *
 * ## Key Validation
 * Keys must match pattern `[a-zA-Z0-9_:]+`. Invalid keys are rejected with a warning.
 */
object StatsRegistry {
    // Regex for valid stat keys (Prometheus-compatible)
    private val KEY_PATTERN = Regex("^[a-zA-Z_:][a-zA-Z0-9_:]*$")

    // Maximum list size to prevent memory issues
    private const val MAX_LIST_SIZE = 1000

    // Maximum number of stats to prevent high cardinality
    private const val MAX_STATS_COUNT = 10000

    // Internal stat storage
    private val stats = ConcurrentHashMap<String, RegisteredStat>()

    /**
     * Internal representation of a registered stat.
     */
    internal data class RegisteredStat(
        val key: String,
        val type: StatType,
        val help: String,
        val atomicValue: AtomicLong = AtomicLong(0),
        @Volatile var stringValue: String = "",
        val listValue: MutableList<String> = mutableListOf(),
        val histogramData: HistogramDataHolder = HistogramDataHolder(),
        @Volatile var lastUpdated: Long = System.currentTimeMillis()
    ) {
        // Lock object for synchronized access to non-atomic fields
        val lock = Any()
    }

    /**
     * Mutable holder for histogram data with thread-safe operations.
     */
    internal class HistogramDataHolder {
        private val count = AtomicLong(0)
        private val sum = AtomicLong(0) // Stored as long bits for atomicity

        fun observe(value: Double) {
            count.incrementAndGet()
            // Use compareAndSet loop for atomic double addition
            var current: Long
            var next: Long
            do {
                current = sum.get()
                val currentDouble = java.lang.Double.longBitsToDouble(current)
                next = java.lang.Double.doubleToLongBits(currentDouble + value)
            } while (!sum.compareAndSet(current, next))
        }

        fun getCount(): Long = count.get()
        fun getSum(): Double = java.lang.Double.longBitsToDouble(sum.get())

        fun toHistogramData(): HistogramData = HistogramData(
            count = getCount(),
            sum = getSum()
        )
    }

    // ========================
    // Handle Classes
    // ========================

    /**
     * Handle for counter metrics. Counters can only be incremented.
     */
    class CounterHandle internal constructor(private val key: String) {
        /**
         * Increment the counter by 1.
         */
        fun inc() = increment(key, 1)

        /**
         * Increment the counter by the specified delta.
         * @param delta The amount to add (must be non-negative)
         */
        fun inc(delta: Long) {
            require(delta >= 0) { "Counter delta must be non-negative" }
            increment(key, delta)
        }

        /**
         * Get the current value of the counter.
         */
        fun get(): Long = stats[key]?.atomicValue?.get() ?: 0
    }

    /**
     * Handle for gauge metrics. Gauges can increase or decrease.
     */
    class GaugeHandle internal constructor(private val key: String) {
        /**
         * Set the gauge to a specific value.
         */
        fun set(value: Long) = set(key, value)

        /**
         * Set the gauge to a specific double value (stored as long bits).
         */
        fun set(value: Double) = set(key, java.lang.Double.doubleToLongBits(value))

        /**
         * Increment the gauge by 1.
         */
        fun inc() = increment(key, 1)

        /**
         * Decrement the gauge by 1.
         */
        fun dec() = increment(key, -1)

        /**
         * Increment or decrement the gauge by the specified delta.
         */
        fun add(delta: Long) = increment(key, delta)

        /**
         * Get the current value of the gauge.
         */
        fun get(): Long = stats[key]?.atomicValue?.get() ?: 0
    }

    /**
     * Handle for string metrics.
     */
    class StringHandle internal constructor(private val key: String) {
        /**
         * Set the string value.
         */
        fun set(value: String) = set(key, value)

        /**
         * Get the current string value.
         */
        fun get(): String = stats[key]?.stringValue ?: ""
    }

    /**
     * Handle for list metrics.
     */
    class ListHandle internal constructor(private val key: String) {
        /**
         * Set the entire list.
         */
        fun set(values: List<String>) = set(key, values)

        /**
         * Append a value to the list.
         */
        fun add(value: String) = appendToList(key, value)

        /**
         * Get a copy of the current list.
         */
        fun get(): List<String> {
            val stat = stats[key] ?: return emptyList()
            synchronized(stat.lock) {
                return stat.listValue.toList()
            }
        }

        /**
         * Clear the list.
         */
        fun clear() = set(key, emptyList<String>())
    }

    /**
     * Handle for histogram metrics.
     */
    class HistogramHandle internal constructor(private val key: String) {
        /**
         * Observe a value (e.g., a duration in milliseconds).
         */
        fun observe(value: Double) = observeDuration(key, value.toLong())

        /**
         * Observe a duration in milliseconds.
         */
        fun observe(durationMillis: Long) = observeDuration(key, durationMillis)

        /**
         * Get the current histogram data.
         */
        fun get(): HistogramData = stats[key]?.histogramData?.toHistogramData() ?: HistogramData()
    }

    /**
     * Handle for timestamp metrics.
     */
    class TimestampHandle internal constructor(private val key: String) {
        /**
         * Set the timestamp to the current time.
         */
        fun setNow() = set(key, System.currentTimeMillis())

        /**
         * Set the timestamp to a specific value.
         */
        fun set(timestamp: Long) = set(key, timestamp)

        /**
         * Get the current timestamp value.
         */
        fun get(): Long = stats[key]?.atomicValue?.get() ?: 0
    }

    // ========================
    // Registration Methods
    // ========================

    /**
     * Register a counter metric.
     *
     * @param key Unique key for the metric (must match [a-zA-Z_:][a-zA-Z0-9_:]*)
     * @param help Description of what this metric measures
     * @return Handle for incrementing the counter, or null if registration failed
     */
    fun registerCounter(key: String, help: String): CounterHandle? {
        return if (registerStat(key, StatType.COUNTER, help = help)) {
            CounterHandle(key)
        } else null
    }

    /**
     * Register a gauge metric.
     *
     * @param key Unique key for the metric
     * @param help Description of what this metric measures
     * @return Handle for updating the gauge, or null if registration failed
     */
    fun registerGauge(key: String, help: String): GaugeHandle? {
        return if (registerStat(key, StatType.GAUGE, help = help)) {
            GaugeHandle(key)
        } else null
    }

    /**
     * Register a string metric.
     *
     * @param key Unique key for the metric
     * @param help Description of what this metric represents
     * @return Handle for updating the string, or null if registration failed
     */
    fun registerString(key: String, help: String): StringHandle? {
        return if (registerStat(key, StatType.STRING, help = help)) {
            StringHandle(key)
        } else null
    }

    /**
     * Register a list metric.
     *
     * @param key Unique key for the metric
     * @param help Description of what this metric represents
     * @return Handle for updating the list, or null if registration failed
     */
    fun registerList(key: String, help: String): ListHandle? {
        return if (registerStat(key, StatType.STRING_LIST, help = help)) {
            ListHandle(key)
        } else null
    }

    /**
     * Register a histogram metric.
     *
     * @param key Unique key for the metric
     * @param help Description of what this metric measures
     * @return Handle for observing values, or null if registration failed
     */
    fun registerHistogram(key: String, help: String): HistogramHandle? {
        return if (registerStat(key, StatType.HISTOGRAM, help = help)) {
            HistogramHandle(key)
        } else null
    }

    /**
     * Register a timestamp metric.
     *
     * @param key Unique key for the metric
     * @param help Description of what this metric represents
     * @return Handle for updating the timestamp, or null if registration failed
     */
    fun registerTimestamp(key: String, help: String): TimestampHandle? {
        return if (registerStat(key, StatType.TIMESTAMP, help = help)) {
            TimestampHandle(key)
        } else null
    }

    /**
     * Generic stat registration. Use typed methods when possible.
     *
     * @param key Unique key for the metric
     * @param type The stat type
     * @param initialValue Optional initial value (type must match StatType)
     * @param help Description of what this metric measures
     * @return true if registration succeeded, false otherwise
     */
    fun registerStat(key: String, type: StatType, initialValue: Any? = null, help: String = ""): Boolean {
        // Validate key
        if (!isValidKey(key)) {
            Logger.warn("[Stats] Invalid stat key '$key'. Keys must match [a-zA-Z_:][a-zA-Z0-9_:]*")
            return false
        }

        // Check cardinality limit
        if (stats.size >= MAX_STATS_COUNT) {
            Logger.warn("[Stats] Maximum stats count ($MAX_STATS_COUNT) reached. Cannot register '$key'")
            return false
        }

        // Check if already registered
        if (stats.containsKey(key)) {
            Logger.debug("[Stats] Stat '$key' is already registered")
            return true // Return true since the stat exists
        }

        val stat = RegisteredStat(key, type, help)

        // Set initial value if provided
        if (initialValue != null) {
            when (type) {
                StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP -> {
                    val longVal = when (initialValue) {
                        is Long -> initialValue
                        is Int -> initialValue.toLong()
                        is Number -> initialValue.toLong()
                        else -> 0L
                    }
                    stat.atomicValue.set(longVal)
                }
                StatType.STRING -> {
                    stat.stringValue = initialValue.toString()
                }
                StatType.STRING_LIST -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = initialValue as? List<String> ?: listOf(initialValue.toString())
                    stat.listValue.addAll(list.take(MAX_LIST_SIZE))
                }
                StatType.HISTOGRAM -> {
                    // Histogram doesn't support initial value
                }
            }
        }

        stats[key] = stat
        Logger.debug("[Stats] Registered stat: $key (type: $type)")
        return true
    }

    // ========================
    // Update Methods
    // ========================

    /**
     * Set a stat value. Type must match the registered stat type.
     *
     * @param key The stat key
     * @param value The new value
     * @throws IllegalArgumentException if value type doesn't match stat type
     */
    fun set(key: String, value: Any) {
        val stat = stats[key]
        if (stat == null) {
            Logger.warn("[Stats] Cannot set value for unregistered stat '$key'")
            return
        }

        when (stat.type) {
            StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP -> {
                val longVal = when (value) {
                    is Long -> value
                    is Int -> value.toLong()
                    is Number -> value.toLong()
                    else -> throw IllegalArgumentException("Expected numeric value for ${stat.type}")
                }
                stat.atomicValue.set(longVal)
            }
            StatType.STRING -> {
                synchronized(stat.lock) {
                    stat.stringValue = value.toString()
                }
            }
            StatType.STRING_LIST -> {
                synchronized(stat.lock) {
                    stat.listValue.clear()
                    @Suppress("UNCHECKED_CAST")
                    val list = value as? List<String> ?: listOf(value.toString())
                    if (list.size > MAX_LIST_SIZE) {
                        Logger.warn("[Stats] List for '$key' exceeds max size ($MAX_LIST_SIZE). Truncating.")
                    }
                    stat.listValue.addAll(list.take(MAX_LIST_SIZE))
                }
            }
            StatType.HISTOGRAM -> {
                Logger.warn("[Stats] Cannot set histogram value directly. Use observeDuration() instead.")
                return
            }
        }
        stat.lastUpdated = System.currentTimeMillis()
    }

    /**
     * Increment a counter or gauge by the specified delta.
     *
     * @param key The stat key
     * @param delta The amount to add (can be negative for gauges)
     */
    fun increment(key: String, delta: Long = 1) {
        val stat = stats[key]
        if (stat == null) {
            Logger.warn("[Stats] Cannot increment unregistered stat '$key'")
            return
        }

        when (stat.type) {
            StatType.COUNTER -> {
                if (delta < 0) {
                    Logger.warn("[Stats] Cannot decrement counter '$key'. Use a gauge instead.")
                    return
                }
                stat.atomicValue.addAndGet(delta)
            }
            StatType.GAUGE -> {
                stat.atomicValue.addAndGet(delta)
            }
            else -> {
                Logger.warn("[Stats] Cannot increment ${stat.type} stat '$key'")
                return
            }
        }
        stat.lastUpdated = System.currentTimeMillis()
    }

    /**
     * Observe a duration for a histogram stat.
     *
     * @param key The histogram stat key
     * @param durationMillis The observed duration in milliseconds
     */
    fun observeDuration(key: String, durationMillis: Long) {
        val stat = stats[key]
        if (stat == null) {
            Logger.warn("[Stats] Cannot observe duration for unregistered stat '$key'")
            return
        }

        if (stat.type != StatType.HISTOGRAM) {
            Logger.warn("[Stats] Cannot observe duration for ${stat.type} stat '$key'")
            return
        }

        stat.histogramData.observe(durationMillis.toDouble())
        stat.lastUpdated = System.currentTimeMillis()
    }

    /**
     * Append a value to a list stat.
     *
     * @param key The list stat key
     * @param value The value to append
     */
    fun appendToList(key: String, value: String) {
        val stat = stats[key]
        if (stat == null) {
            Logger.warn("[Stats] Cannot append to unregistered stat '$key'")
            return
        }

        if (stat.type != StatType.STRING_LIST) {
            Logger.warn("[Stats] Cannot append to ${stat.type} stat '$key'")
            return
        }

        synchronized(stat.lock) {
            if (stat.listValue.size >= MAX_LIST_SIZE) {
                Logger.warn("[Stats] List for '$key' at max size ($MAX_LIST_SIZE). Ignoring append.")
                return
            }
            stat.listValue.add(value)
        }
        stat.lastUpdated = System.currentTimeMillis()
    }

    // ========================
    // Query Methods
    // ========================

    /**
     * Check if a stat is registered.
     */
    fun isRegistered(key: String): Boolean = stats.containsKey(key)

    /**
     * Get the type of a registered stat.
     */
    fun getType(key: String): StatType? = stats[key]?.type

    /**
     * Get all registered stat keys.
     */
    fun getKeys(): Set<String> = stats.keys.toSet()

    /**
     * Get the current value of a stat as a StatValue.
     */
    fun getValue(key: String): StatValue? {
        val stat = stats[key] ?: return null
        return synchronized(stat.lock) {
            when (stat.type) {
                StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP ->
                    StatValue.ofLong(stat.atomicValue.get())
                StatType.STRING ->
                    StatValue.ofString(stat.stringValue)
                StatType.STRING_LIST ->
                    StatValue.ofList(stat.listValue.toList())
                StatType.HISTOGRAM ->
                    StatValue.ofHistogram(stat.histogramData.toHistogramData())
            }
        }
    }

    // ========================
    // Internal Access for Service
    // ========================

    /**
     * Get all registered stats. Internal use only.
     */
    internal fun getAllStats(): Map<String, RegisteredStat> = stats.toMap()

    /**
     * Clear all stats. Used for testing.
     */
    internal fun clear() {
        stats.clear()
    }

    /**
     * Restore stats from persistence. Internal use only.
     */
    internal fun restore(persistedStats: Map<String, yv.tils.stats.data.PersistedStat>) {
        for ((key, persisted) in persistedStats) {
            val type = try {
                StatType.valueOf(persisted.type)
            } catch (_: IllegalArgumentException) {
                Logger.warn("[Stats] Unknown stat type '${persisted.type}' for key '$key'. Skipping.")
                continue
            }

            if (registerStat(key, type, help = persisted.help)) {
                val value = persisted.value
                when (type) {
                    StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP -> {
                        value.longValue?.let { set(key, it) }
                    }
                    StatType.STRING -> {
                        value.stringValue?.let { set(key, it) }
                    }
                    StatType.STRING_LIST -> {
                        value.listValue?.let { set(key, it) }
                    }
                    StatType.HISTOGRAM -> {
                        // Histogram data is observation-based, we restore count and sum
                        value.histogramValue?.let { hist ->
                            val stat = stats[key]
                            if (stat != null) {
                                // Can't perfectly restore histogram, but we can approximate
                                // by observing the average value 'count' times
                                if (hist.count > 0) {
                                    val avg = hist.sum / hist.count
                                    repeat(hist.count.toInt().coerceAtMost(1000)) {
                                        stat.histogramData.observe(avg)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ========================
    // Utility Methods
    // ========================

    private fun isValidKey(key: String): Boolean {
        return key.isNotBlank() && KEY_PATTERN.matches(key)
    }
}
