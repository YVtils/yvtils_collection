/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.multiMine.configs

import org.bukkit.Material
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger

class ConfigFile {
    companion object {
        private const val FILE_PATH = "/multiMine/config.yml"

        /** The single source of truth - edited directly by [yv.tils.gui.logic.DataClassConfigGui]. */
        var state: MultiMineConfigState = MultiMineConfigState()

        /**
         * Flattened, read-only-by-convention views derived from [state], kept around purely
         * for the rest of the module's existing `ConfigFile.config["key"]`/`ConfigFile.blockList`
         * call sites - re-synced on every [loadConfig]/[registerStrings]/[updateBlockList] call.
         */
        val config: MutableMap<String, Any> = mutableMapOf()
        var blockList: MutableList<Material> = mutableListOf()

        fun get(key: String): Any? = config[key]
        fun getString(key: String): String? = get(key)?.toString()
        fun getInt(key: String): Int? = (get(key) as? Number)?.toInt()
        fun getBoolean(key: String): Boolean? = when (val v = get(key)) {
            is Boolean -> v
            is String -> v.toBoolean()
            else -> null
        }

        private fun syncDerivedViews() {
            config.clear()
            config["documentation"] = state.documentation
            config["defaultState"] = state.defaultState
            config["animationTime"] = state.animationTime
            config["cooldownTime"] = state.cooldownTime
            config["breakLimit"] = state.breakLimit
            config["leaveDecay"] = state.leaveDecay
            config["matchBlockTypeOnly"] = state.matchBlockTypeOnly
            config["canToolsBreak"] = state.canToolsBreak
            config["blocks"] = state.blocks

            blockList = state.blocks.mapNotNull { name ->
                Material.getMaterial(name) ?: run {
                    Logger.error("Trying to load a block that does not exist: $name")
                    null
                }
            }.toMutableList()
        }
    }

    fun loadConfig() {
        // Reads whatever's on disk, falling back to each field's own Kotlin default for
        // anything missing (e.g. a field added to MultiMineConfigState after config.yml was
        // first created). Re-persisting straight after is safe (not destructive) precisely
        // because `state` already reflects disk-plus-new-defaults at this point.
        state = ObjectMapperFileUtils.load(FILE_PATH, MultiMineConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedViews()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(FILE_PATH, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: MultiMineConfigState) {
        state = newState
        syncDerivedViews()
        registerStrings()
    }

    fun updateBlockList(blocks: MutableList<Material>) {
        state = state.copy(blocks = blocks.map { it.name })
        syncDerivedViews()

        CoroutineHandler.launchTask(
            suspend { registerStrings() },
            null,
            isOnce = true,
        )
    }
}
