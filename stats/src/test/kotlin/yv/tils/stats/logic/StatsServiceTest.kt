/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.logic

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import yv.tils.stats.configs.ConfigFile
import yv.tils.stats.configs.StatsConfigState
import yv.tils.stats.data.StatsExport
import yv.tils.stats.registry.StatsRegistry

class StatsServiceTest {
    @BeforeEach
    fun reset() {
        StatsRegistry.clear()
        ConfigFile.state = StatsConfigState()
    }

    @AfterEach
    fun cleanup() = reset()

    @Test
    fun `Prometheus preserves fractions and includes the required infinity bucket`() {
        StatsRegistry.registerGauge("load", "load")!!.set(1.25)
        StatsRegistry.registerHistogram("duration", "duration")!!.observe(0.125)
        val text = StatsService.exportAsPrometheusText()
        assertTrue(text.contains("load 1.25\n"))
        assertTrue(text.contains("duration_bucket{le=\"+Inf\"} 1\n"))
        assertTrue(text.contains("duration_count 1\n"))
        assertTrue(text.contains("duration_sum 0.125\n"))
    }

    @Test
    fun `untrusted help strings and lists cannot inject Prometheus samples`() {
        StatsRegistry.registerCounter("events", "help\\text\r\nforged 1")
        StatsRegistry.registerString("version", "")!!.set("Paper\nforged 2\rmore")
        StatsRegistry.registerList("modules", "")!!.set(listOf("one\nforged 3", "two"))
        val text = StatsService.exportAsPrometheusText()
        assertFalse(text.lines().any { it.startsWith("forged") || it == "more" })
        assertTrue(text.contains("help\\\\text\\r\\nforged 1"))
        assertTrue(text.contains("Paper\\nforged 2\\rmore"))
    }

    @Test
    fun `JSON round trips typed sorted snapshots without a Bukkit server`() {
        StatsRegistry.registerGauge("z_load", "")!!.set(0.75)
        StatsRegistry.registerString("server_version", "")!!.set("Paper test")
        StatsRegistry.registerCounter("a_events", "")!!.inc(3)
        val export = Json.decodeFromString<StatsExport>(StatsService.exportAsJson())
        assertEquals("Paper test", export.metadata.serverVersion)
        assertEquals(listOf("a_events", "server_version", "z_load"), export.stats.map { it.key })
        assertEquals(0.75, export.stats.last().value.doubleValue)
        assertEquals(3L, export.stats.first().value.longValue)
    }

    @Test
    fun `exported lists are immutable snapshots of the registry`() {
        val modules = StatsRegistry.registerList("modules", "")!!
        modules.set(listOf("one"))
        val export = StatsService.buildExport()
        modules.add("two")
        assertEquals(listOf("one"), export.stats.single().value.listValue)
    }
}
