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

package yv.tils.yv_smp.configs

import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.files.YMLFileUtils
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger

/**
 * Configuration file handler for the YV_SMP module.
 *
 * Manages configuration values for:
 * - music.master_volume: Master volume for all audio playback (0.0 - 1.0)
 * - music.enabled: Whether music system is enabled
 */
class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
        val configNew: MutableList<ConfigEntry> = mutableListOf()
        private val configIndex: MutableMap<String, ConfigEntry> = mutableMapOf()

        fun getConfigEntry(key: String): ConfigEntry? = configIndex[key]

        fun get(key: String): Any? {
            val e = getConfigEntry(key)
            return e?.value ?: e?.defaultValue ?: config[key]
        }

        fun getString(key: String): String? = get(key)?.toString()
        fun getInt(key: String): Int? = (get(key) as? Number)?.toInt()
        fun getDouble(key: String): Double? = (get(key) as? Number)?.toDouble()
        fun getFloat(key: String): Float? = (get(key) as? Number)?.toFloat()
        fun getBoolean(key: String): Boolean? = get(key) as? Boolean
        fun getList(key: String): List<*>? = get(key) as? List<*>
        fun getStringList(key: String): List<String>? = getList(key)?.mapNotNull { it?.toString() }

        /**
         * Get the master volume for audio playback.
         * @return Volume level between 0.0 (mute) and 1.0 (full volume), default 0.5
         */
        fun getMasterVolume(): Float {
            val volume = getFloat("music.master_volume") ?: 0.5f
            return volume.coerceIn(0.0f, 1.0f)
        }

        /**
         * Check if music system is enabled.
         * @return true if enabled, false otherwise
         */
        fun isMusicEnabled(): Boolean {
            return getBoolean("music.enabled") ?: true
        }
    }

    private val filePath = "/yv_smp/config.yml"

    fun loadConfig() {
        val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)
            if (value != null) {
                Logger.debug("Loading YV_SMP config key: $key -> $value", DEBUGLEVEL.VERBOSE)
                config[key] = value
            }
        }
    }

    fun registerStrings() {
        val entries = mutableListOf<ConfigEntry>()

        // Documentation
        entries.add(
            ConfigEntry(
                "documentation",
                EntryType.STRING,
                null,
                "https://docs.yvtils.net/yv_smp/config.yml",
                "Documentation URL for YV_SMP configuration"
            )
        )

        // Music System Settings
        entries.add(
            ConfigEntry(
                "music.enabled",
                EntryType.BOOLEAN,
                null,
                true,
                "Enable or disable the music system (requires Simple Voice Chat)"
            )
        )

        entries.add(
            ConfigEntry(
                "music.master_volume",
                EntryType.DOUBLE,
                null,
                0.5,
                "Master volume for all audio playback (0.0 = mute, 1.0 = full volume). " +
                        "This affects all music played via /music commands. Default: 0.5 (50%)"
            )
        )

        entries.add(
            ConfigEntry(
                "music.default_volume",
                EntryType.DOUBLE,
                null,
                1.0,
                "Default volume for individual audio tracks before master volume is applied (0.0 - 1.0). " +
                        "Default: 1.0 (100%)"
            )
        )

        entries.add(
            ConfigEntry(
                "music.allow_player_override",
                EntryType.BOOLEAN,
                null,
                false,
                "Allow players to override master volume with their own volume settings (future feature)"
            )
        )

        // Store entries in configNew list
        configNew.addAll(entries)

        // Build index for quick lookup
        entries.forEach { entry ->
            configIndex[entry.key] = entry
        }

        // Create YAML file
        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries(filePath, entries)
        yv.tils.config.files.FileUtils.saveFile(filePath, ymlFile)
    }
}


