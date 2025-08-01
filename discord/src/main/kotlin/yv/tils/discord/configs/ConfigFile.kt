package yv.tils.discord.configs

import files.FileUtils
import logger.Logger

// TODO: Add option to allow multiple whitelist channels
class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()

        fun getValue(key: String): Any? {
            return config[key]
        }

        fun getValueAsString(key: String): String? {
            return config[key]?.toString()
        }

        fun getValueAsInt(key: String): Int? {
            return config[key]?.toString()?.toIntOrNull()
        }

        fun getValueAsBoolean(key: String): Boolean? {
            return config[key]?.toString()?.toBoolean()
        }
    }

    private val filePath = "/discord/config.yml"

    fun loadConfig() {
        val file = FileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        if (content.isEmpty()) {
            content["documentation"] = "https://docs.yvtils.net/discord/config.yml"
            content["appToken"] = "YOUR TOKEN HERE"
            content["mainGuild"] = "GUILD ID"
            content["botSettings.onlineStatus"] = "online"
            content["botSettings.activity"] = "none"
            content["botSettings.activityMessage"] = "Minecraft"

            content["general.settings.ignoreBotMessages"] = true

            content["whitelistFeature.channel"] = "CHANNEL ID"
            content["whitelistFeature.roles"] = "ROLE ID 1, ROLE ID 2, ROLE ID ..."
            content["whitelistFeature.settings.checkMinecraftAccount"] = true

            content["command.serverInfoCommand.permission"] = "PERMISSION"
            content["command.whitelistCommand.permission"] = "PERMISSION"

            content["syncFeature.chatSync.enabled"] = true
            content["syncFeature.chatSync.permission"] = "PERMISSION"
            content["syncFeature.chatSync.channel"] = "CHANNEL ID"
            content["syncFeature.chatSync.settings.syncMinecraftMessages"] = true
            content["syncFeature.chatSync.settings.syncDiscordMessages"] = true
            content["syncFeature.chatSync.settings.syncAdvancements"] = true
            content["syncFeature.chatSync.settings.syncJoinLeaveMessages"] = true
            content["syncFeature.chatSync.settings.syncDeaths"] = true

            content["syncFeature.consoleSync.enabled"] = true
            content["syncFeature.consoleSync.channel"] = "CHANNEL ID"
            content["syncFeature.consoleSync.settings.ignoreBotMessages"] = true

            content["syncFeature.serverStats.enabled"] = true
            content["syncFeature.serverStats.mode"] = "both" // "both", "discord", "minecraft"
            content["syncFeature.serverStats.channel"] = "CHANNEL ID"

            // Server Status
            content["syncFeature.serverStats.design.status.text"] = "<emoji> | SERVER <status>"
            content["syncFeature.serverStats.design.status.emoji.online"] = "💚"
            content["syncFeature.serverStats.design.status.emoji.offline"] = "❤️"
            content["syncFeature.serverStats.design.status.emoji.maintenance"] = "💛"

            // Server Version
            content["syncFeature.serverStats.design.version.text"] = "<emoji> | <version>"
            content["syncFeature.serverStats.design.version.emoji"] = "🛠️"

            // Server Players
            content["syncFeature.serverStats.design.players.text"] = "<emoji> | <players> / <maxPlayers> Players"
            content["syncFeature.serverStats.design.players.emoji"] = "👥"

            // Last Refresh
            content["syncFeature.serverStats.design.lastRefresh.text"] = "<emoji> | <time>"
            content["syncFeature.serverStats.design.lastRefresh.emoji"] = "⌚"

            content["syncFeature.serverStats.settings.showServerStatus"] = true
            content["syncFeature.serverStats.settings.showServerVersion"] = true
            content["syncFeature.serverStats.settings.showServerPlayers"] = true
            content["syncFeature.serverStats.settings.showLastRefresh"] = true
        }

        val ymlFile = FileUtils.makeYAMLFile(filePath, content)
        FileUtils.saveFile(filePath, ymlFile)
    }

}
