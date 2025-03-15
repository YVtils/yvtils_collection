package yv.tils.multiMine.configs

import coroutine.CoroutineHandler
import files.FileUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger
import java.util.*

class SaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MultiMineSave>()
    }

    fun loadConfig() {
        val file = FileUtils.loadJSONFile("/multiMine/save.json")
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

            saves[UUID.fromString(uuid)] = MultiMineSave(uuid, toggled)
        }
    }

    fun registerStrings(saveList: MutableList<MultiMineSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile("/multiMine/save.json", saveWrapper)
        FileUtils.saveFile("/multiMine/save.json", jsonFile)
    }

    fun updatePlayerSetting(uuid: UUID, state: Boolean) {
        if (saves.containsKey(uuid)) {
            saves[uuid]?.toggled = state
        } else {
            saves[uuid] = MultiMineSave(uuid.toString(), state)
        }

        Logger.dev(" Updating save for $uuid: ${saves[uuid]}")
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