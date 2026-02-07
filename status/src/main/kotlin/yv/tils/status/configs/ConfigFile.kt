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

package yv.tils.status.configs

import yv.tils.config.files.YMLFileUtils
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.utils.logger.Logger

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
    }

    private val filePath = "/status/config.yml"

    fun loadConfig() {
    val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        // If a map is provided, convert it to ConfigEntry list for compatibility
        val entries = mutableListOf<ConfigEntry>()

        if (content.isEmpty()) {
            entries.add(ConfigEntry("documentation", EntryType.STRING, null, "https://docs.yvtils.net/status/config.yml", "Documentation URL"))
            entries.add(ConfigEntry("display", EntryType.STRING, null, "<dark_gray>[<white><status><dark_gray>] |<white> <playerName>", "Display format"))
            entries.add(ConfigEntry("maxLength", EntryType.INT, null, 20, "Maximum display length"))
            entries.add(ConfigEntry("defaultStatus", EntryType.LIST, null, defaultStatus(), "Default statuses"))
            entries.add(ConfigEntry("blacklist", EntryType.LIST, null, blacklist(), "Blacklisted statuses"))
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

    private fun defaultStatus(): List<String> {
        val list: MutableList<String> = mutableListOf()

        list.add("<green>Online")
        list.add("<yellow>Away")
        list.add("<red>Busy")

        return list
    }

    private fun blacklist(): List<String> {
        val list: MutableList<String> = mutableListOf()

        return list
    }
}
