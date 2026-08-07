/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.status.configs

import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, StatusSave>()
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
        if (saves.containsKey(uuid)) {
            saves[uuid]?.content = content
        } else {
            saves[uuid] = StatusSave(uuid.toString(), content)
        }

        CoroutineHandler.launchTask(
            suspend { registerStrings(saves.values.toMutableList()) },
            null,
            isOnce = true,
        )
    }

    data class StatusSave(
        val uuid: String,
        var content: String,
    )
}
