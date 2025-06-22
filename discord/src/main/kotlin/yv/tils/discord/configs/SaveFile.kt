package yv.tils.discord.configs

import files.FileUtils
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger
import yv.tils.discord.logic.whitelist.WhitelistEntry
import yv.tils.discord.logic.whitelist.WhitelistLogic

class SaveFile {
    private val filePath = "/discord/save.json"

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
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    private fun upgradeStrings(saveList: MutableList<WhitelistEntry> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile, true)
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