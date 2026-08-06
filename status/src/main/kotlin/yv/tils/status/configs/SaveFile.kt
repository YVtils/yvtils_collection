/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.status.configs

import kotlinx.serialization.Serializable
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, StatusSave>()
    }

    private val filePath = "/status/save.json"

    fun loadConfig() {
        val file = runCatching { JsonFileUtils.loadJsonFile(filePath) }.getOrNull() ?: return
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val uuid = saveNode.node("uuid").string ?: continue
            val content = saveNode.node("content").string ?: continue

            saves[UUID.fromString(uuid)] = StatusSave(uuid, content)
        }
    }

    fun registerStrings(saveList: MutableList<StatusSave> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map { mapOf("uuid" to it.uuid, "content" to it.content) }
        )

        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        // Matches the original's `FileUtils.updateFile(path, jsonFile)` (no
        // explicit overwrite flag, i.e. `overwriteExisting = false`) - merge
        // with whatever's already on disk rather than fully replacing it.
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = false)
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

    @Serializable
    data class StatusSave (
        val uuid: String,
        var content: String,
    )
}
