/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.multiMine.configs

import yv.tils.config.files.ObjectMapperFileUtils
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MultiMineSave>()
        private val lock = Any()

        private const val FILE_PATH = "/multiMine/save.json"
    }

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<MultiMineSave>(FILE_PATH)

        if (loaded.isEmpty()) {
            // Either save.json doesn't exist yet (first run) or it's genuinely empty (no
            // player has toggled multiMine yet) - either way there's nothing to lose, so
            // (re-)writing an empty list is safe and ensures the file exists going forward.
            registerStrings()
            return
        }

        saves.clear()
        loaded.forEach { saves[UUID.fromString(it.uuid)] = it }
    }

    fun registerStrings(saveList: MutableList<MultiMineSave> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(FILE_PATH, saveList)
    }

    fun updatePlayerSetting(uuid: UUID, state: Boolean) {
        synchronized(lock) {
            val next = saves.toMutableMap().apply { put(uuid, MultiMineSave(uuid.toString(), state)) }
            registerStrings(next.values.toMutableList())
            saves.clear()
            saves.putAll(next)
        }
    }

    data class MultiMineSave(
        val uuid: String,
        var toggled: Boolean,
    )
}
