package yv.tils.migration.config

import files.FileUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger

class SaveFile {
    companion object {
        val saveFile = mutableListOf<MigrationEntry>()

        fun wasMigrated(configFileName: String): Boolean {
            return saveFile.any { it.configFileName == configFileName && it.migrated }
        }
    }

    private val filePath = "/migration/save.json"

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

            val configFileName = save.jsonObject["configFileName"]?.toString()?.replace("\"", "") ?: continue
            val migrated = save.jsonObject["migrated"]?.toString()?.toBoolean() ?: false

            val migrationEntry = MigrationEntry(configFileName, migrated)
            saveFile.add(migrationEntry)
            Logger.debug("Migration entry loaded: $migrationEntry")
        }
    }

    fun registerStrings(saveList: MutableList<MigrationEntry> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    private fun upgradeStrings(saveList: MutableList<MigrationEntry> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile, true)
    }
}

@Serializable
data class MigrationEntry(
    val configFileName: String,
    val migrated: Boolean,
)
