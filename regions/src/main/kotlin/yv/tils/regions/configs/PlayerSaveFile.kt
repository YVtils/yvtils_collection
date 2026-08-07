/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.regions.configs

import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.regions.data.PlayerManager
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class PlayerSaveFile {
    private val filePath = "/regions/player_save.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<PlayerManager.PlayerRegion>(filePath, key = "players")

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        loaded.forEach {
            try {
                val playerUUID = UUID.fromString(it.uuid)
                val regionUUID = UUID.fromString(it.region)
                PlayerManager.loadPlayer(playerUUID, regionUUID, it)
            } catch (e: IllegalArgumentException) {
                Logger.error("Failed to parse UUID: player=${it.uuid}, region=${it.region}")
                Logger.error("Error: ${e.message}")
            }
        }
    }

    fun registerStrings(saveList: MutableList<PlayerManager.PlayerRegion> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList, key = "players")
    }

    fun updatePlayerSetting(uuid: UUID, rUUID: UUID, content: PlayerManager.PlayerRegion?) {
        PlayerManager.loadPlayer(uuid, rUUID, content)

        CoroutineHandler.launchTask(
            suspend {
                val allRegions = PlayerManager.savePlayer().values.flatMap { it.values }.toMutableList()
                registerStrings(allRegions)
            },
            null,
            isOnce = true,
        )
    }
}
