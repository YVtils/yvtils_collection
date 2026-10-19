/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.moderation.configs.saveFile

import yv.tils.config.files.ObjectMapperFileUtils
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WarnSaveFile {
    companion object {
        val saves = ConcurrentHashMap<UUID, WarnSave>()
        private val lock = Any()
    }

    private val filePath = "/moderation/warnedPlayers.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<WarnSave>(filePath)

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        saves.clear()
        loaded.forEach { saves[UUID.fromString(it.uuid)] = it }
    }

    fun registerStrings(saveList: MutableList<WarnSave> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList)
    }

    fun warnPlayer(uuid: UUID, newWarn: Warning) {
        synchronized(lock) {
            val warnings = (saves[uuid]?.warnings.orEmpty() + newWarn).toMutableList()
            val next = saves.toMutableMap().apply { put(uuid, WarnSave(uuid.toString(), warnings.size, warnings)) }
            registerStrings(next.values.toMutableList())
            saves[uuid] = next.getValue(uuid)
        }
    }

    fun getWarnings(uuid: UUID): List<Warning> {
        val warnSave = saves[uuid]
        return warnSave?.warnings ?: emptyList()
    }

    fun getWarningCount(uuid: UUID): Int {
        val warnSave = saves[uuid]
        return warnSave?.warningCount ?: 0
    }

    fun removeWarning(uuid: UUID, warningID: String) {
        synchronized(lock) {
            val previous = saves[uuid] ?: return
            val warnings = previous.warnings.filterNot { it.id == warningID }.toMutableList()
            val next = saves.toMutableMap().apply { put(uuid, previous.copy(warningCount = warnings.size, warnings = warnings)) }
            registerStrings(next.values.toMutableList())
            saves[uuid] = next.getValue(uuid)
        }
    }

    fun clearWarnings(uuid: UUID) {
        synchronized(lock) {
            registerStrings(saves.filterKeys { it != uuid }.values.toMutableList())
            saves.remove(uuid)
        }
    }
}
