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

package yv.tils.common.config

import yv.tils.config.files.YMLFileUtils
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.utils.logger.Logger

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()

        fun getValue(key: String): Any? {
            return config[key]
        }

        fun getValueAsString(key: String): String? {
            return config[key]?.toString()
        }

        fun getValueAsInt(key: String): Int? {
            return config[key]?.toString()?.toIntOrNull()
        }

        fun getValueAsBoolean(key: String): Boolean? {
            return config[key]?.toString()?.toBoolean()
        }
    }

    private val filePath = "/config.yml"

    fun loadConfig() {
    val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        val entries = mutableListOf<ConfigEntry>()

        if (content.isEmpty()) {
            entries.add(ConfigEntry("documentation", EntryType.STRING, null, "https://docs.yvtils.net/config.yml", "Documentation URL"))
            entries.add(ConfigEntry("language", EntryType.STRING, null, "en", "Default language"))
            entries.add(ConfigEntry("serverIP", EntryType.STRING, null, "smp.net", "Server IP"))
            entries.add(ConfigEntry("serverPort", EntryType.INT, null, -1, "Server port"))
            entries.add(ConfigEntry("timezone", EntryType.STRING, null, "default", "Timezone"))
            entries.add(ConfigEntry("updateCheck.enabled", EntryType.BOOLEAN, null, true, "Update check enabled"))
            entries.add(ConfigEntry("updateCheck.sendToOps", EntryType.BOOLEAN, null, true, "Send updates to ops"))
            entries.add(ConfigEntry("debug.active", EntryType.BOOLEAN, null, false, "Debug active"))
            entries.add(ConfigEntry("debug.level", EntryType.INT, null, 3, "Debug level"))
        } else {
            for ((k, v) in content) {
                val type = when (v) {
                    is Boolean -> EntryType.BOOLEAN
                    is Int -> EntryType.INT
                    is Double -> EntryType.DOUBLE
                    is List<*> -> EntryType.LIST
                    is Map<*, *> -> EntryType.MAP
                    is String -> EntryType.STRING
                    else -> EntryType.UNKNOWN
                }
                entries.add(ConfigEntry(k, type, null, v, null))
            }
        }

        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries(filePath, entries)
        yv.tils.config.files.FileUtils.saveFile(filePath, ymlFile)
    }
}
