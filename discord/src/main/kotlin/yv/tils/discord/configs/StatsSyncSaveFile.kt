package yv.tils.discord.configs

import yv.tils.config.files.FileUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import yv.tils.utils.logger.Logger

class StatsSyncSaveFile {
    companion object {
        var saves = mutableMapOf<String, StatsSyncSave>()
    }

    private val filePath = "/discord/statsSync.json"

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

            val guildID = save.jsonObject["guildID"]?.toString()?.replace("\"", "") ?: continue
            val status = save.jsonObject["status"]?.toString()?.replace("\"", "") ?: continue
            val version = save.jsonObject["version"]?.toString()?.replace("\"", "") ?: continue
            val playerCount = save.jsonObject["playerCount"]?.toString()?.replace("\"", "") ?: continue
            val lastRefreshed = save.jsonObject["lastRefreshed"]?.toString()?.replace("\"", "") ?: continue

            saves[guildID] = StatsSyncSave(
                guildID = guildID,
                status = status,
                version = version,
                playerCount = playerCount,
                lastRefreshed = lastRefreshed
            )
        }
    }

    fun registerStrings(saveList: MutableList<StatsSyncSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    @Serializable
    data class StatsSyncSave (
        val guildID: String,
        var status: String,
        var version: String,
        var playerCount: String,
        var lastRefreshed: String,
    )
}
