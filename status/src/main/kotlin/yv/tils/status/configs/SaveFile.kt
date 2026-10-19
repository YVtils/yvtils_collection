/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.status.configs

import yv.tils.config.files.ObjectMapperFileUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SaveFile {
    companion object {
        val saves = ConcurrentHashMap<UUID, StatusSave>()
        private val lock = Any()
    }

    private val filePath = "/status/save.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<StatusSave>(filePath)

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        saves.clear()
        loaded.forEach { saves[UUID.fromString(it.uuid)] = it }
    }

    fun registerStrings(saveList: MutableList<StatusSave> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList)
    }

    fun updatePlayerSetting(uuid: UUID, content: String) {
        synchronized(lock) {
            val next = saves.toMutableMap()
            if (content.isEmpty()) next.remove(uuid) else next[uuid] = StatusSave(uuid.toString(), content)
            registerStrings(next.values.toMutableList())
            saves.clear()
            saves.putAll(next)
        }
    }

    data class StatusSave(
        val uuid: String,
        var content: String,
    )
}
