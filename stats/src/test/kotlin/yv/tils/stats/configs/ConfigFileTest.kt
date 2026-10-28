/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.configs

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import yv.tils.config.files.ConfigurateFileUtils
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class ConfigFileTest {
    @TempDir lateinit var directory: Path
    private val previousBase = ConfigurateFileUtils.baseDirectory

    @BeforeEach
    fun setup() {
        ConfigurateFileUtils.baseDirectory = directory.toFile()
        ConfigFile.state = StatsConfigState()
        ConfigFile.onChange = null
    }

    @AfterEach
    fun cleanup() {
        ConfigurateFileUtils.baseDirectory = previousBase
        ConfigFile.state = StatsConfigState()
        ConfigFile.onChange = null
        ConfigFile.config.clear()
    }

    @Test
    fun `fresh install does not assume consent`() {
        ConfigFile().loadConfig()
        assertTrue(ConfigFile.needsOptInPrompt())
        assertFalse(ConfigFile.isOptedIn())
        assertEquals("", ConfigFile.state.server_id)
    }

    @Test
    fun `opt in and opt out persist synchronously and survive reload`() {
        ConfigFile().loadConfig()
        ConfigFile.markOptIn(true)
        val id = ConfigFile.state.server_id
        assertEquals(4, UUID.fromString(id).version())
        ConfigFile.state = StatsConfigState()
        ConfigFile().loadConfig()
        assertTrue(ConfigFile.isOptedIn())
        assertEquals(id, ConfigFile.state.server_id)
        ConfigFile.markOptIn(false)
        ConfigFile.state = StatsConfigState()
        ConfigFile().loadConfig()
        assertFalse(ConfigFile.isOptedIn())
        assertFalse(ConfigFile.needsOptInPrompt())
        assertEquals(id, ConfigFile.state.server_id)
    }

    @Test
    fun `identifier does not change when configured name changes`() {
        ConfigFile.markOptIn(true)
        val id = ConfigFile.state.server_id
        ConfigFile().applyState(ConfigFile.state.copy(metadata = StatsConfigState.Metadata(server_name = "new server")))
        assertEquals(id, ConfigFile.state.server_id)
    }

    @Test
    fun `GUI changes notify runtime only after persistence and do not share mutable metadata`() {
        ConfigFile().loadConfig()
        var changes = 0
        ConfigFile.onChange = {
            changes++
            assertTrue(Files.readString(directory.resolve("stats/config.yml")).contains("opt_in: true"))
        }
        val edited = ConfigFile.state.copy(opt_in = true, metadata = StatsConfigState.Metadata(collect_player_count = false))
        ConfigFile().applyState(edited)
        assertEquals(1, changes)
        assertTrue(ConfigFile.isOptedIn())
        assertEquals(false, ConfigFile.getBoolean("metadata.collect_player_count"))
        edited.metadata.collect_player_count = true
        assertFalse(ConfigFile.state.metadata.collect_player_count)
    }

    @Test
    fun `unsafe limits are clamped to supported bounds`() {
        ConfigFile().applyState(StatsConfigState(max_list_size = -5, max_stats_count = Int.MAX_VALUE))
        assertEquals(1, ConfigFile.state.max_list_size)
        assertEquals(10000, ConfigFile.state.max_stats_count)
    }

    @Test
    fun `malformed files are not overwritten or treated as consent`() {
        val file = directory.resolve("stats/config.yml")
        Files.createDirectories(file.parent)
        val invalid = "opt_in: [broken\n"
        Files.writeString(file, invalid)
        assertThrows(Exception::class.java) { ConfigFile().loadConfig() }
        assertEquals(invalid, Files.readString(file))
        assertFalse(ConfigFile.isOptedIn())
    }

    @Test
    fun `failed save never enables telemetry or notifies the runtime`() {
        Files.writeString(directory.resolve("stats"), "not a directory")
        var changes = 0
        ConfigFile.onChange = { changes++ }
        assertThrows(Exception::class.java) { ConfigFile.markOptIn(true) }
        assertFalse(ConfigFile.isOptedIn())
        assertEquals(0, changes)
    }

    @Test
    fun `failed opt out save still revokes runtime consent and reports the failure`() {
        ConfigFile.state = StatsConfigState(opt_in = true, server_id = UUID.randomUUID().toString())
        Files.writeString(directory.resolve("stats"), "not a directory")
        var changes = 0
        ConfigFile.onChange = { changes++ }
        assertThrows(Exception::class.java) { ConfigFile.markOptIn(false) }
        assertFalse(ConfigFile.isOptedIn())
        assertEquals(1, changes)
    }
}
