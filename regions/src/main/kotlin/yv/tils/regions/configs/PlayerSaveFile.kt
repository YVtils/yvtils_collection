package yv.tils.regions.configs

import coroutine.CoroutineHandler
import files.FileUtils
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import logger.Logger
import yv.tils.regions.data.PlayerManager
import yv.tils.regions.data.RegionRoles
import java.util.*

class PlayerSaveFile {
    private val filePath = "/regions/player_save.json"

    fun loadConfig() {
        val file = FileUtils.loadJSONFile(filePath)
        val jsonFile = file.content
        val saveList = jsonFile["players"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")
            val playerStr = save.jsonObject["player"]?.jsonPrimitive?.content ?: continue
            val regionStr = save.jsonObject["region"]?.jsonPrimitive?.content ?: continue
            val roleStr = save.jsonObject["role"]?.jsonPrimitive?.content ?: continue

            try {
                val region = PlayerManager.PlayerRegion(
                    uuid = playerStr,
                    region = regionStr,
                    role = RegionRoles.fromString(roleStr)
                )
                val playerUUID = UUID.fromString(playerStr)
                val regionUUID = UUID.fromString(regionStr)

                PlayerManager.loadPlayer(playerUUID, regionUUID, region)
            } catch (e: IllegalArgumentException) {
                Logger.error("Failed to parse UUID: player=$playerStr, region=$regionStr")
                Logger.error("Error: ${e.message}")
            }
        }
    }

    fun registerStrings(saveList: MutableList<PlayerManager.PlayerRegion> = mutableListOf()) {
        val saveWrapper = mapOf("players" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    fun updatePlayerSetting(uuid: UUID, rUUID: UUID, content: PlayerManager.PlayerRegion?) {
        PlayerManager.loadPlayer(uuid, rUUID, content)

        Logger.debug("Updating player save: $uuid -> $content")

        CoroutineHandler.launchTask(
            suspend {
                val allRegions = PlayerManager.savePlayer().values.flatMap { it.values }.toMutableList()
                Logger.dev("All player regions: $allRegions")
                upgradeStrings(allRegions)
            },
            null,
            isOnce = true,
        )
    }

    private fun upgradeStrings(saveList: MutableList<PlayerManager.PlayerRegion> = mutableListOf()) {
        val saveWrapper = mapOf("players" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile, true)
    }
}
