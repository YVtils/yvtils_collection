/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.stats.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
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
        /** The single source of truth. */
        var state: StatsConfigState = StatsConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig]/[registerStrings] call - kept around purely so the module's existing
         * `ConfigFile.getString("metadata.server_name")`-style call sites keep working
         * unchanged on top of the new nested data class.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        fun get(key: String): Any? = config[key]
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
        fun needsOptInPrompt(): Boolean = state.opt_in == null

        /**
         * Check if the user has opted in to stats collection.
         */
        fun isOptedIn(): Boolean = state.opt_in ?: false

        /**
         * Persist the opt-in decision.
         */
        fun markOptIn(decision: Boolean) {
            state = state.copy(opt_in = decision)
            syncDerivedView()

            CoroutineHandler.launchTask(
                suspend { ConfigFile().registerStrings() },
                null,
                isOnce = true,
            )

            Logger.info("[Stats] Opt-in decision recorded: $decision")
        }

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/stats/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, StatsConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: StatsConfigState) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
