package yv.tils.migration.discord

import data.Data
import files.FileUtils
import files.FileUtils.Companion.YAMLFile
import logger.Logger
import org.bukkit.configuration.file.YamlConfiguration
import yv.tils.migration.MigrationYVtils
import java.io.File

// TODO: Fix syncFeature.serverStats config entries.

@Suppress("Deprecation")
object ConfigMigrator {
    private const val FOLDER_NAME = "discord"
    private const val ORIGINAL_NAME = "config.yml"
    private const val BACKUP_NAME = "backup_config.yml"

    /**
     * Entrypoint that the plugin calls.  It is intentionally silent when no
     * migration is required so your start‑up log stays tidy.
     */
    fun migrateIfNeeded() {
        val dataFolder = MigrationYVtils.oldPluginFolder
        val newDataFolder = Data.pluginFolder
        val oldFile = File(File(dataFolder, FOLDER_NAME), ORIGINAL_NAME)
        File(File(newDataFolder, FOLDER_NAME), ORIGINAL_NAME)

        // Nothing to do if config.yml is missing
        if (! oldFile.exists()) {
            Logger.debug("$ORIGINAL_NAME not found - no migration needed")
            return
        }

        val oldYaml: YamlConfiguration = try {
            FileUtils.loadYAMLFile(oldFile.path, true).content
        } catch (ex: Exception) {
            Logger.error("Unable to load $ORIGINAL_NAME – migration skipped: ${ex.message}")
            return
        }

        // Bail out if we already see a marker that only exists in the new spec
        if (isAlreadyNewFormat(oldYaml)) return

        /*───────────────────────────────────────────────────────────*
         *  1. Back‑up the original before we write anything.
         *───────────────────────────────────────────────────────────*/
        val backupFile = File(File(newDataFolder, FOLDER_NAME), BACKUP_NAME)
        try {
            if (backupFile.exists() && ! backupFile.delete()) {
                Logger.error("Failed to remove existing backup file - migration aborted")
                return
            }
            if (! oldFile.renameTo(backupFile)) {
                Logger.error("Failed to rename $ORIGINAL_NAME ➜ $BACKUP_NAME – migration aborted")
                return
            }
            Logger.info("Old configuration backed up as $BACKUP_NAME")
        } catch (ex: Exception) {
            Logger.error("Failed to create backup: ${ex.message}")
            return
        }

        /*───────────────────────────────────────────────────────────*
         *  2. Transform its contents → Map<String, Any?>
         *───────────────────────────────────────────────────────────*/
        val newStructure: Map<String, Any?> = convertOldToNew(oldYaml)

        /*───────────────────────────────────────────────────────────*
         *  3. Persist via FileUtils so we respect your existing file API
         *     (creates a new config.yml because the old one has been renamed).
         *───────────────────────────────────────────────────────────*/
        val newYamlFile: YAMLFile =
            FileUtils.makeYAMLFile(ORIGINAL_NAME, newStructure.filterValues { it != null } as Map<String, Any>)
        FileUtils.saveFile("$FOLDER_NAME/$ORIGINAL_NAME", newYamlFile)

        Logger.info("Config file migrated to new format (see $BACKUP_NAME for backup)")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Quick heuristic: the *new* format always contains the top‑level key
     * "general".  You can swap this out for a more elaborate check at any time.
     */
    private fun isAlreadyNewFormat(yaml: YamlConfiguration): Boolean =
        yaml.contains("general")

    /**
     * Converts the old configuration format to the new structure.
     * @param old The old configuration to convert
     * @return A map containing the new configuration structure
     */
    private fun convertOldToNew(old: YamlConfiguration): Map<String, Any?> {
        val newRoot = linkedMapOf<String, Any?>()

        // 1. straight copies or simple renames
        listOf("documentation", "mainGuild", "embedSettings").forEach { key ->
            old.get(key)?.let { newRoot[key] = it }
        }
        old.getString("botToken")?.let { newRoot["appToken"] = it }

        // 2. botSettings – lower‑case activity for style consistency
        old.getConfigurationSection("botSettings")?.let { section ->
            val map = section.getValues(true).toMutableMap()
            (map["activity"] as? String)?.let { map["activity"] = it.lowercase() }
            newRoot["botSettings"] = map
        }

        // 3. whitelistFeature – role ➜ roles
        old.getConfigurationSection("whitelistFeature")?.let { section ->
            val target = linkedMapOf<String, Any?>()
            section.getKeys(false).forEach { k ->
                when (k) {
                    "role" -> target["roles"] = section.get(k)
                    else -> section.get(k)?.let { target[k] = it }
                }
            }
            newRoot["whitelistFeature"] = target
        }

        // 4. command group
        val command = linkedMapOf<String, Any?>()
        listOf("serverInfoCommand", "whitelistCommand").forEach { key ->
            if (old.contains(key)) command[key] = old[key]
        }
        if (command.isNotEmpty()) newRoot["command"] = command

        // 5. syncFeature group
        val sync = linkedMapOf<String, Any?>()
        listOf("chatSync", "consoleSync").forEach { key ->
            if (old.contains(key)) sync[key] = old[key]
        }
        if (old.isConfigurationSection("serverStats")) {
            val statsSec = old.getConfigurationSection("serverStats") !!
            val stats = linkedMapOf<String, Any?>()
            statsSec.getKeys(true).forEach { k ->
                when (k) {
                    "layout" -> stats["design"] = statsSec[k] // rename
                    else -> stats[k] = statsSec[k]
                }
            }
            sync["serverStats"] = stats
        }
        if (sync.isNotEmpty()) newRoot["syncFeature"] = sync

        // 6. General defaults that did not exist before
        newRoot["general"] = mapOf(
            "settings" to mapOf(
                "ignoreBotMessages" to true
            )
        )

        Logger.dev("New config structure:")
        newRoot.forEach { (key, value) ->
            Logger.dev("  $key: $value")
        }

        return newRoot
    }
}
