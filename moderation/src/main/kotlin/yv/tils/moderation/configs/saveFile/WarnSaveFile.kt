/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.moderation.configs.saveFile

import yv.tils.configv2.files.ObjectMapperFileUtils
import java.util.UUID

class WarnSaveFile {
    companion object {
        val saves = mutableMapOf<UUID, WarnSave>()
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
        val warnSave = saves[uuid]

        if (warnSave != null) {
            warnSave.warningCount += 1
            warnSave.warnings.add(newWarn)
        } else {
            saves[uuid] = WarnSave(
                uuid = uuid.toString(),
                warningCount = 1,
                warnings = mutableListOf(newWarn)
            )
        }

        registerStrings(saves.values.toMutableList())
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
        val warnSave = saves[uuid] ?: return

        val warningToRemove = warnSave.warnings.find { it.id == warningID } ?: return

        warnSave.warnings.remove(warningToRemove)
        warnSave.warningCount -= 1

        registerStrings(saves.values.toMutableList())
    }

    fun clearWarnings(uuid: UUID) {
        saves.remove(uuid)

        registerStrings(saves.values.toMutableList())
    }
}
