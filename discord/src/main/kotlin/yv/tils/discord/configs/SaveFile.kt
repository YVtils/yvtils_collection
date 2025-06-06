package yv.tils.discord.configs

import files.FileUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger

class SaveFile {
    companion object {
        val saves = mutableMapOf<String, DiscordSave>()
    }

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

            saves[discordUserID] = DiscordSave(
                discordUserID = discordUserID,
                minecraftName = minecraftName,
                minecraftUUID = minecraftUUID
            )
        }
    }

    fun registerStrings(saveList: MutableList<DiscordSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    fun addSave(discordUserID: String, minecraftName: String, minecraftUUID: String) {
        val newSave = DiscordSave(
            discordUserID = discordUserID,
            minecraftName = minecraftName,
            minecraftUUID = minecraftUUID
        )
        saves[discordUserID] = newSave
        registerStrings(saves.values.toMutableList())
    }

    fun removeSave(discordUserID: String) {
        saves.remove(discordUserID)
        registerStrings(saves.values.toMutableList())
    }

    fun getSave(discordUserID: String): DiscordSave? {
        return saves[discordUserID]
    }

    fun getAllSaves(): List<DiscordSave> {
        return saves.values.toList()
    }

    @Serializable
    data class DiscordSave (
        val discordUserID: String,
        val minecraftName: String,
        val minecraftUUID: String,
    )
}