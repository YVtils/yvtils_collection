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

package yv.tils.essentials.config

import org.bukkit.Material
import yv.tils.configv2.data.ConfigEntry
import yv.tils.configv2.data.ConfigEntryFileUtils
import yv.tils.configv2.data.EntryType
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.essentials.commands.handler.DimensionHandler
import yv.tils.essentials.commands.handler.PvPHandler
import yv.tils.utils.logger.Logger
import yv.tils.utils.logger.DEBUG_LEVEL as DEBUGLEVEL

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
        var blockList: MutableList<Material> = mutableListOf()
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

        fun set(key: String, value: Any) {
            val entry = getConfigEntry(key)
            if (entry != null) {
                entry.value = value
                config[key] = value
                // Update the file on disk whenever a value is set
                ConfigFile().registerStrings(config)
            } else {
                Logger.debug("Attempted to set unknown config key: $key", DEBUGLEVEL.VERBOSE)
            }
        }
    }

    fun loadConfig() {
        val file = ConfigurateFileUtils.load("/essentials/config.yml", ConfigFormat.YAML)
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

        PvPHandler().loadPvPState()
        DimensionHandler().loadDimensionStates() // TODO: Think about moving this to a separate save file maybe?
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
        val ymlFile = ConfigEntryFileUtils.buildConfigFile("/essentials/config.yml", configNew, ConfigFormat.YAML)
        Logger.debug("ConfigFile.registerStrings: about to update file on disk", DEBUGLEVEL.DETAILED)
        // Use update() with overwriteExisting = true so GUI edits overwrite existing keys
        ConfigurateFileUtils.update(ymlFile, overwriteExisting = true)
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
                "https://docs.yvtils.net/essentials/config.yml",
                "Documentation URL"
            )
        )
        configNew.add(
            ConfigEntry(
                "pvpState",
                EntryType.BOOLEAN,
                null,
                true,
                "Whether PvP is enabled or disabled on the server",
                dynamicInvItem = { if (it.value as? Boolean == true) Material.DIAMOND_SWORD else Material.RED_DYE }
            )
        )

        // populate index for fast lookups
        for (entry in configNew) configIndex[entry.key] = entry
    }
}
