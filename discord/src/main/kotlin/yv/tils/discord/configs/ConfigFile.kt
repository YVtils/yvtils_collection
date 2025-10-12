package yv.tils.discord.configs

import yv.tils.config.files.YMLFileUtils
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.utils.logger.Logger

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
    val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        val entries = mutableListOf<ConfigEntry>()

        if (content.isEmpty()) {
            entries.add(ConfigEntry("documentation", EntryType.STRING, null, "https://docs.yvtils.net/discord/config.yml", "Documentation URL"))
            entries.add(ConfigEntry("appToken", EntryType.STRING, null, "YOUR TOKEN HERE", "Discord bot token"))
            entries.add(ConfigEntry("mainGuild", EntryType.STRING, null, "GUILD ID", "Main guild ID"))
            entries.add(ConfigEntry("botSettings.onlineStatus", EntryType.STRING, null, "online", "Bot online status"))
            entries.add(ConfigEntry("botSettings.activity", EntryType.STRING, null, "none", "Bot activity"))
            entries.add(ConfigEntry("botSettings.activityMessage", EntryType.STRING, null, "Minecraft", "Bot activity message"))

            entries.add(ConfigEntry("general.settings.ignoreBotMessages", EntryType.BOOLEAN, null, true, "Ignore bot messages"))

            entries.add(ConfigEntry("whitelistFeature.channel", EntryType.STRING, null, "CHANNEL ID", "Whitelist channel"))
            entries.add(ConfigEntry("whitelistFeature.roles", EntryType.STRING, null, "ROLE ID 1, ROLE ID 2, ROLE ID ...", "Whitelist roles"))
            entries.add(ConfigEntry("whitelistFeature.settings.checkMinecraftAccount", EntryType.BOOLEAN, null, true, "Check minecraft accounts"))

            entries.add(ConfigEntry("commands.serverInfoCommand.permission", EntryType.STRING, null, "PERMISSION", "Server info permission"))
            entries.add(ConfigEntry("commands.whitelistCommand.permission", EntryType.STRING, null, "PERMISSION", "Whitelist command permission"))

            entries.add(ConfigEntry("syncFeature.chatSync.enabled", EntryType.BOOLEAN, null, true, "Chat sync enabled"))
            entries.add(ConfigEntry("syncFeature.chatSync.permission", EntryType.STRING, null, "PERMISSION", "Chat sync permission"))
            entries.add(ConfigEntry("syncFeature.chatSync.channel", EntryType.STRING, null, "CHANNEL ID", "Chat sync channel"))
            entries.add(ConfigEntry("syncFeature.chatSync.settings.syncMinecraftMessages", EntryType.BOOLEAN, null, true, "Sync MC messages"))
            entries.add(ConfigEntry("syncFeature.chatSync.settings.syncDiscordMessages", EntryType.BOOLEAN, null, true, "Sync Discord messages"))
            entries.add(ConfigEntry("syncFeature.chatSync.settings.syncAdvancements", EntryType.BOOLEAN, null, true, "Sync advancements"))
            entries.add(ConfigEntry("syncFeature.chatSync.settings.syncJoinLeaveMessages", EntryType.BOOLEAN, null, true, "Sync join/leave"))
            entries.add(ConfigEntry("syncFeature.chatSync.settings.syncDeaths", EntryType.BOOLEAN, null, true, "Sync deaths"))

            entries.add(ConfigEntry("syncFeature.consoleSync.enabled", EntryType.BOOLEAN, null, true, "Console sync enabled"))
            entries.add(ConfigEntry("syncFeature.consoleSync.channel", EntryType.STRING, null, "CHANNEL ID", "Console sync channel"))
            entries.add(ConfigEntry("syncFeature.consoleSync.settings.ignoreBotMessages", EntryType.BOOLEAN, null, true, "Ignore bot messages in console sync"))

            entries.add(ConfigEntry("syncFeature.serverStats.enabled", EntryType.BOOLEAN, null, true, "Server stats enabled"))
            entries.add(ConfigEntry("syncFeature.serverStats.mode", EntryType.STRING, null, "both", "Server stats mode"))
            entries.add(ConfigEntry("syncFeature.serverStats.channel", EntryType.STRING, null, "CHANNEL ID", "Server stats channel"))

            entries.add(ConfigEntry("syncFeature.serverStats.design.status.text", EntryType.STRING, null, "<emoji> | SERVER <status>", "Status text"))
            entries.add(ConfigEntry("syncFeature.serverStats.design.status.emoji.online", EntryType.STRING, null, "💚", "Online emoji"))
            entries.add(ConfigEntry("syncFeature.serverStats.design.status.emoji.offline", EntryType.STRING, null, "❤️", "Offline emoji"))
            entries.add(ConfigEntry("syncFeature.serverStats.design.status.emoji.maintenance", EntryType.STRING, null, "💛", "Maintenance emoji"))

            entries.add(ConfigEntry("syncFeature.serverStats.design.version.text", EntryType.STRING, null, "<emoji> | <version>", "Version text"))
            entries.add(ConfigEntry("syncFeature.serverStats.design.version.emoji", EntryType.STRING, null, "🛠️", "Version emoji"))

            entries.add(ConfigEntry("syncFeature.serverStats.design.players.text", EntryType.STRING, null, "<emoji> | <players> / <maxPlayers> Players", "Players text"))
            entries.add(ConfigEntry("syncFeature.serverStats.design.players.emoji", EntryType.STRING, null, "👥", "Players emoji"))

            entries.add(ConfigEntry("syncFeature.serverStats.design.lastRefresh.text", EntryType.STRING, null, "<emoji> | <time>", "Last refresh text"))
            entries.add(ConfigEntry("syncFeature.serverStats.design.lastRefresh.emoji", EntryType.STRING, null, "⌚", "Last refresh emoji"))

            entries.add(ConfigEntry("syncFeature.serverStats.settings.showServerStatus", EntryType.BOOLEAN, null, true, "Show server status"))
            entries.add(ConfigEntry("syncFeature.serverStats.settings.showServerVersion", EntryType.BOOLEAN, null, true, "Show server version"))
            entries.add(ConfigEntry("syncFeature.serverStats.settings.showServerPlayers", EntryType.BOOLEAN, null, true, "Show server players"))
            entries.add(ConfigEntry("syncFeature.serverStats.settings.showLastRefresh", EntryType.BOOLEAN, null, true, "Show last refresh"))
        }

        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries(filePath, entries)
        yv.tils.config.files.FileUtils.saveFile(filePath, ymlFile)
    }

}
