/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.configs

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import yv.tils.config.files.ConfigurateFileUtils
import java.nio.file.Files
import java.nio.file.Path

class MetricsConfigTest {
    @TempDir lateinit var directory: Path

    @AfterEach fun cleanup() { ConfigurateFileUtils.baseDirectory = null }

    @Test fun `fresh config uses private defaults and round trips through Configurate`() {
        ConfigurateFileUtils.baseDirectory = directory.toFile()
        val settings = MetricsConfig.load()
        assertEquals("127.0.0.1", settings.bindAddress)
        assertFalse(settings.publishLeaderboards)
        assertFalse(settings.importVanillaOnFirstJoin)
        assertEquals(settings, MetricsConfig.load())
    }

    @Test fun `existing opt in and partial settings survive defaults being added`() {
        ConfigurateFileUtils.baseDirectory = directory.toFile()
        val file = directory.resolve("metrics/config.yml")
        Files.createDirectories(file.parent)
        Files.writeString(file, "publishLeaderboards: true\nimportVanillaOnFirstJoin: true\nflushSeconds: 30\n")
        val settings = MetricsConfig.load()
        assertTrue(settings.publishLeaderboards)
        assertEquals(30L, settings.flushSeconds)
        assertEquals(9108, settings.port)
        assertTrue(settings.importVanillaOnFirstJoin)
        assertTrue(settings.collectBreakdowns)
        assertTrue(settings.trackAfk)
        assertEquals(300L, settings.afkThresholdSeconds)
        assertFalse(settings.publishFavorites) // Leaderboard opt-in must not silently expand identifying exports.
    }

    @Test fun `invalid and malformed configuration is not overwritten`() {
        ConfigurateFileUtils.baseDirectory = directory.toFile()
        val file = directory.resolve("metrics/config.yml")
        Files.createDirectories(file.parent)
        listOf("port: -1\n", "port: [unclosed\n").forEach { content ->
            Files.writeString(file, content)
            assertThrows(Exception::class.java) { MetricsConfig.load() }
            assertEquals(content, Files.readString(file))
        }
    }

    @Test fun `favorites cannot be published without collection and limits are validated`() {
        assertThrows(IllegalArgumentException::class.java) { MetricsConfig(publishFavorites = true, collectBreakdowns = false).validate() }
        assertThrows(IllegalArgumentException::class.java) { MetricsConfig(favoriteSize = 101).validate() }
        assertThrows(IllegalArgumentException::class.java) { MetricsConfig(afkThresholdSeconds = 0).validate() }
    }
}
