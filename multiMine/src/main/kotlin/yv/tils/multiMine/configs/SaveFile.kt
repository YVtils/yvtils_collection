/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.multiMine.configs

import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MultiMineSave>()

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
        if (saves.containsKey(uuid)) {
            saves[uuid]?.toggled = state
        } else {
            saves[uuid] = MultiMineSave(uuid.toString(), state)
        }

        CoroutineHandler.launchTask(
            suspend { registerStrings(saves.values.toMutableList()) },
            null,
            isOnce = true,
        )
    }

    data class MultiMineSave(
        val uuid: String,
        var toggled: Boolean,
    )
}
