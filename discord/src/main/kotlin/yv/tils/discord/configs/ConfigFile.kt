/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.discord.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

// TODO: Add option to allow multiple whitelist channels
class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: DiscordConfigState = DiscordConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig]/[registerStrings] call - kept around purely so the module's many
         * existing `ConfigFile.getValueAsString("botSettings.onlineStatus")`-style call
         * sites keep working unchanged on top of the new nested data class.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        fun getValue(key: String): Any? = config[key]
        fun getValueAsString(key: String): String? = config[key]?.toString()
        fun getValueAsInt(key: String): Int? = config[key]?.toString()?.toIntOrNull()
        fun getValueAsBoolean(key: String): Boolean? = config[key]?.toString()?.toBoolean()

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/discord/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, DiscordConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: DiscordConfigState) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
