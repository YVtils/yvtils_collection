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

package yv.tils.stats.registry

import yv.tils.stats.data.HistogramData
import yv.tils.stats.data.StatType
import yv.tils.stats.data.StatValue
import yv.tils.stats.data.StatEntry
import yv.tils.utils.logger.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import yv.tils.stats.data.PersistedStat

/**
 * Thread-safe registry for stats metrics.
 *
 * ## Thread Safety
 * All public methods are thread-safe and can be called from any thread.
 * - Counters use atomic primitives; gauge updates synchronize their numeric representation
 * - String and list operations are synchronized per-stat
 * - Registration methods use ConcurrentHashMap for safe concurrent access
 *
 * ## Concurrency Expectations
 * - `increment()`, `set()` for counters: Lock-free, O(1)
 * - Gauge updates: Synchronized per stat, O(1)
 * - `set()` for strings: Synchronized on the stat entry, O(1)
 * - `appendToList()`: Synchronized on the stat entry, O(1) amortized
 * - `exportAsJson()`, `exportAsPrometheusText()`: Read-only snapshot, may briefly block writes
 *
 * ## Key Validation
 * Keys must match pattern `[a-zA-Z_:][a-zA-Z0-9_:]*`. Invalid keys are rejected with a warning.
 */
object StatsRegistry {
    // Regex for valid stat keys (Prometheus-compatible)
    private val KEY_PATTERN = Regex("^[a-zA-Z_:][a-zA-Z0-9_:]*$")

    // Maximum list size to prevent memory issues
    @Volatile private var maxListSize = 1000

    // Maximum number of stats to prevent high cardinality
    @Volatile private var maxStatsCount = 10000
    private val exportedNames = mutableMapOf<String, String>()

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
        @Volatile var doubleGauge: Boolean = false,
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
        private var count = 0L
        private var sum = 0.0

        @Synchronized
        fun observe(value: Double) {
            require(value.isFinite() && value >= 0) { "Histogram observations must be finite and non-negative" }
            val nextSum = sum + value
            require(nextSum.isFinite()) { "Histogram sum overflow" }
            count = Math.addExact(count, 1)
            sum = nextSum
        }

        @Synchronized
        fun toHistogramData(): HistogramData = HistogramData(
            count = count,
            sum = sum
        )

        @Synchronized
        fun restore(data: HistogramData) {
            require(data.count >= 0 && data.sum.isFinite() && data.sum >= 0) { "Invalid histogram data" }
            count = data.count
            sum = data.sum
        }
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
         * Set the gauge to a finite double value without losing fractional precision.
         */
        fun set(value: Double) = set(key, value)

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
        fun get(): Long = getValue(key)?.let { it.longValue ?: it.doubleValue?.toLong() } ?: 0

        fun getDouble(): Double = getValue(key)?.let { it.doubleValue ?: it.longValue?.toDouble() } ?: 0.0
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
        fun observe(value: Double) = observeValue(key, value)

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
    @Synchronized
    fun registerStat(key: String, type: StatType, initialValue: Any? = null, help: String = ""): Boolean {
        // Validate key
        if (!isValidKey(key)) {
            Logger.warn("[Stats] Invalid stat key '$key'. Keys must match [a-zA-Z_:][a-zA-Z0-9_:]*")
            return false
        }

        stats[key]?.let { existing ->
            if (existing.type != type) {
                Logger.warn("[Stats] Stat '$key' is already registered as ${existing.type}, not $type")
                return false
            }
            return true
        }

        if (stats.size >= maxStatsCount) {
            Logger.warn("[Stats] Maximum stats count ($maxStatsCount) reached. Cannot register '$key'")
            return false
        }
        val names = if (type == StatType.HISTOGRAM) {
            listOf(key, "${key}_bucket", "${key}_count", "${key}_sum")
        } else listOf(key)
        if (names.any { it in exportedNames }) {
            Logger.warn("[Stats] Stat '$key' conflicts with an existing Prometheus metric family")
            return false
        }

        val stat = RegisteredStat(key, type, help)

        // Set initial value if provided
        if (initialValue != null) {
            when (type) {
                StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP -> setNumeric(stat, initialValue)
                StatType.STRING -> {
                    stat.stringValue = initialValue.toString()
                }
                StatType.STRING_LIST -> {
                    stat.listValue.addAll(asStringList(initialValue).take(maxListSize))
                }
                StatType.HISTOGRAM -> {
                    // Histogram doesn't support initial value
                }
            }
        }

        stats[key] = stat
        names.forEach { exportedNames[it] = key }
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
            StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP -> setNumeric(stat, value)
            StatType.STRING -> {
                synchronized(stat.lock) {
                    stat.stringValue = value.toString()
                }
            }
            StatType.STRING_LIST -> {
                val list = asStringList(value)
                synchronized(stat.lock) {
                    stat.listValue.clear()
                    if (list.size > maxListSize) {
                        Logger.warn("[Stats] List for '$key' exceeds max size ($maxListSize). Truncating.")
                    }
                    stat.listValue.addAll(list.take(maxListSize))
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
                stat.atomicValue.updateAndGet { Math.addExact(it, delta) }
            }
            StatType.GAUGE -> {
                synchronized(stat.lock) {
                    if (stat.doubleGauge) {
                        val next = Double.fromBits(stat.atomicValue.get()) + delta
                        require(next.isFinite()) { "Gauge overflow" }
                        stat.atomicValue.set(next.toBits())
                    } else {
                        stat.atomicValue.updateAndGet { Math.addExact(it, delta) }
                    }
                }
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
    fun observeDuration(key: String, durationMillis: Long) = observeValue(key, durationMillis.toDouble())

    private fun observeValue(key: String, value: Double) {
        val stat = stats[key]
        if (stat == null) {
            Logger.warn("[Stats] Cannot observe duration for unregistered stat '$key'")
            return
        }

        if (stat.type != StatType.HISTOGRAM) {
            Logger.warn("[Stats] Cannot observe duration for ${stat.type} stat '$key'")
            return
        }

        stat.histogramData.observe(value)
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
            if (stat.listValue.size >= maxListSize) {
                Logger.warn("[Stats] List for '$key' at max size ($maxListSize). Ignoring append.")
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
        return valueOf(stat)
    }

    private fun valueOf(stat: RegisteredStat): StatValue {
        return synchronized(stat.lock) {
            when (stat.type) {
                StatType.GAUGE -> if (stat.doubleGauge) {
                    StatValue.ofDouble(Double.fromBits(stat.atomicValue.get()))
                } else StatValue.ofLong(stat.atomicValue.get())
                StatType.COUNTER, StatType.TIMESTAMP ->
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

    internal fun snapshot(): List<StatEntry> = stats.values.map { stat ->
        synchronized(stat.lock) {
            StatEntry(stat.key, stat.type.name, stat.help, valueOf(stat), stat.lastUpdated)
        }
    }.sortedBy { it.key }

    /**
     * Clear all stats on withdrawal/disable, and in tests.
     */
    @Synchronized
    internal fun clear() {
        stats.clear()
        exportedNames.clear()
    }

    @Synchronized
    internal fun unregister(key: String) {
        stats.remove(key)
        exportedNames.entries.removeIf { it.value == key }
    }

    @Synchronized
    internal fun configureLimits(listSize: Int, statsCount: Int) {
        maxListSize = listSize.coerceIn(1, 1000)
        maxStatsCount = statsCount.coerceIn(1, 10000)
        stats.values.forEach { stat ->
            synchronized(stat.lock) {
                if (stat.listValue.size > maxListSize) {
                    stat.listValue.subList(maxListSize, stat.listValue.size).clear()
                }
            }
        }
    }

    /**
     * Restore stats from persistence. Internal use only.
     */
    internal fun restore(persistedStats: Map<String, PersistedStat>) {
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
                    StatType.GAUGE -> {
                        (value.doubleValue ?: value.longValue)?.let { set(key, it) }
                    }
                    StatType.COUNTER, StatType.TIMESTAMP -> {
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
                            stats[key]?.histogramData?.restore(hist)
                        }
                    }
                }
            }
        }
    }

    // ========================
    // Utility Methods
    // ========================

    private fun asStringList(value: Any): List<String> {
        require(value is List<*> && value.all { it is String }) { "Expected a list of strings" }
        return value.map { it as String }
    }

    private fun setNumeric(stat: RegisteredStat, value: Any) {
        require(value is Number) { "Expected numeric value for ${stat.type}" }
        if (stat.type == StatType.GAUGE) {
            synchronized(stat.lock) {
                val floatingPoint = value is Double || value is Float
                require(!floatingPoint || value.toDouble().isFinite()) { "Gauge must be finite" }
                stat.atomicValue.set(if (floatingPoint) value.toDouble().toBits() else value.toLong())
                stat.doubleGauge = floatingPoint
            }
        } else {
            require(value is Long || value is Int || value is Short || value is Byte) { "Expected an integer for ${stat.type}" }
            val next = value.toLong()
            if (stat.type == StatType.COUNTER) {
                stat.atomicValue.updateAndGet { current ->
                    require(next >= current) { "Counters cannot decrease" }
                    next
                }
            } else stat.atomicValue.set(next)
        }
    }

    private fun isValidKey(key: String): Boolean {
        return key.isNotBlank() && KEY_PATTERN.matches(key)
    }
}
