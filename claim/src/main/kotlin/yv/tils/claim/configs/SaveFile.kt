package yv.tils.claim.configs

import coroutine.CoroutineHandler
import files.FileUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import logger.Logger
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, StatusSave>()
    }

    private val filePath = "/claim/save.json"

    fun loadConfig() {
        val file = FileUtils.loadJSONFile(filePath)
        val jsonFile = file.content
        val saveList = jsonFile["regions"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")



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