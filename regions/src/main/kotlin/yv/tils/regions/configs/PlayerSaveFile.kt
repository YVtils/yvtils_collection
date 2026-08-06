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

import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.regions.data.PlayerManager
import yv.tils.regions.data.RegionRoles
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class PlayerSaveFile {
    private val filePath = "/regions/player_save.json"

    fun loadConfig() {
        val file = runCatching { JsonFileUtils.loadJsonFile(filePath) }.getOrNull() ?: return
        val saveListNode = file.node.node("players")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val playerStr = saveNode.node("uuid").string ?: run {
                Logger.debug("Player UUID is empty, skipping.")
                continue
            }

            val regionStr = saveNode.node("region").string ?: run {
                Logger.debug("Region UUID is empty, skipping.")
                continue
            }
            val roleStr = saveNode.node("role").string ?: run {
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
        val saveWrapper = mapOf(
            "players" to saveList.map { mapOf("uuid" to it.uuid, "region" to it.region, "role" to it.role.name) }
        )
        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        // Matches the original's `FileUtils.updateFile(path, jsonFile)` (no
        // explicit overwrite flag, i.e. `overwriteExisting = false`) - merge
        // with whatever's already on disk rather than fully replacing it.
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = false)
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
        val saveWrapper = mapOf(
            "players" to saveList.map { mapOf("uuid" to it.uuid, "region" to it.region, "role" to it.role.name) }
        )
        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
    }
}
