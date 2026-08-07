/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.server.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.logger.Logger

class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: ServerConfigState = ServerConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig]/[registerStrings] call - kept around purely so the module's existing
         * `ConfigFile.get("motd.enabled")`-style call sites keep working unchanged on top of
         * the new nested data class.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        fun get(key: String): Any? = config[key]

        /**
         * Updates a single known runtime-toggleable setting and persists it. Only
         * `"maintenance.enabled"` is ever actually set at runtime today (see
         * `MaintenanceHandler`) - unrecognized keys are logged and ignored rather than
         * silently accepted, since (unlike the old generic `ConfigEntry` map) there's no
         * arbitrary string-keyed backing store to fall back to anymore.
         */
        fun set(key: String, value: Any) {
            when (key) {
                "maintenance.enabled" -> state = state.copy(maintenance = state.maintenance.copy(enabled = value as Boolean))
                else -> {
                    Logger.debug("ConfigFile.set: unknown key '$key', ignoring")
                    return
                }
            }

            syncDerivedView()
            ConfigFile().registerStrings()
        }

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/server/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, ServerConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: ServerConfigState) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
