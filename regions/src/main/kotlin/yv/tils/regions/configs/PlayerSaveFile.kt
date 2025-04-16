package yv.tils.regions.configs

import coroutine.CoroutineHandler
import files.FileUtils
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger
import yv.tils.regions.data.RegionManager
import yv.tils.regions.data.RegionRoles
import java.util.*

class PlayerSaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MutableMap<UUID, RegionManager.PlayerRegion>>()
    }

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
            val player =  save.jsonObject["player"]?.toString() ?: continue
            val regionID = save.jsonObject["region"]?.toString() ?: continue
            val role = save.jsonObject["role"]?.toString() ?: continue

            val region = RegionManager.PlayerRegion(
                player = player,
                region = regionID,
                role = RegionRoles.valueOf(role)
            )
            val playerUUID = UUID.fromString(player)

            if (saves.containsKey(playerUUID)) {
                saves[playerUUID]?.put(UUID.fromString(regionID), region)
            } else {
                saves[playerUUID] = mutableMapOf(
                    UUID.fromString(regionID) to region
                )
            }
        }
    }

    fun registerStrings(saveList: MutableList<RegionManager.PlayerRegion> = mutableListOf()) {
        val saveWrapper = mapOf("players" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    fun updatePlayerSetting(uuid: UUID, content: RegionManager.PlayerRegion) {
        val regionID = UUID.fromString(content.region)
        if (saves.containsKey(uuid)) {
            saves[uuid]?.put(regionID, content)
        } else {
            saves[uuid] = mutableMapOf(
                regionID to content
            )
        }

        Logger.debug("Updating player save: $uuid -> $content")

        CoroutineHandler.launchTask(
            suspend {
                val allRegions = saves.values.flatMap { it.values }.toMutableList()
                registerStrings(allRegions)
            },
            null,
            isOnce = true,
        )
    }
}