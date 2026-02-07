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

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import yv.tils.config.files.JSONFileUtils
import yv.tils.discord.logic.whitelist.WhitelistEntry
import yv.tils.discord.logic.whitelist.WhitelistLogic
import yv.tils.utils.logger.Logger

class SaveFile {
    private val filePath = "/discord/save.json"

    fun loadConfig() {
    val file = JSONFileUtils.loadJSONFile(filePath)
    val jsonFile = file.content
        val saveList = jsonFile["saves"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")

            val discordUserID = save.jsonObject["discordUserID"]?.toString()?.replace("\"", "") ?: continue
            val minecraftName = save.jsonObject["minecraftName"]?.toString()?.replace("\"", "") ?: continue
            val minecraftUUID = save.jsonObject["minecraftUUID"]?.toString()?.replace("\"", "") ?: continue

            WhitelistLogic.whitelistMap[discordUserID] = WhitelistEntry(
                discordUserID = discordUserID,
                minecraftName = minecraftName,
                minecraftUUID = minecraftUUID
            )
        }
    }

    fun registerStrings(saveList: MutableList<WhitelistEntry> = mutableListOf()) {
    val saveWrapper = mapOf("saves" to saveList)
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile)
    }

    private fun upgradeStrings(saveList: MutableList<WhitelistEntry> = mutableListOf()) {
    val saveWrapper = mapOf("saves" to saveList)
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile, true)
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
