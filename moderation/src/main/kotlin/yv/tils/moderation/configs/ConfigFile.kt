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

package yv.tils.moderation.configs

import org.bukkit.Material
import org.bukkit.Tag
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.files.YMLFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.DEBUGLEVEL
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
        val file = YMLFileUtils.loadYAMLFile("/moderation/config.yml")
        // populate legacy config map
        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value", DEBUGLEVEL.VERBOSE)
            if (value != null) config[key] = value
        }

        // ensure configNew contains base entries and then load values into them
        ensureBaseEntries()
        // load values into entries and populate index
        for (entry in configNew) {
            val v = file.content.get(entry.key)
            if (v != null) entry.value = v
            configIndex[entry.key] = entry
            val vv = entry.value ?: entry.defaultValue
            if (vv != null) config[entry.key] = vv
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        Logger.debug("ConfigFile.registerStrings called with ${content.size} entries", DEBUGLEVEL.DETAILED)

        // Always start from base default entries
        ensureBaseEntries()

        // If a map is provided, set entry.value from it
        if (content.isNotEmpty()) {
            for (entry in configNew) {
                if (content.containsKey(entry.key)) {
                    Logger.debug(
                        "Updating entry ${entry.key} from ${entry.value} to ${content[entry.key]}",
                        DEBUGLEVEL.VERBOSE
                    )
                    entry.value = content[entry.key]
                }
            }
        }

        // sync index and legacy map
        syncEntriesToMap()

        Logger.debug(
            "ConfigFile.registerStrings: about to create YAML file with ${configNew.size} entries",
            DEBUGLEVEL.DETAILED
        )
        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries("/moderation/config.yml", configNew)
        Logger.debug("ConfigFile.registerStrings: about to update file on disk", DEBUGLEVEL.DETAILED)
        // Use updateFile with overwriteExisting = true so GUI edits overwrite existing keys
        yv.tils.config.files.FileUtils.updateFile("/moderation/config.yml", ymlFile, overwriteExisting = true)
        Logger.debug("ConfigFile.registerStrings: file update complete", DEBUGLEVEL.DETAILED)
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