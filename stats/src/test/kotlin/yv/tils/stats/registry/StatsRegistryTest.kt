/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.registry

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import yv.tils.stats.data.HistogramData
import yv.tils.stats.data.PersistedStat
import yv.tils.stats.data.StatType
import yv.tils.stats.data.StatValue
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class StatsRegistryTest {
    @BeforeEach
    fun reset() {
        StatsRegistry.clear()
        StatsRegistry.configureLimits(1000, 10000)
    }

    @AfterEach
    fun cleanup() = reset()

    @Test
    fun `double gauges retain fractional values and support arithmetic`() {
        val gauge = StatsRegistry.registerGauge("load", "load")!!
        gauge.set(1.25)
        gauge.add(2)
        gauge.dec()
        assertEquals(2.25, gauge.getDouble())
        assertEquals(2, gauge.get())
        assertEquals(StatValue.ofDouble(2.25), StatsRegistry.getValue("load"))
        gauge.set(4L)
        assertEquals(StatValue.ofLong(4), StatsRegistry.getValue("load"))
    }

    @Test
    fun `numeric values reject NaN infinity fractional counters and overflow`() {
        val gauge = StatsRegistry.registerGauge("load", "")!!
        assertThrows(IllegalArgumentException::class.java) { gauge.set(Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) { gauge.set(Double.POSITIVE_INFINITY) }
        val counter = StatsRegistry.registerCounter("events", "")!!
        assertThrows(IllegalArgumentException::class.java) { StatsRegistry.set("events", 1.5) }
        StatsRegistry.set("events", Long.MAX_VALUE)
        assertThrows(ArithmeticException::class.java) { counter.inc() }
        assertEquals(Long.MAX_VALUE, counter.get())
    }

    @Test
    fun `counters cannot decrease through handles or generic updates`() {
        val counter = StatsRegistry.registerCounter("events", "")!!
        counter.inc(3)
        StatsRegistry.increment("events", -1)
        assertThrows(IllegalArgumentException::class.java) { counter.inc(-1) }
        assertThrows(IllegalArgumentException::class.java) { StatsRegistry.set("events", 2L) }
        assertEquals(3L, counter.get())
    }

    @Test
    fun `same type registration is idempotent and other types are rejected`() {
        val counter = StatsRegistry.registerCounter("events", "original")!!
        counter.inc(7)
        assertNotNull(StatsRegistry.registerCounter("events", "new"))
        assertNull(StatsRegistry.registerGauge("events", "wrong type"))
        assertEquals(7, counter.get())
        assertEquals("original", StatsRegistry.snapshot().single().help)
    }

    @Test
    fun `configured limits bound registration and lists without blocking existing handles`() {
        StatsRegistry.configureLimits(2, 1)
        val list = StatsRegistry.registerList("modules", "")!!
        list.set(listOf("one", "two", "three"))
        list.add("four")
        assertEquals(listOf("one", "two"), list.get())
        assertNull(StatsRegistry.registerCounter("events", ""))
        assertNotNull(StatsRegistry.registerList("modules", ""))
        StatsRegistry.configureLimits(1, 1)
        assertEquals(listOf("one"), list.get())
    }

    @Test
    fun `list validation does not erase the previous value`() {
        val list = StatsRegistry.registerList("modules", "")!!
        list.set(listOf("one"))
        assertThrows(IllegalArgumentException::class.java) { StatsRegistry.set("modules", listOf(1, 2)) }
        assertEquals(listOf("one"), list.get())
    }

    @Test
    fun `histograms retain fractions and reject invalid observations`() {
        val histogram = StatsRegistry.registerHistogram("duration", "")!!
        histogram.observe(0.125)
        histogram.observe(2L)
        assertEquals(HistogramData(2, 2.125), histogram.get())
        assertThrows(IllegalArgumentException::class.java) { histogram.observe(-1L) }
        assertThrows(IllegalArgumentException::class.java) { histogram.observe(Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) { histogram.observe(Double.POSITIVE_INFINITY) }
        assertEquals(HistogramData(2, 2.125), histogram.get())
    }

    @Test
    fun `histogram restore retains counts above integer range exactly`() {
        val data = HistogramData(Int.MAX_VALUE.toLong() + 10, 123456.75)
        StatsRegistry.restore(mapOf("duration" to PersistedStat("HISTOGRAM", StatValue.ofHistogram(data), "")))
        assertEquals(data, StatsRegistry.getValue("duration")?.histogramValue)
    }

    @Test
    fun `histogram names cannot collide with generated series in either registration order`() {
        assertNotNull(StatsRegistry.registerHistogram("duration", ""))
        for (suffix in listOf("count", "sum", "bucket")) {
            assertNull(StatsRegistry.registerGauge("duration_$suffix", ""))
        }
        StatsRegistry.clear()
        StatsRegistry.registerCounter("duration_count", "")
        assertNull(StatsRegistry.registerHistogram("duration", ""))
        StatsRegistry.unregister("duration_count")
        assertNotNull(StatsRegistry.registerHistogram("duration", ""))
    }

    @Test
    fun `invalid keys are rejected`() {
        for (key in listOf("", "1count", "with space", "line\nbreak", "dashed-key")) {
            assertNull(StatsRegistry.registerCounter(key, ""))
        }
    }

    @Test
    fun `concurrent registrations never replace accumulated values`() {
        Executors.newFixedThreadPool(8).use { pool ->
            pool.invokeAll((1..8).map {
                Callable {
                    repeat(500) { StatsRegistry.registerCounter("events", "")!!.inc() }
                }
            }).forEach { it.get() }
        }
        assertEquals(4000L, StatsRegistry.getValue("events")?.longValue)
    }

    @Test
    fun `concurrent registrations respect the configured cardinality limit`() {
        StatsRegistry.configureLimits(1000, 10)
        Executors.newFixedThreadPool(8).use { pool ->
            pool.invokeAll((1..100).map { id -> Callable { StatsRegistry.registerCounter("events_$id", "") } })
                .forEach { it.get() }
        }
        assertEquals(10, StatsRegistry.getKeys().size)
    }

    @Test
    fun `concurrent histogram snapshots keep count and sum consistent`() {
        val histogram = StatsRegistry.registerHistogram("duration", "")!!
        Executors.newFixedThreadPool(4).use { pool ->
            val writers = (1..3).map { pool.submit { repeat(1000) { histogram.observe(1.0) } } }
            repeat(1000) {
                val data = histogram.get()
                assertEquals(data.count.toDouble(), data.sum)
            }
            writers.forEach { it.get() }
        }
        assertEquals(HistogramData(3000, 3000.0), histogram.get())
    }

    @Test
    fun `double gauges round trip through persistence values`() {
        StatsRegistry.restore(mapOf("load" to PersistedStat("GAUGE", StatValue.ofDouble(0.75), "")))
        assertEquals(StatValue.ofDouble(0.75), StatsRegistry.getValue("load"))
    }
}
