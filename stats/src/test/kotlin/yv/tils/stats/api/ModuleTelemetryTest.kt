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
import yv.tils.utils.modules.Module
import yv.tils.utils.stats.ModuleStats
import yv.tils.utils.stats.ModuleTelemetry
import yv.tils.utils.stats.StatsApi
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ModuleTelemetryTest {
    private val consent = AtomicBoolean(false)
    private lateinit var backend: RegistryStatsBackend
    private val modules = listOf(
        "core", "common", "config", "utils", "gui", "stats", "metrics", "discord", "essentials", "fusion",
        "message", "moderation", "multiMine", "regions", "server", "sit", "status", "yv_smp", "vanish",
        "waypoint", "remade-ender-dragon",
    )

    @BeforeEach
    fun setup() {
        ModuleTelemetry.clear()
        StatsRegistry.clear()
        StatsRegistry.configureLimits(1000, 10000)
        backend = RegistryStatsBackend(consent::get)
    }

    @AfterEach
    fun cleanup() {
        ModuleTelemetry.clear()
        backend.close()
        StatsApi.uninstallBackend(backend)
        StatsRegistry.clear()
        StatsRegistry.configureLimits(1000, 10000)
    }

    private fun registerAll() = modules.forEach { ModuleTelemetry.register(it, "26.10.01-test") }

    @Test
    fun `all module metadata and usage remain absent before backend and consent`() {
        registerAll()
        ModuleTelemetry.Event.entries.forEach { assertFalse(ModuleTelemetry.record(it)) }
        StatsApi.installBackend(backend)
        ModuleTelemetry.publishModules()
        ModuleTelemetry.Event.entries.forEach { assertFalse(ModuleTelemetry.record(it)) }
        assertTrue(StatsService.buildExport().stats.isEmpty())
    }

    @Test
    fun `all loaded modules appear after late acceptance without requiring a restart`() {
        registerAll()
        StatsApi.installBackend(backend)
        consent.set(true)
        val entries = StatsService.buildExport().stats.associateBy { it.key }
        assertEquals(modules.size * 2, entries.size)
        modules.forEach { module ->
            val prefix = RegistryStatsBackend.prefix(module)
            assertEquals(1L, entries.getValue(prefix + "enabled").value.longValue)
            assertEquals("26.10.01-test", entries.getValue(prefix + "version").value.stringValue)
        }
    }

    @Test
    fun `every fixed usage event is exported and included in remote push payloads`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        registerAll()
        ModuleTelemetry.Event.entries.forEach { assertTrue(ModuleTelemetry.record(it), it.name) }
        val payload = StatsPusher.buildPayload().stats.associateBy { it.key }
        ModuleTelemetry.Event.entries.forEach { event ->
            val key = RegistryStatsBackend.prefix(event.module()) + event.key()
            assertEquals(1L, payload.getValue(key).value.longValue, event.name)
            assertEquals("COUNTER", payload.getValue(key).type)
        }
        assertTrue(StatsService.exportAsPrometheusText().contains("yvtils_module_sit__sessions_started_total 1"))
    }

    @Test
    fun `the metrics module exposes only presence and version never gameplay data`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        ModuleTelemetry.register("metrics", "26.10.01-test")
        assertEquals(
            setOf("yvtils_module_metrics__enabled", "yvtils_module_metrics__version"),
            StatsPusher.buildPayload().stats.map { it.key }.toSet(),
        )
        assertTrue(ModuleTelemetry.Event.entries.none { it.module() == "metrics" })
    }

    @Test
    fun `withdrawal clears values and reacceptance never replays discarded events`() {
        StatsApi.installBackend(backend)
        ModuleTelemetry.register("sit", "26.10.01-test")
        repeat(5) { ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED) }
        consent.set(true)
        ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED)
        assertEquals(1L, StatsRegistry.getValue("yvtils_module_sit__sessions_started_total")?.longValue)
        consent.set(false)
        backend.clearRegistry()
        repeat(5) { assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED)) }
        assertTrue(StatsService.buildExport().stats.isEmpty())
        consent.set(true)
        ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED)
        assertEquals(1L, StatsRegistry.getValue("yvtils_module_sit__sessions_started_total")?.longValue)
    }

    @Test
    fun `module registry owns publication lifecycle including cleanup`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        val module = Module.YVtilsModuleData("sit", "26.10.01-test")
        Module.addModule(module)
        try {
            assertTrue(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
            assertEquals(3, StatsRegistry.getKeys().size)
        } finally {
            Module.removeModule(module)
        }
        assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
        assertTrue(StatsRegistry.getKeys().isEmpty())
    }

    @Test
    fun `repeated metadata publication and version updates do not reset counters`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        ModuleTelemetry.register("fusion", "old")
        ModuleTelemetry.record(ModuleTelemetry.Event.FUSION_CRAFTS, 4L)
        ModuleTelemetry.register("fusion", "new")
        repeat(3) { ModuleTelemetry.publishModules() }
        assertEquals(4L, StatsRegistry.getValue("yvtils_module_fusion__crafts_total")?.longValue)
        assertEquals("new", StatsRegistry.getValue("yvtils_module_fusion__version")?.stringValue)
    }

    @Test
    fun `disabled modules reject events and reenabled modules start new counters`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        ModuleTelemetry.register("regions", "v1")
        ModuleTelemetry.register("sit", "v1")
        ModuleTelemetry.record(ModuleTelemetry.Event.REGIONS_CLAIMS_CREATED)
        ModuleTelemetry.unregister("regions")
        assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.REGIONS_CLAIMS_CREATED))
        assertTrue(StatsRegistry.getKeys().all { it.startsWith("yvtils_module_sit__") })
        ModuleTelemetry.register("regions", "v2")
        ModuleTelemetry.record(ModuleTelemetry.Event.REGIONS_CLAIMS_CREATED)
        assertEquals(1L, StatsRegistry.getValue("yvtils_module_regions__claims_created_total")?.longValue)
    }

    @Test
    fun `configured limits and invalid deltas do not disrupt feature code`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        StatsRegistry.configureLimits(1, 1)
        registerAll()
        assertEquals(1, StatsRegistry.getKeys().size)
        assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.FUSION_CRAFTS, -1))
        assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.FUSION_CRAFTS, 0))
        assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.FUSION_CRAFTS, 1))
    }

    @Test
    fun `existing module registrations follow a replacement stats backend`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        ModuleTelemetry.register("sit", "v1")
        ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED)
        backend.close()
        StatsApi.uninstallBackend(backend)
        StatsRegistry.clear()
        assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
        backend = RegistryStatsBackend(consent::get)
        StatsApi.installBackend(backend)
        ModuleTelemetry.publishModules()
        assertTrue(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
        assertEquals(1L, StatsRegistry.getValue("yvtils_module_sit__sessions_started_total")?.longValue)
        assertEquals("v1", StatsRegistry.getValue("yvtils_module_sit__version")?.stringValue)
    }

    @Test
    fun `an unavailable telemetry backend cannot break a feature action`() {
        val broken = object : StatsApi.Backend {
            override fun isCollecting(): Boolean = throw IllegalStateException("unavailable")
            override fun publish(module: String, key: String, help: String, type: ModuleStats.MetricType, value: Any) = false
            override fun removeModule(module: String) { throw IllegalStateException("unavailable") }
        }
        StatsApi.installBackend(broken)
        try {
            assertDoesNotThrow { ModuleTelemetry.register("sit", "v1") }
            assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
            assertDoesNotThrow { ModuleTelemetry.publishModules() }
            assertDoesNotThrow { ModuleTelemetry.unregister("sit") }
        } finally {
            StatsApi.uninstallBackend(broken)
        }
    }

    @Test
    fun `parallel fixed events accumulate exactly`() {
        StatsApi.installBackend(backend)
        consent.set(true)
        ModuleTelemetry.register("discord", "v1")
        Executors.newFixedThreadPool(4).use { pool ->
            pool.invokeAll((1..4).map { Callable {
                repeat(100) { ModuleTelemetry.record(ModuleTelemetry.Event.MINECRAFT_TO_DISCORD_MESSAGES) }
            } }).forEach { it.get() }
        }
        assertEquals(400L, StatsRegistry.getValue("yvtils_module_discord__minecraft_to_discord_messages_total")?.longValue)
    }
}
