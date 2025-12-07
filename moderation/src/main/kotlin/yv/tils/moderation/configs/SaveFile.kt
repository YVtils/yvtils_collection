/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.moderation.configs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import yv.tils.config.files.FileUtils
import yv.tils.config.files.JSONFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.UUID

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, ModerationSave>()
    }

    fun loadConfig() {
        val file = JSONFileUtils.loadJSONFile("/moderation/save.json")
        val jsonFile = file.content
        val saveList = jsonFile["saves"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")

            val uuid = save.jsonObject["uuid"]?.toString()?.replace("\"", "") ?: continue
            val toggled = save.jsonObject["toggled"]?.toString()?.toBoolean() ?: continue

            saves[UUID.fromString(uuid)] = ModerationSave(uuid, toggled)
        }
    }

    fun registerStrings(saveList: MutableList<ModerationSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = JSONFileUtils.makeJSONFile("/moderation/save.json", saveWrapper)
        FileUtils.updateFile("/moderation/save.json", jsonFile, true)
    }

    fun updatePlayerSetting(uuid: UUID, state: Boolean) {
        if (saves.containsKey(uuid)) {
            saves[uuid]?.toggled = state
        } else {
            saves[uuid] = ModerationSave(uuid.toString(), state)
        }

        CoroutineHandler.launchTask(
            suspend { registerStrings(saves.values.toMutableList()) },
            null,
            isOnce = true,
        )
    }

    @Serializable
    data class ModerationSave(
        val uuid: String,
        var toggled: Boolean,
    )
}