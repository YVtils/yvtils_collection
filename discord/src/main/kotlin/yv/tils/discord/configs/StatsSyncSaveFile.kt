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

package yv.tils.discord.configs

import kotlinx.serialization.Serializable
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.utils.logger.Logger

class StatsSyncSaveFile {
    companion object {
        var saves = mutableMapOf<String, StatsSyncSave>()
    }

    private val filePath = "/discord/statsSync.json"

    fun loadConfig() {
        val file = runCatching { JsonFileUtils.loadJsonFile(filePath) }.getOrNull() ?: return
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val guildID = saveNode.node("guildID").string ?: continue
            val status = saveNode.node("status").string ?: continue
            val version = saveNode.node("version").string ?: continue
            val playerCount = saveNode.node("playerCount").string ?: continue
            val lastRefreshed = saveNode.node("lastRefreshed").string ?: continue

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
        val saveWrapper = mapOf(
            "saves" to saveList.map {
                mapOf(
                    "guildID" to it.guildID,
                    "status" to it.status,
                    "version" to it.version,
                    "playerCount" to it.playerCount,
                    "lastRefreshed" to it.lastRefreshed,
                )
            }
        )

        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        // Matches the original's `FileUtils.updateFile(path, jsonFile)` (no
        // explicit overwrite flag, i.e. `overwriteExisting = false`) - merge
        // with whatever's already on disk rather than fully replacing it.
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = false)
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
