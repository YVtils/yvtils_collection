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

package yv.tils.stats.configs

import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.files.FileUtils
import yv.tils.config.files.YMLFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger

/**
 * Configuration file handler for the stats module.
 *
 * Manages loading, saving, and accessing configuration values for:
 * - opt_in: Whether the user has opted in to stats collection
 * - metadata.server_name: Optional server name for identification
 * - metadata.collect_player_count: Whether to collect player count
 *
 * Note: Stats are pushed to api.yvtils.net/stats and the endpoint
 * is not configurable. Only the opt-in and metadata settings can be changed.
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
        fun getLong(key: String): Long? = (get(key) as? Number)?.toLong()
        fun getBoolean(key: String): Boolean? = when (val v = get(key)) {
            is Boolean -> v
            is String -> v.toBoolean()
            else -> null
        }

        /**
         * Check if opt-in is required (user hasn't made a decision yet).
         */
        fun needsOptInPrompt(): Boolean {
            // If opt_in is null or missing, we need to prompt
            val value = config["opt_in"]
            return value == null
        }

        /**
         * Check if the user has opted in to stats collection.
         */
        fun isOptedIn(): Boolean {
            return getBoolean("opt_in") ?: false
        }

        /**
         * Persist the opt-in decision.
         */
        fun markOptIn(decision: Boolean) {
            config["opt_in"] = decision
            val existing = getConfigEntry("opt_in")
            if (existing != null) {
                existing.value = decision
            }

            CoroutineHandler.launchTask(
                suspend { ConfigFile().registerStrings(config) },
                null,
                isOnce = true,
            )

            Logger.info("[Stats] Opt-in decision recorded: $decision")
        }
    }

    fun loadConfig() {
        val file = YMLFileUtils.loadYAMLFile("/stats/config.yml")
        
        // Populate legacy config map
        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)
            Logger.debug("Loading config key: $key -> $value", DEBUGLEVEL.VERBOSE)
            if (value != null) config[key] = value
        }

        // Ensure configNew contains base entries and then load values into them
        ensureBaseEntries()
        
        // Load values into entries and populate index
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
                    Logger.debug("Updating entry ${entry.key} from ${entry.value} to ${content[entry.key]}", DEBUGLEVEL.VERBOSE)
                    entry.value = content[entry.key]
                }
            }
        }

        // Sync index and legacy map
        syncEntriesToMap()

        Logger.debug("ConfigFile.registerStrings: about to create YAML file with ${configNew.size} entries", DEBUGLEVEL.DETAILED)
        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries("/stats/config.yml", configNew)
        Logger.debug("ConfigFile.registerStrings: about to update file on disk", DEBUGLEVEL.DETAILED)
        FileUtils.updateFile("/stats/config.yml", ymlFile, overwriteExisting = true)
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

        configNew.add(ConfigEntry(
            "documentation",
            EntryType.STRING,
            null,
            "https://docs.yvtils.net/stats/config.yml",
            "Documentation URL"
        ))

        // Note: opt_in starts as null (unset) - not false
        // This allows us to detect if the user hasn't made a decision yet
        configNew.add(ConfigEntry(
            "opt_in",
            EntryType.BOOLEAN,
            null,
            true, // No default - must be explicitly set
            "Whether you have opted in to anonymous stats collection. Set to true to enable, false to disable. Stats are sent to api.yvtils.net/stats"
        ))

        configNew.add(ConfigEntry(
            "metadata.server_name",
            EntryType.STRING,
            null,
            "",
            "Optional human-readable name for the server shown in developer stats."
        ))

        configNew.add(ConfigEntry(
            "metadata.collect_player_count",
            EntryType.BOOLEAN,
            null,
            true,
            "Whether to include current player count in exported stats."
        ))

        configNew.add(ConfigEntry(
            "max_list_size",
            EntryType.INT,
            null,
            1000,
            "Maximum size for list-type stats to prevent memory issues."
        ))

        configNew.add(ConfigEntry(
            "max_stats_count",
            EntryType.INT,
            null,
            10000,
            "Maximum number of stats to prevent high cardinality issues."
        ))
    }
}
