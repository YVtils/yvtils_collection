package yv.tils.migration.modules

import files.FileUtils
import logger.Logger
import yv.tils.migration.base.BaseMigrator
import java.io.File

/**
 * Discord module configuration migration
 *
 * Migrates Discord config from old YVtils-Discord plugin format to new unified YVtils structure
 * with updated keys and organization.
 */
class DiscordMigrator: BaseMigrator() {

    override val moduleName = "discord"

    private val oldConfigPath = "plugins/YVtils-Discord/discord/config.yml"
    private val newConfigPath = "/discord/config.yml"
    private val oldSavePath = "plugins/YVtils-Discord/discord/save.yml"
    private val newSavePath = "/discord/save.json"

    override fun performMigration(): Boolean {
        var configMigrated = false
        var saveMigrated = false

        // Migrate config file
        configMigrated = migrateConfigFile()

        // Migrate save file
        saveMigrated = migrateSaveFile()

        // Log results
        Logger.info("Discord migration summary:")
        Logger.info("  Config migrated: $configMigrated")
        Logger.info("  Save migrated: $saveMigrated")

        return configMigrated || saveMigrated
    }

    /** Migrates Discord config file from YAML to YAML with structure changes */
    private fun migrateConfigFile(): Boolean {
        if (! shouldMigrate(oldConfigPath, newConfigPath)) {
            return false
        }

        Logger.info("Migrating Discord config: $oldConfigPath → $newConfigPath")

        // Create backup
        val oldFile = File(oldConfigPath)
        createBackup(oldFile, "backup_config.yml")

        // Load old config using FileUtils
        val oldYaml = FileUtils.loadYAMLFile(oldConfigPath, true)

        // Transform structure to new format
        val newStructure = transformConfigStructure(oldYaml)

        // Save new config
        val newYamlFile = FileUtils.makeYAMLFile(newConfigPath, newStructure)
        FileUtils.saveFile(newConfigPath, newYamlFile)

        Logger.info("Discord config migrated successfully")
        return true
    }

    /** Migrates Discord save file from YAML to JSON with structure changes */
    private fun migrateSaveFile(): Boolean {
        if (! shouldMigrate(oldSavePath, newSavePath)) {
            return false
        }

        Logger.info("Migrating Discord save: $oldSavePath → $newSavePath")

        // Create backup
        val oldFile = File(oldSavePath)
        createBackup(oldFile, "backup_save.yml")

        // Load old save
        val oldYaml = FileUtils.loadYAMLFile(oldSavePath, true)

        // Transform to new structure
        val saveEntries = transformSaveStructure(oldYaml)
        val saveWrapper = mapOf("saves" to saveEntries)

        // Save as JSON
        val jsonFile = FileUtils.makeJSONFile(newSavePath, saveWrapper)
        FileUtils.saveFile(newSavePath, jsonFile)

        Logger.info("Discord save migrated successfully (${saveEntries.size} entries)")
        return true
    }

    /** Transforms Discord config structure from old to new format */
    private fun transformConfigStructure(oldYaml: FileUtils.Companion.YAMLFile): Map<String, Any> {
        val newRoot = mutableMapOf<String, Any>()
        val yaml = oldYaml.content

        // 1. Direct key renames and basic values
        newRoot["documentation"] = "https://docs.yvtils.net/discord/config.yml"
        newRoot["appToken"] = yaml.getString("botToken") ?: "YOUR TOKEN HERE"
        newRoot["mainGuild"] = yaml.getString("mainGuild") ?: "GUILD ID"

        // 2. Bot settings (preserve existing)
        val botSettings = mutableMapOf<String, Any>()
        val oldBotSettings = yaml.getConfigurationSection("botSettings")
        if (oldBotSettings != null) {
            botSettings["onlineStatus"] = oldBotSettings.getString("onlineStatus") ?: "online"
            botSettings["activity"] = oldBotSettings.getString("activity") ?: "PLAYING"
            botSettings["activityMessage"] =
                oldBotSettings.getString("activityMessage") ?: "Minecraft"
        }
        newRoot["botSettings"] = botSettings

        // 3. Embed settings (preserve existing)
        val embedSettings = mutableMapOf<String, Any>()
        val oldEmbedSettings = yaml.getConfigurationSection("embedSettings")
        if (oldEmbedSettings != null) {
            embedSettings["author"] = oldEmbedSettings.getString("author") ?: "Server"
            embedSettings["authorIconURL"] = oldEmbedSettings.getString("authorIconURL") ?: "URL"
        }
        newRoot["embedSettings"] = embedSettings

        // 4. Commands section (restructured)
        val commands = mutableMapOf<String, Any>()

        // Whitelist feature and command
        val whitelistFeature = mutableMapOf<String, Any>()
        val oldWhitelistFeature = yaml.getConfigurationSection("whitelistFeature")
        if (oldWhitelistFeature != null) {
            whitelistFeature["role"] =
                oldWhitelistFeature.getString("role") ?: "ROLE ID 1, ROLE ID 2, ROLE ID ..."
            whitelistFeature["channel"] = oldWhitelistFeature.getString("channel") ?: "CHANNEL ID"
        }

        val whitelistCommand = mutableMapOf<String, Any>()
        val oldWhitelistCommand = yaml.getConfigurationSection("whitelistCommand")
        if (oldWhitelistCommand != null) {
            whitelistCommand["permission"] =
                oldWhitelistCommand.getString("permission") ?: "PERMISSION"
        }

        commands["whitelist"] = mapOf("feature" to whitelistFeature, "command" to whitelistCommand)

        // Server info command
        val serverInfoCommand = mutableMapOf<String, Any>()
        val oldServerInfoCommand = yaml.getConfigurationSection("serverInfoCommand")
        if (oldServerInfoCommand != null) {
            serverInfoCommand["permission"] =
                oldServerInfoCommand.getString("permission") ?: "PERMISSION"
        }
        commands["serverInfo"] = serverInfoCommand

        newRoot["commands"] = commands

        // 5. Sync features (restructured)
        val syncFeature = mutableMapOf<String, Any>()

        // Chat sync
        val chatSync = mutableMapOf<String, Any>()
        val oldChatSync = yaml.getConfigurationSection("chatSync")
        if (oldChatSync != null) {
            chatSync["enabled"] = oldChatSync.getBoolean("enabled", true)
            chatSync["permission"] = oldChatSync.getString("permission") ?: "PERMISSION"
            chatSync["channel"] = oldChatSync.getString("channel") ?: "CHANNEL ID"
        }
        syncFeature["chatSync"] = chatSync

        // Console sync
        val consoleSync = mutableMapOf<String, Any>()
        val oldConsoleSync = yaml.getConfigurationSection("consoleSync")
        if (oldConsoleSync != null) {
            consoleSync["enabled"] = oldConsoleSync.getBoolean("enabled", true)
            consoleSync["channel"] = oldConsoleSync.getString("channel") ?: "CHANNEL ID"
        }
        syncFeature["consoleSync"] = consoleSync

        // Server stats (complex restructuring)
        val serverStats = mutableMapOf<String, Any>()
        val oldServerStats = yaml.getConfigurationSection("serverStats")
        if (oldServerStats != null) {
            serverStats["enabled"] = oldServerStats.getBoolean("enabled", true)
            serverStats["mode"] = oldServerStats.getString("mode") ?: "description"
            serverStats["channel"] = oldServerStats.getString("channel") ?: "CHANNEL ID"

            // Transform layout structure
            val layout = mutableMapOf<String, Any>()
            val oldLayout = oldServerStats.getConfigurationSection("layout")
            if (oldLayout != null) {
                // Server status with emoji mapping
                val serverStatus = mutableMapOf<String, Any>()
                val oldServerStatus = oldLayout.getConfigurationSection("serverStatus")
                if (oldServerStatus != null) {
                    serverStatus["text"] =
                        oldServerStatus.getString("text") ?: "<emoji> | SERVER <status>"

                    val emoji = mutableMapOf<String, Any>()
                    val oldEmoji = oldServerStatus.getConfigurationSection("emoji")
                    if (oldEmoji != null) {
                        emoji["online"] = oldEmoji.getString("online") ?: "💚"
                        emoji["offline"] = oldEmoji.getString("offline") ?: "❤️"
                    }
                    serverStatus["emoji"] = emoji
                }
                layout["serverStatus"] = serverStatus

                // Other layout items
                layout["serverVersion"] =
                    oldLayout.getString("serverVersion") ?: "💻 | VERSION <version>"
                layout["lastPlayerCount"] =
                    oldLayout.getString("lastPlayerCount") ?: "🎮 | PLAYERS <count>"
                layout["lastRefreshed"] = oldLayout.getString("lastRefreshed") ?: "⌚ | <time>"
            }
            serverStats["layout"] = layout
        }
        syncFeature["serverStats"] = serverStats

        newRoot["syncFeature"] = syncFeature

        // 6. Log channel (direct copy)
        newRoot["logChannel"] = yaml.getString("logChannel") ?: "CHANNEL ID"

        return newRoot
    }

    /** Transforms Discord save structure from YAML to JSON format */
    private fun transformSaveStructure(
        oldYaml: FileUtils.Companion.YAMLFile,
    ): List<Map<String, String>> {
        val entries = mutableListOf<Map<String, String>>()
        val yaml = oldYaml.content

        // Process each entry (Discord ID -> "username uuid")
        for (key in yaml.getKeys(false)) {
            if (key == "documentation") continue

            val raw = yaml.getString(key) ?: continue
            val parts = raw.split(" ")

            if (parts.size >= 2) {
                val entry =
                    mapOf(
                        "discordID" to key,
                        "playerName" to parts[0],
                        "playerUUID" to parts[1]
                    )
                entries.add(entry)
            } else {
                Logger.warn("Malformed save entry: $key -> $raw")
            }
        }

        return entries
    }
}
