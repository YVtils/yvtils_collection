/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.api

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import yv.tils.stats.logic.StatsPusher
import yv.tils.stats.logic.StatsService
import yv.tils.stats.registry.StatsRegistry
import yv.tils.utils.stats.StatsApi
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ModuleStatsApiTest {
    private val consent = AtomicBoolean(false)
    private lateinit var backend: RegistryStatsBackend

    @BeforeEach
    fun setup() {
        StatsRegistry.clear()
        StatsRegistry.configureLimits(1000, 10000)
        backend = RegistryStatsBackend(consent::get)
    }

    @AfterEach
    fun cleanup() {
        StatsApi.uninstallBackend(backend)
        backend.close()
        StatsRegistry.clear()
        StatsRegistry.configureLimits(1000, 10000)
    }

    @Test
    fun `optional stats backend does not buffer updates before install or consent`() {
        val publisher = StatsApi.forModule("regions")
        assertFalse(publisher.isEnabled)
        assertFalse(publisher.incrementCounter("claims", "Claims", 5))
        StatsApi.installBackend(backend)
        assertFalse(publisher.incrementCounter("claims", "Claims", 5))
        assertTrue(StatsRegistry.getKeys().isEmpty())
        consent.set(true)
        assertTrue(publisher.isEnabled)
        assertTrue(publisher.incrementCounter("claims", "Claims", 1))
        assertEquals(1L, StatsRegistry.getValue("yvtils_module_regions__claims")?.longValue)
    }

    @Test
    fun `all stat types use module-scoped keys and enter standard exports and payloads`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        val publisher = StatsApi.forModule("regions")
        assertTrue(publisher.incrementCounter("claims", "Claims", 2))
        assertTrue(publisher.setGauge("load", "Load", 0.75))
        assertTrue(publisher.setGauge("players", "Players", 3L))
        assertTrue(publisher.setString("mode", "Mode", "survival"))
        assertTrue(publisher.setList("features", "Features", listOf("claims")))
        assertTrue(publisher.setTimestamp("updated_milliseconds", "Updated", 123L))
        assertTrue(publisher.observeHistogram("duration", "Duration", 0.125))
        assertEquals(7, StatsRegistry.getKeys().size)
        assertEquals(0.75, StatsRegistry.getValue("yvtils_module_regions__load")?.doubleValue)
        assertEquals(0.125, StatsRegistry.getValue("yvtils_module_regions__duration")?.histogramValue?.sum)
        assertTrue(StatsService.exportAsPrometheusText().contains("yvtils_module_regions__claims 2"))
        assertTrue(StatsPusher.buildPayload().stats.any { it.key == "yvtils_module_regions__claims" })
    }

    @Test
    fun `module separators cannot alias another module namespace`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        for (module in listOf("a-b", "a.b", "a_b", "a_b_")) {
            assertTrue(StatsApi.forModule(module).setGauge("value", "", 1L))
        }
        assertEquals(4, StatsRegistry.getKeys().size)
    }

    @Test
    fun `withdrawal discards updates and existing publishers resume only on reacceptance`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        val publisher = StatsApi.forModule("sit")
        publisher.incrementCounter("uses", "", 4)
        consent.set(false)
        backend.clearRegistry()
        assertFalse(publisher.isEnabled)
        assertFalse(publisher.incrementCounter("uses", "", 10))
        assertTrue(StatsRegistry.getKeys().isEmpty())
        consent.set(true)
        publisher.incrementCounter("uses", "", 1)
        assertEquals(1L, StatsRegistry.getValue("yvtils_module_sit__uses")?.longValue)
    }

    @Test
    fun `closed publishers cannot publish and remove only their own module`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        val one = StatsApi.forModule("sit")
        val two = StatsApi.forModule("regions")
        one.setGauge("count", "", 1L)
        two.setGauge("count", "", 2L)
        one.close()
        one.close()
        assertFalse(one.isEnabled)
        assertFalse(one.setGauge("count", "", 3L))
        assertEquals(setOf("yvtils_module_regions__count"), StatsRegistry.getKeys())
    }

    @Test
    fun `detach disables old publishers without requiring module dependencies or ordering`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        val publisher = StatsApi.forModule("sit")
        StatsApi.uninstallBackend(backend)
        backend.close()
        assertFalse(publisher.incrementCounter("uses", "", 1))
        val replacement = RegistryStatsBackend(consent::get)
        try {
            StatsApi.installBackend(replacement)
            StatsApi.uninstallBackend(backend)
            assertTrue(publisher.incrementCounter("uses", "", 1))
        } finally {
            StatsApi.uninstallBackend(replacement)
            replacement.close()
        }
    }

    @Test
    fun `conflicting metric types and configured limits return false`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        StatsRegistry.configureLimits(1, 1)
        val publisher = StatsApi.forModule("sit")
        assertTrue(publisher.setList("features", "", listOf("one", "two")))
        assertEquals(listOf("one"), StatsRegistry.getValue("yvtils_module_sit__features")?.listValue)
        assertFalse(publisher.incrementCounter("features", "", 1))
        assertFalse(publisher.incrementCounter("another", "", 1))
    }

    @Test
    fun `malformed names and values are rejected even without a backend`() {
        assertThrows(IllegalArgumentException::class.java) { StatsApi.forModule("../stats") }
        val publisher = StatsApi.forModule("sit")
        assertThrows(IllegalArgumentException::class.java) { publisher.setGauge("bad\nkey", "", 1L) }
        assertThrows(IllegalArgumentException::class.java) { publisher.incrementCounter("uses", "", -1) }
        assertThrows(IllegalArgumentException::class.java) { publisher.setGauge("load", "", Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) { publisher.observeHistogram("load", "", -1.0) }
    }

    @Test
    fun `concurrent module updates accumulate without replacing registrations`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        val publisher = StatsApi.forModule("regions")
        Executors.newFixedThreadPool(4).use { pool ->
            pool.invokeAll((1..4).map { Callable { repeat(250) { publisher.incrementCounter("claims", "", 1) } } })
                .forEach { it.get() }
        }
        assertEquals(1000L, StatsRegistry.getValue("yvtils_module_regions__claims")?.longValue)
    }
}
