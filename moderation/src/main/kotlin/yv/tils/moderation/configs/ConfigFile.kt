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

package yv.tils.moderation.configs

import yv.tils.configv2.data.ConfigEntry
import yv.tils.configv2.data.ConfigEntryFileUtils
import yv.tils.configv2.data.EntryType
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger

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
        fun getBoolean(key: String): Boolean? = when (val v = get(key)) {
            is Boolean -> v
            is String -> v.toBoolean()
            else -> null
        }
    }

    fun loadConfig() {
        val file = ConfigurateFileUtils.load("/moderation/config.yml", ConfigFormat.YAML)

        // populate legacy config map
        val flattened = ConfigurateFileUtils.flattenToMap(file.node)
        config.putAll(flattened)

        // ensure configNew contains base entries and then load values into them
        ensureBaseEntries()
        // load values into entries and populate index
        ConfigEntryFileUtils.loadFromNode(file.node, configNew)
        for (entry in configNew) {
            configIndex[entry.key] = entry
            val vv = entry.value ?: entry.defaultValue
            if (vv != null) config[entry.key] = vv
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        Logger.debug("ConfigFile.registerStrings called with ${content.size} entries", DEBUG_LEVEL.DETAILED)

        // Always start from base default entries
        ensureBaseEntries()

        // If a map is provided, set entry.value from it
        if (content.isNotEmpty()) {
            for (entry in configNew) {
                if (content.containsKey(entry.key)) {
                    Logger.debug(
                        "Updating entry ${entry.key} from ${entry.value} to ${content[entry.key]}",
                        DEBUG_LEVEL.VERBOSE
                    )
                    entry.value = content[entry.key]
                }
            }
        }

        // sync index and legacy map
        syncEntriesToMap()

        Logger.debug(
            "ConfigFile.registerStrings: about to create YAML file with ${configNew.size} entries",
            DEBUG_LEVEL.DETAILED
        )
        val ymlFile = ConfigEntryFileUtils.buildConfigFile("/moderation/config.yml", configNew, ConfigFormat.YAML)
        Logger.debug("ConfigFile.registerStrings: about to update file on disk", DEBUG_LEVEL.DETAILED)
        // Use update() with overwriteExisting = true so GUI edits overwrite existing keys
        ConfigurateFileUtils.update(ymlFile, overwriteExisting = true)
        Logger.debug("ConfigFile.registerStrings: file update complete", DEBUG_LEVEL.DETAILED)
    }

    private fun syncEntriesToMap() {
        configIndex.clear()
        for (entry in configNew) {
            configIndex[entry.key] = entry
            val vv = entry.value ?: entry.defaultValue
            if (vv != null) config[entry.key] = vv
        }
    }

    private fun ensureBaseEntries() {
        if (configNew.isNotEmpty()) return

        configNew.add(
            ConfigEntry(
                "documentation",
                EntryType.STRING,
                null,
                "https://docs.yvtils.net/moderation/config.yml",
                "Documentation URL"
            )
        )

        // populate index for fast lookups
        for (entry in configNew) configIndex[entry.key] = entry
    }
}