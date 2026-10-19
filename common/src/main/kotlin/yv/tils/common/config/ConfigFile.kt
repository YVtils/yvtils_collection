/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.common.config

import yv.tils.config.files.ConfigFormat
import yv.tils.config.files.ObjectMapperFileUtils
import java.time.ZoneId
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.logger.Logger
import yv.tils.utils.server.ServerUtils
import yv.tils.utils.time.TimeUtils

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
        validate(state)
        registerStrings()
        syncDerivedView()
        applyRuntimeSettings()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: RootConfigState) {
        validate(newState)
        ObjectMapperFileUtils.save(filePath, newState, format = ConfigFormat.YAML)
        state = newState
        syncDerivedView()
        applyRuntimeSettings()
    }

    private fun validate(candidate: RootConfigState) {
        require(candidate.language.isNotBlank()) { "Default language must not be blank" }
        if (candidate.timezone != "default") ZoneId.of(candidate.timezone)
        require(candidate.serverPort == -1 || candidate.serverPort in 1..65535) { "Invalid server port" }
    }

    private fun applyRuntimeSettings() {
        LanguageHandler().setServerDefaultLanguage(state.language)
        Logger.setDebugMode(state.debug.active, state.debug.level)
        TimeUtils.timeZone = state.timezone
        ServerUtils.serverIP = state.serverIP
        ServerUtils.serverPort = state.serverPort
    }
}
