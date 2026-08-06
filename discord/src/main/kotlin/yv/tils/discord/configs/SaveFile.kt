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

import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.discord.logic.whitelist.WhitelistEntry
import yv.tils.discord.logic.whitelist.WhitelistLogic
import yv.tils.utils.logger.Logger

class SaveFile {
    private val filePath = "/discord/save.json"

    fun loadConfig() {
        val file = runCatching { JsonFileUtils.loadJsonFile(filePath) }.getOrNull() ?: return
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val discordUserID = saveNode.node("discordUserID").string ?: continue
            val minecraftName = saveNode.node("minecraftName").string ?: continue
            val minecraftUUID = saveNode.node("minecraftUUID").string ?: continue

            WhitelistLogic.whitelistMap[discordUserID] = WhitelistEntry(
                discordUserID = discordUserID,
                minecraftName = minecraftName,
                minecraftUUID = minecraftUUID
            )
        }
    }

    fun registerStrings(saveList: MutableList<WhitelistEntry> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map {
                mapOf(
                    "discordUserID" to it.discordUserID,
                    "minecraftName" to it.minecraftName,
                    "minecraftUUID" to it.minecraftUUID,
                )
            }
        )

        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        // Matches the original's `FileUtils.updateFile(path, jsonFile)` (no
        // explicit overwrite flag, i.e. `overwriteExisting = false`) - merge
        // with whatever's already on disk rather than fully replacing it.
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = false)
    }

    private fun upgradeStrings(saveList: MutableList<WhitelistEntry> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map {
                mapOf(
                    "discordUserID" to it.discordUserID,
                    "minecraftName" to it.minecraftName,
                    "minecraftUUID" to it.minecraftUUID,
                )
            }
        )

        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
    }

    fun addSave(discordUserID: String, minecraftName: String, minecraftUUID: String) {
        val newSave = WhitelistEntry(
            discordUserID = discordUserID,
            minecraftName = minecraftName,
            minecraftUUID = minecraftUUID
        )
        WhitelistLogic.whitelistMap[discordUserID] = newSave
        upgradeStrings(WhitelistLogic.getAllEntries().toMutableList())
    }

    fun removeSave(discordUserID: String) {
        WhitelistLogic.whitelistMap.remove(discordUserID)
        upgradeStrings(WhitelistLogic.getAllEntries().toMutableList())
    }
}
