package yv.tils.migration.discord

import data.Data
import files.FileUtils
import files.FileUtils.Companion.JSONFile
import kotlinx.serialization.Serializable
import logger.Logger
import org.bukkit.configuration.file.YamlConfiguration
import yv.tils.migration.MigrationYVtils
import java.io.File

@Suppress("Deprecation")
object SaveMigrator {
    private const val FOLDER_NAME = "discord"
    private const val OLD_NAME = "save.yml"
    private const val NEW_NAME = "save.json"
    private const val BACKUP_NAME = "backup_$OLD_NAME"

    fun migrateIfNeeded() {
        val dataFolder = MigrationYVtils.oldPluginFolder
        val newDataFolder = Data.pluginFolder
        val oldFile = File(File(dataFolder, FOLDER_NAME), OLD_NAME)
        val newFile = File(File(newDataFolder, FOLDER_NAME), NEW_NAME)

        if (! oldFile.exists() || newFile.exists()) {
            return
        }

        val oldYaml: YamlConfiguration = try {
            FileUtils.loadYAMLFile(oldFile.path, true).content
        } catch (ex: Exception) {
            Logger.error("Unable to load $OLD_NAME – migration skipped due to: ${ex.message}")
            return
        }

        /*───────────────────────────────────────────────────────────*
         * 1. Back‑up save.yml before we alter anything.
         *───────────────────────────────────────────────────────────*/
        val backupFile = File(File(newDataFolder, FOLDER_NAME), BACKUP_NAME)
        if (! oldFile.renameTo(backupFile)) {
            Logger.error("Failed to rename $OLD_NAME  ➜  $BACKUP_NAME – migration aborted")
            return
        }
        Logger.info("Legacy saves backed up as $BACKUP_NAME")

        /*───────────────────────────────────────────────────────────*
         * 2. Convert to list of SaveEntry.
         *───────────────────────────────────────────────────────────*/
        val entries: List<SaveEntry> = buildList {
            oldYaml.getKeys(false).forEach { key ->
                val raw = oldYaml.getString(key) ?: return@forEach
                val parts = raw.split(' ')
                if (parts.size < 2) return@forEach  // malformed line
                val name = parts[0]
                val uuid = parts[1]
                add(SaveEntry(discordUserID = key, minecraftName = name, minecraftUUID = uuid))
            }
        }

        val wrapper = SavesWrapper(saves = entries)

        /*───────────────────────────────────────────────────────────*
         * 3. Persist via FileUtils → save.json
         *───────────────────────────────────────────────────────────*/
        val jsonFile: JSONFile = FileUtils.makeJSONFile(NEW_NAME, wrapper)
        FileUtils.saveFile("$FOLDER_NAME/$NEW_NAME", jsonFile)

        Logger.info("save.yml migrated to $NEW_NAME (${entries.size} entries)")
    }

    @Serializable
    private data class SavesWrapper(val saves: List<SaveEntry>)

    @Serializable
    private data class SaveEntry(
        val discordUserID: String,
        val minecraftName: String,
        val minecraftUUID: String,
    )
}
