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

package yv.tils.regions.configs

import kotlinx.serialization.json.*
import yv.tils.config.files.JSONFileUtils
import yv.tils.regions.data.PlayerManager
import yv.tils.regions.data.RegionRoles
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class PlayerSaveFile {
    private val filePath = "/regions/player_save.json"

    fun loadConfig() {
    val file = JSONFileUtils.loadJSONFile(filePath)
    val jsonFile = file.content
        val saveList = jsonFile["players"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")
            val playerStr = save.jsonObject["uuid"]?.jsonPrimitive?.content ?: run {
                Logger.debug("Player UUID is empty, skipping.")
                continue
            }

            val regionStr = save.jsonObject["region"]?.jsonPrimitive?.content ?: run {
                Logger.debug("Region UUID is empty, skipping.")
                continue
            }
            val roleStr = save.jsonObject["role"]?.jsonPrimitive?.content ?: run {
                Logger.debug("Role is empty, skipping.")
                continue
            }

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
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile)
    }

    fun updatePlayerSetting(uuid: UUID, rUUID: UUID, content: PlayerManager.PlayerRegion?) {
        PlayerManager.loadPlayer(uuid, rUUID, content)

        Logger.debug("Updating player save: $uuid -> $content")

        CoroutineHandler.launchTask(
            suspend {
                val allRegions = PlayerManager.savePlayer().values.flatMap { it.values }.toMutableList()
                upgradeStrings(allRegions)
            },
            null,
            isOnce = true,
        )
    }

    private fun upgradeStrings(saveList: MutableList<PlayerManager.PlayerRegion> = mutableListOf()) {
    val saveWrapper = mapOf("players" to saveList)
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile, true)
    }
}
