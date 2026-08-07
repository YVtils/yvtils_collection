/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.regions.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.regions.data.Flag
import yv.tils.regions.data.FlagType

class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: RegionsConfigState = RegionsConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig]/[registerStrings] call - kept around purely so the module's existing
         * `ConfigFile.getValueAsString("settings.region.max.size")`-style call sites keep
         * working unchanged on top of the new nested data class.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        fun getValue(key: String): Any? = config[key]
        fun getValueAsString(key: String): String? = config[key]?.toString()
        fun getValueAsInt(key: String): Int? = config[key]?.toString()?.toIntOrNull()
        fun getValueAsBoolean(key: String): Boolean? = config[key]?.toString()?.toBoolean()

        /**
         * Effective per-flag value (global boolean, role-based minimum role name, or the
         * flag's own default if it isn't configured at all), checked in the same
         * global -> role-based -> locked-global -> locked-role-based -> default priority
         * order the original dotted-key lookup used.
         */
        fun getFlags(): MutableMap<Flag, Any> {
            val flags: MutableMap<Flag, Any> = mutableMapOf()

            for (flag in Flag.entries) {
                flags[flag] = state.flags.global[flag]
                    ?: state.flags.role_based[flag]
                    ?: state.flags.locked.global[flag]
                    ?: state.flags.locked.role_based[flag]
                    ?: flag.defaultValue
            }

            return flags
        }

        fun getFlagTypes(): MutableMap<Flag, FlagType> {
            val flags: MutableMap<Flag, FlagType> = mutableMapOf()

            for (flag in Flag.entries) {
                flags[flag] = when {
                    state.flags.global.containsKey(flag) -> FlagType.GLOBAL
                    state.flags.role_based.containsKey(flag) -> FlagType.ROLE_BASED
                    state.flags.locked.global.containsKey(flag) -> FlagType.LOCKED_GLOBAL
                    state.flags.locked.role_based.containsKey(flag) -> FlagType.LOCKED_ROLE_BASED
                    else -> flag.defaultGroup
                }
            }

            return flags
        }

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/regions/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, RegionsConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: RegionsConfigState) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
