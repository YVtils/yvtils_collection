package yv.tils.status.configs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import yv.tils.config.files.JSONFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, StatusSave>()
    }

    private val filePath = "/status/save.json"

    fun loadConfig() {
    val file = JSONFileUtils.loadJSONFile(filePath)
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
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    // Use FileUtils.updateFile for merging/overwriting logic (keeps existing behavior)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile)
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
