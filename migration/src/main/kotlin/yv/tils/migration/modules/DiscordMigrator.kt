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

import yv.tils.configv2.files.ConfigFile
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
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
    val oldYaml = ConfigurateFileUtils.load(oldConfigPath, ConfigFormat.YAML, overwriteParentDir = true)

        // Transform structure to new format
        val newStructure = transformConfigStructure(oldYaml)

        // Save new config
    val newYamlFile = ConfigurateFileUtils.create(newConfigPath, newStructure, ConfigFormat.YAML)
    ConfigurateFileUtils.save(newYamlFile)

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
    val oldYaml = ConfigurateFileUtils.load(oldSavePath, ConfigFormat.YAML, overwriteParentDir = true)

        // Transform to new structure
        val saveEntries = transformSaveStructure(oldYaml)
        val saveWrapper = mapOf("saves" to saveEntries)

        // Save as JSON
    val jsonFile = JsonFileUtils.makeJsonFile(newSavePath, saveWrapper)
    ConfigurateFileUtils.save(jsonFile)

        Logger.info("Discord save migrated successfully (${saveEntries.size} entries)")
        return true
    }

    /** Transforms Discord config structure from old to new format */
    private fun transformConfigStructure(oldYaml: ConfigFile): Map<String, Any> {
        val newRoot = mutableMapOf<String, Any>()
        val yaml = oldYaml.node

        // 1. Direct key renames and basic values
        newRoot["documentation"] = "https://docs.yvtils.net/discord/config.yml"
        newRoot["appToken"] = yaml.node("botToken").string ?: "YOUR TOKEN HERE"
        newRoot["mainGuild"] = yaml.node("mainGuild").string ?: "GUILD ID"

        // 2. Bot settings (preserve existing)
        val botSettings = mutableMapOf<String, Any>()
        val oldBotSettings = yaml.node("botSettings")
        if (!oldBotSettings.virtual()) {
            botSettings["onlineStatus"] = oldBotSettings.node("onlineStatus").string ?: "online"
            botSettings["activity"] = oldBotSettings.node("activity").string ?: "PLAYING"
            botSettings["activityMessage"] =
                oldBotSettings.node("activityMessage").string ?: "Minecraft"
        }
        newRoot["botSettings"] = botSettings

//        // 3. Embed settings (preserve existing)
//        val embedSettings = mutableMapOf<String, Any>()
//        val oldEmbedSettings = yaml.node("embedSettings")
//        if (!oldEmbedSettings.virtual()) {
//            embedSettings["author"] = oldEmbedSettings.node("author").string ?: "Server"
//            embedSettings["authorIconURL"] = oldEmbedSettings.node("authorIconURL").string ?: "URL"
//        }
//        newRoot["embedSettings"] = embedSettings

        // 4. Commands section (restructured)
        val commands = mutableMapOf<String, Any>()

        // Whitelist command
        val whitelistCommand = mutableMapOf<String, Any>()
        val oldWhitelistCommand = yaml.node("whitelistCommand")
        if (!oldWhitelistCommand.virtual()) {
            whitelistCommand["permission"] =
                oldWhitelistCommand.node("permission").string ?: "PERMISSION"
        }
        commands["whitelistCommand"] = whitelistCommand

        // Server info command
        val serverInfoCommand = mutableMapOf<String, Any>()
        val oldServerInfoCommand = yaml.node("serverInfoCommand")
        if (!oldServerInfoCommand.virtual()) {
            serverInfoCommand["permission"] =
                oldServerInfoCommand.node("permission").string ?: "PERMISSION"
        }
        commands["serverInfoCommand"] = serverInfoCommand

        newRoot["commands"] = commands

        // 5. Whitelist feature (restructured)
        val whitelistFeature = mutableMapOf<String, Any>()
        val oldWhitelistFeature = yaml.node("whitelistFeature")
        if (!oldWhitelistFeature.virtual()) {
            whitelistFeature["roles"] =
                oldWhitelistFeature.node("role").string ?: "ROLE ID 1, ROLE ID 2, ROLE ID ..."
            whitelistFeature["channel"] = oldWhitelistFeature.node("channel").string ?: "CHANNEL ID"
        }
        newRoot["whitelistFeature"] = whitelistFeature

        // 6. Sync features (restructured)
        val syncFeature = mutableMapOf<String, Any>()

        // Chat sync
        val chatSync = mutableMapOf<String, Any>()
        val oldChatSync = yaml.node("chatSync")
        if (!oldChatSync.virtual()) {
            chatSync["enabled"] = oldChatSync.node("enabled").get(Boolean::class.javaObjectType) ?: true
            chatSync["permission"] = oldChatSync.node("permission").string ?: "PERMISSION"
            chatSync["channel"] = oldChatSync.node("channel").string ?: "CHANNEL ID"
        }
        syncFeature["chatSync"] = chatSync

        // Console sync
        val consoleSync = mutableMapOf<String, Any>()
        val oldConsoleSync = yaml.node("consoleSync")
        if (!oldConsoleSync.virtual()) {
            consoleSync["enabled"] = oldConsoleSync.node("enabled").get(Boolean::class.javaObjectType) ?: true
            consoleSync["channel"] = oldConsoleSync.node("channel").string ?: "CHANNEL ID"
        }
        syncFeature["consoleSync"] = consoleSync

        // Server stats (complex restructuring)
        val serverStats = mutableMapOf<String, Any>()
        val oldServerStats = yaml.node("serverStats")
        if (!oldServerStats.virtual()) {
            serverStats["enabled"] = oldServerStats.node("enabled").get(Boolean::class.javaObjectType) ?: true
            serverStats["mode"] = oldServerStats.node("mode").string ?: "description"
            serverStats["channel"] = oldServerStats.node("channel").string ?: "CHANNEL ID"

            // Transform layout structure
            val design = mutableMapOf<String, Any>()
            val oldLayout = oldServerStats.node("layout")
            if (!oldLayout.virtual()) {
                // Server status with emoji mapping
                val serverStatus = mutableMapOf<String, Any>()
                val oldServerStatus = oldLayout.node("serverStatus")
                if (!oldServerStatus.virtual()) {
                    serverStatus["text"] =
                        oldServerStatus.node("text").string ?: "<emoji> | SERVER <status>"

                    val emoji = mutableMapOf<String, Any>()
                    val oldEmoji = oldServerStatus.node("emoji")
                    if (!oldEmoji.virtual()) {
                        emoji["online"] = oldEmoji.node("online").string ?: "💚"
                        emoji["offline"] = oldEmoji.node("offline").string ?: "❤️"
                    }
                    serverStatus["emoji"] = emoji
                }
                design["status"] = serverStatus

                // Server version with emoji
                val version = mutableMapOf<String, Any>()
                val oldVersion = oldLayout.node("serverVersion").string
                if (oldVersion != null) {
                    version["text"] = oldVersion
                    version["emoji"] = "🛠️"
                }
                design["version"] = version

                // Server players with emoji
                val players = mutableMapOf<String, Any>()
                val oldPlayers = oldLayout.node("lastPlayerCount").string
                if (oldPlayers != null) {
                    players["text"] = oldPlayers.replace("<count>", "<players> / <maxPlayers>")
                    players["emoji"] = "👥"
                }
                design["players"] = players

                // Last refresh time with emoji
                val lastRefresh = mutableMapOf<String, Any>()
                val oldLastRefresh = oldLayout.node("lastRefreshed").string
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
//        newRoot["logChannel"] = yaml.node("logChannel").string ?: "CHANNEL ID"

        return newRoot
    }

    /** Transforms Discord save structure from YAML to JSON format */
    private fun transformSaveStructure(
        oldYaml: ConfigFile,
    ): List<Map<String, String>> {
        val entries = mutableListOf<Map<String, String>>()
        val yaml = oldYaml.node

        // Process each entry (Discord ID -> "username uuid")
        for ((key, child) in yaml.childrenMap()) {
            val keyStr = key.toString()
            if (keyStr == "documentation") continue

            val raw = child.string ?: continue
            val parts = raw.split(" ")

            if (parts.size >= 2) {
                val entry =
                    mapOf(
                        "discordID" to keyStr,
                        "playerName" to parts[0],
                        "playerUUID" to parts[1]
                    )
                entries.add(entry)
            } else {
                Logger.warn("Malformed save entry: $keyStr -> $raw")
            }
        }

        return entries
    }
}
