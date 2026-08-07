/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.common.config

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

/**
 * Note (pre-existing, not introduced by this migration): nothing currently calls
 * [loadConfig]/[registerStrings] from any module's `onLoad()`/`enablePlugin()` - every call
 * site (`PlayerJoin`, `PluginVersion`) only ever observes [state]'s in-memory defaults,
 * exactly as before this migration. See [yv.tils.configv2.GeneralConfig]'s own note for a
 * near-identical, previously-flagged case - left disconnected here too rather than silently
 * wiring it into some arbitrary module's lifecycle as part of an unrelated migration.
 */
class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: RootConfigState = RootConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig]/[registerStrings] call - kept around purely so the existing
         * `ConfigFile.getValueAsBoolean("updateCheck.enabled")`-style call sites keep working
         * unchanged on top of the new nested data class.
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

    private val filePath = "/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, RootConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }
}
