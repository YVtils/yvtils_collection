package yv.tils.status.configs

import coroutine.CoroutineHandler
import files.FileUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, StatusSave>()
    }

    private val filePath = "/status/save.json"

    fun loadConfig() {
        val file = FileUtils.loadJSONFile(filePath)
        val jsonFile = file.content
        val saveList = jsonFile["saves"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")

            val uuid = save.jsonObject["uuid"]?.toString()?.replace("\"", "") ?: continue
            val content = save.jsonObject["content"]?.toString() ?: continue

            saves[UUID.fromString(uuid)] = StatusSave(uuid, content)
        }
    }

    fun registerStrings(saveList: MutableList<StatusSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
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