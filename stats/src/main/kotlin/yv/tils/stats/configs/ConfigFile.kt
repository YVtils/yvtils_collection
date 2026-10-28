/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.stats.configs

import yv.tils.config.files.ConfigFormat
import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.utils.logger.Logger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
        @Volatile
        var state: StatsConfigState = StatsConfigState()

        @Volatile internal var onChange: (() -> Unit)? = null
        private val stateLock = Any()
        private const val FILE_PATH = "/stats/config.yml"

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * configuration change - kept around purely so the module's existing
         * `ConfigFile.getString("metadata.server_name")`-style call sites keep working
         * unchanged on top of the new nested data class.
         */
        val config: MutableMap<String, Any> = ConcurrentHashMap()

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
            synchronized(stateLock) {
                persistAndPublish(state.copy(opt_in = decision))
            }
            onChange?.invoke()
            Logger.info("[Stats] Opt-in decision recorded: $decision")
        }

        private fun persistAndPublish(candidate: StatsConfigState) {
            val normalized = candidate.copy(
                metadata = candidate.metadata.copy(),
                max_list_size = candidate.max_list_size.coerceIn(1, 1000),
                max_stats_count = candidate.max_stats_count.coerceIn(1, 10000),
                server_id = candidate.server_id.takeIf { runCatching { UUID.fromString(it) }.isSuccess }
                    ?: if (candidate.opt_in == true) UUID.randomUUID().toString() else "",
            )
            // Persist first: a failed save must not silently enable telemetry.
            try {
                ObjectMapperFileUtils.save(FILE_PATH, normalized, format = ConfigFormat.YAML)
            } catch (e: Exception) {
                // Withdrawal still takes effect in memory when storage fails. Report the
                // failure so the administrator knows it cannot survive a restart yet.
                if (normalized.opt_in != true) {
                    state = normalized
                    syncDerivedView()
                    onChange?.invoke()
                }
                throw e
            }
            state = normalized
            syncDerivedView()
        }

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    fun loadConfig() {
        synchronized(stateLock) {
            val loaded = ObjectMapperFileUtils.load(FILE_PATH, StatsConfigState(), format = ConfigFormat.YAML)
            persistAndPublish(loaded)
        }
        onChange?.invoke()
    }

    fun registerStrings() {
        synchronized(stateLock) {
            persistAndPublish(state)
        }
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: StatsConfigState) {
        synchronized(stateLock) {
            persistAndPublish(newState.copy(server_id = state.server_id))
        }
        onChange?.invoke()
    }
}
