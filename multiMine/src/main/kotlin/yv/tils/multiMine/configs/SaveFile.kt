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

package yv.tils.multiMine.configs

import kotlinx.serialization.Serializable
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MultiMineSave>()

        private const val FILE_PATH = "/multiMine/save.json"
    }

    fun loadConfig() {
        val file = runCatching { JsonFileUtils.loadJsonFile(FILE_PATH) }.getOrNull() ?: return
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val uuid = saveNode.node("uuid").string ?: continue
            val toggled = saveNode.node("toggled").get(Boolean::class.javaObjectType) ?: continue

            saves[UUID.fromString(uuid)] = MultiMineSave(uuid, toggled)
        }
    }

    fun registerStrings(saveList: MutableList<MultiMineSave> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map { mapOf("uuid" to it.uuid, "toggled" to it.toggled) }
        )

        val jsonFile = JsonFileUtils.makeJsonFile(FILE_PATH, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
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

    @Serializable
    data class MultiMineSave (
        val uuid: String,
        var toggled: Boolean,
    )
}
