package yv.tils.migration.discord

import data.Data
import files.FileUtils
import logger.Logger
import yv.tils.migration.config.SaveFile
import java.io.File

class DiscordMigrationLogic {
    companion object {
        val oldSaveFile = mutableListOf<Structures.OldSaveFileStructure>()
        var oldConfigFile: Structures.OldConfigFileStructure? = null

        val directory = "discord"
        val oldConfigName = "config.yml"
        val oldSaveName = "save.yml"
    }

    fun startMigration() {
        if (migrationNeeded()) {
            Logger.info("Starting migration for discord module...")
            if (checkForOldConfigFile()) {
                Logger.info("Old config file found, starting migration for config.")
                getOldConfig()
                Logger.info("Old config file loaded, generating new config file.")
                generateNewConfig()
                Logger.info("New config file generated.")
            } else {
                Logger.info("Old config file not found, skipping migration for config.")
            }

            if (checkForOldSaveFile()) {
                Logger.info("Old save file found, starting migration for save.")
                getOldSaveFile()
                Logger.info("Old save file loaded, generating new save file.")
                generateNewSaveFile()
                Logger.info("New save file generated.")
            } else {
                Logger.info("Old save file not found, skipping migration for save.")
            }

            cleanUp()
        }
    }

    private fun generateNewConfig() {
        if (oldConfigFile == null) {
            Logger.warn("Old config file is null, cannot generate new config.")
            return
        }

        // Use extension function for mapping
        val newConfigFileStructure = oldConfigFile !!.toNewConfig()

        // Use utility to convert to map for YAML/JSON
        val content = newConfigFileStructure.toMap().toMutableMap()
        content["documentation"] = newConfigFileStructure.documentation

        val filePath = "$directory/config.yml"
        val ymlFile = FileUtils.makeYAMLFile(filePath, content.filterValues { it != null } as Map<String, Any>)
        FileUtils.saveFile(filePath, ymlFile)
    }

    private fun generateNewSaveFile() {
        if (oldSaveFile.isEmpty()) {
            Logger.warn("Old save file is empty, cannot generate new save file.")
            return
        }

        val newSaveFileStructure = mutableListOf<Structures.NewSaveFileStructure>()

        for (oldSaveEntry in oldSaveFile) {
            val newSaveEntry = Structures.NewSaveFileStructure(
                discordUserID = oldSaveEntry.key,
                minecraftName = oldSaveEntry.value.name,
                minecraftUUID = oldSaveEntry.value.uuid
            )
            newSaveFileStructure.add(newSaveEntry)
            Logger.debug("New save entry created: $newSaveEntry")
        }

        val filePath = "$directory/save.json"

        val saveWrapper = mapOf("saves" to newSaveFileStructure)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    private fun getOldConfig() {
        val oldConfigFile = Structures.OldConfigFileStructure()

        FileUtils.loadYAMLFile("$directory/$oldConfigName").let { file ->
            for (key in file.content.getKeys(true)) {
                val value = file.content.get(key)
                Logger.debug("Loading old config key: $key -> $value")

                when (key) {
                    "botToken" -> oldConfigFile.botToken = value as String
                    "mainGuild" -> oldConfigFile.mainGuild = value as String
                    "botSettings.onlineStatus" -> oldConfigFile.botSettings_onlineStatus = value as String
                    "botSettings.activity" -> oldConfigFile.botSettings_activity = value as String
                    "botSettings.activityMessage" -> oldConfigFile.botSettings_activityMessage = value as String
                    "embedSettings.author" -> oldConfigFile.embedSettings_author = value as String
                    "embedSettings.authorIconURL" -> oldConfigFile.embedSettings_authorIconURL = value as String
                    "whitelistFeature.channel" -> oldConfigFile.whitelistFeature_channel = value as String
                    "whitelistFeature.role" -> oldConfigFile.whitelistFeature_role = value as String
                    "serverInfoCommand.permission" -> oldConfigFile.serverInfoCommand_permission = value as String
                    "whitelistCommand.permission" -> oldConfigFile.whitelistCommand_permission = value as String
                    "chatSync.enabled" -> oldConfigFile.chatSync_enabled = value as Boolean
                    "chatSync.permission" -> oldConfigFile.chatSync_permission = value as String
                    "chatSync.channel" -> oldConfigFile.chatSync_channel = value as String
                    "consoleSync.enabled" -> oldConfigFile.consoleSync_enabled = value as Boolean
                    "consoleSync.channel" -> oldConfigFile.consoleSync_channel = value as String
                    "serverStats.enabled" -> oldConfigFile.serverStats_enabled = value as Boolean
                    "serverStats.mode" -> oldConfigFile.serverStats_mode = value as String
                    "serverStats.channel" -> oldConfigFile.serverStats_channel = value as String
                    "serverStats.layout.serverStatus.text" -> oldConfigFile.serverStats_layout_serverStatus_text =
                        value as String

                    "serverStats.layout.serverStatus.emoji.online" -> oldConfigFile.serverStats_layout_serverStatus_emoji_online =
                        value as String

                    "serverStats.layout.serverStatus.emoji.offline" -> oldConfigFile.serverStats_layout_serverStatus_emoji_offline =
                        value as String

                    "serverStats.layout.serverVersion" -> oldConfigFile.serverStats_layout_serverVersion =
                        value as String

                    "serverStats.layout.lastPlayerCount" -> oldConfigFile.serverStats_layout_lastPlayerCount =
                        value as String

                    "serverStats.layout.lastRefreshed" -> oldConfigFile.serverStats_layout_lastRefreshed =
                        value as String

                    "logChannel" -> oldConfigFile.logChannel = value as String
                    else -> Logger.warn("Unknown key in old config: $key")
                }
            }
        }

        backupOldFiles(oldConfigName)
    }

    private fun getOldSaveFile() {
        FileUtils.loadYAMLFile("$directory/$oldSaveName").let { file ->
            for (key in file.content.getKeys(true)) {
                val value = file.content.get(key)
                Logger.debug("Loading old save key: $key -> $value")

                if (value is String) {
                    val parts = value.split(" ")
                    if (parts.size == 2) {
                        val name = parts[0]
                        val uuid = parts[1]

                        val oldSaveEntry = Structures.OldSaveFileStructure(
                            key,
                            Structures.OldSaveFileValueStructure(name, uuid)
                        )
                        oldSaveFile.add(oldSaveEntry)
                        Logger.debug("Old save entry loaded: $oldSaveEntry")
                    } else {
                        Logger.warn("Invalid format for old save entry: $value")
                    }
                } else {
                    Logger.warn("Unexpected value type for old save entry: $value")
                }
            }
        }

        backupOldFiles(oldSaveName)
    }

    private fun checkForOldConfigFile(): Boolean {
        val path = "$directory/$oldConfigName"

        val file = File(Data.instance.dataFolder, path)
        return file.exists()
    }

    private fun checkForOldSaveFile(): Boolean {
        val path = "$directory/$oldSaveName"

        val file = File(Data.instance.dataFolder, path)
        return file.exists()
    }

    private fun migrationNeeded(): Boolean {
        val wasConfigMigrated = SaveFile.wasMigrated(oldConfigName)
        val wasSaveMigrated = SaveFile.wasMigrated(oldSaveName)

        return ! wasConfigMigrated || ! wasSaveMigrated
    }

    private fun cleanUp() {
        oldSaveFile.clear()
        oldConfigFile = null
    }

    private fun backupOldFiles(fileName: String) {
        // Backup old files if needed
        val oldFilePath = "$directory/$fileName"
        val oldFile = File(Data.instance.dataFolder, oldFilePath)

        if (oldFile.exists()) {
            val backupFilePath = "$directory/backup_$fileName"
            val backupFile = File(Data.instance.dataFolder, backupFilePath)
            oldFile.copyTo(backupFile, overwrite = true)
            Logger.info("Backed up old file: $oldFilePath to $backupFilePath")
        } else {
            Logger.warn("Old file not found for backup: $oldFilePath")
        }
    }
}
