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

package yv.tils.regionsv2.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: RegionsV2ConfigState = RegionsV2ConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig] call - kept around purely so `ConfigFile.config["key"]`-style call
         * sites work without needing the full data class shape.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/regions-v2/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, RegionsV2ConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: RegionsV2ConfigState) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
