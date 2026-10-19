/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.essentials.config

import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.essentials.commands.handler.PvPHandler

class StatesFile {
    companion object {
        private const val FILE_PATH = "/essentials/states.json"

        var state: EssentialsState = EssentialsState()
    }

    fun loadConfig() {
        // Reads whatever's on disk, falling back to each field's own Kotlin default for
        // anything missing (e.g. a field added to EssentialsState after states.json was
        // first created). Re-persisting straight after is safe (not destructive) precisely
        // because `state` already reflects disk-plus-new-defaults at this point - unlike the
        // old onLoad()-registers-before-loadConfig() ordering, load always runs before save.
        state = ObjectMapperFileUtils.load(FILE_PATH, EssentialsState())
        registerStrings()

        PvPHandler().loadPvPState()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(FILE_PATH, state)
    }

    fun updatePvPState(pvpState: Boolean) {
        val next = state.copy(pvpState = pvpState)
        ObjectMapperFileUtils.save(FILE_PATH, next)
        state = next
    }

    fun updateDimensionState(dimension: String, allowed: Boolean) {
        val next = state.copy(dimensions = state.dimensions.toMutableMap().apply { put(dimension, allowed) })
        ObjectMapperFileUtils.save(FILE_PATH, next)
        state = next
    }

    data class EssentialsState(
        var pvpState: Boolean = true,
        val dimensions: MutableMap<String, Boolean> = mutableMapOf()
    )
}
