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

package yv.tils.migration.modules

import yv.tils.config.files.YMLFileUtils
import yv.tils.config.files.JSONFileUtils
import yv.tils.migration.base.BaseMigrator
import yv.tils.utils.logger.Logger
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
        // Migrate config file
        val configMigrated = migrateConfigFile()

        // Migrate save file
        val saveMigrated = migrateSaveFile()

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
    val oldYaml = YMLFileUtils.loadYAMLFile(oldConfigPath, true)

        // Transform structure to new format
        val newStructure = transformConfigStructure(oldYaml)

        // Save new config
    val newYamlFile = YMLFileUtils.makeYAMLFile(newConfigPath, newStructure)
    yv.tils.config.files.FileUtils.saveFile(newConfigPath, newYamlFile)

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
    val oldYaml = YMLFileUtils.loadYAMLFile(oldSavePath, true)

        // Transform to new structure
        val saveEntries = transformSaveStructure(oldYaml)
        val saveWrapper = mapOf("saves" to saveEntries)

        // Save as JSON
    val jsonFile = JSONFileUtils.makeJSONFile(newSavePath, saveWrapper)
    yv.tils.config.files.FileUtils.saveFile(newSavePath, jsonFile)

        Logger.info("Discord save migrated successfully (${saveEntries.size} entries)")
        return true
    }

    /** Transforms Discord config structure from old to new format */
    private fun transformConfigStructure(oldYaml: YMLFileUtils.Companion.YAMLFile): Map<String, Any> {
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

//        // 3. Embed settings (preserve existing)
//        val embedSettings = mutableMapOf<String, Any>()
//        val oldEmbedSettings = yaml.getConfigurationSection("embedSettings")
//        if (oldEmbedSettings != null) {
//            embedSettings["author"] = oldEmbedSettings.getString("author") ?: "Server"
//            embedSettings["authorIconURL"] = oldEmbedSettings.getString("authorIconURL") ?: "URL"
//        }
//        newRoot["embedSettings"] = embedSettings

        // 4. Commands section (restructured)
        val commands = mutableMapOf<String, Any>()

        // Whitelist command
        val whitelistCommand = mutableMapOf<String, Any>()
        val oldWhitelistCommand = yaml.getConfigurationSection("whitelistCommand")
        if (oldWhitelistCommand != null) {
            whitelistCommand["permission"] =
                oldWhitelistCommand.getString("permission") ?: "PERMISSION"
        }
        commands["whitelistCommand"] = whitelistCommand

        // Server info command
        val serverInfoCommand = mutableMapOf<String, Any>()
        val oldServerInfoCommand = yaml.getConfigurationSection("serverInfoCommand")
        if (oldServerInfoCommand != null) {
            serverInfoCommand["permission"] =
                oldServerInfoCommand.getString("permission") ?: "PERMISSION"
        }
        commands["serverInfoCommand"] = serverInfoCommand

        newRoot["commands"] = commands

        // 5. Whitelist feature (restructured)
        val whitelistFeature = mutableMapOf<String, Any>()
        val oldWhitelistFeature = yaml.getConfigurationSection("whitelistFeature")
        if (oldWhitelistFeature != null) {
            whitelistFeature["roles"] =
                oldWhitelistFeature.getString("role") ?: "ROLE ID 1, ROLE ID 2, ROLE ID ..."
            whitelistFeature["channel"] = oldWhitelistFeature.getString("channel") ?: "CHANNEL ID"
        }
        newRoot["whitelistFeature"] = whitelistFeature

        // 6. Sync features (restructured)
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
            val design = mutableMapOf<String, Any>()
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
                design["status"] = serverStatus

                // Server version with emoji
                val version = mutableMapOf<String, Any>()
                val oldVersion = oldLayout.getString("serverVersion")
                if (oldVersion != null) {
                    version["text"] = oldVersion
                    version["emoji"] = "🛠️"
                }
                design["version"] = version

                // Server players with emoji
                val players = mutableMapOf<String, Any>()
                val oldPlayers = oldLayout.getString("lastPlayerCount")
                if (oldPlayers != null) {
                    players["text"] = oldPlayers.replace("<count>", "<players> / <maxPlayers>")
                    players["emoji"] = "👥"
                }
                design["players"] = players

                // Last refresh time with emoji
                val lastRefresh = mutableMapOf<String, Any>()
                val oldLastRefresh = oldLayout.getString("lastRefreshed")
                if (oldLastRefresh != null) {
                    lastRefresh["text"] = oldLastRefresh
                    lastRefresh["emoji"] = "⌚"
                }
                design["lastRefresh"] = lastRefresh
            }
            serverStats["design"] = design
        }
        syncFeature["serverStats"] = serverStats

        newRoot["syncFeature"] = syncFeature

//        // 6. Log channel (direct copy)
//        newRoot["logChannel"] = yaml.getString("logChannel") ?: "CHANNEL ID"

        return newRoot
    }

    /** Transforms Discord save structure from YAML to JSON format */
    private fun transformSaveStructure(
        oldYaml: YMLFileUtils.Companion.YAMLFile,
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
