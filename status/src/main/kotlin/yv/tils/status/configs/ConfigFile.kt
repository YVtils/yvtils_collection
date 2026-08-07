/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.status.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: StatusConfigState = StatusConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig]/[registerStrings] call - kept around purely so the module's existing
         * `ConfigFile.config["display"]`-style call sites keep working unchanged on top of
         * the new data class.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/status/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, StatusConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: StatusConfigState) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
