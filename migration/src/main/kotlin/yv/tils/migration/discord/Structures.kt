package yv.tils.migration.discord

class Structures {
    data class NewSaveFileStructure(
        val discordUserID: String = "",
        val minecraftName: String = "",
        val minecraftUUID: String = "",
    )

    data class NewConfigFileStructure(
        val documentation: String = "https://docs.yvtils.net/discord/config.yml",
        val appToken: String = "YOUR TOKEN HERE",
        val mainGuild: String = "GUILD ID",
        val botSettings_onlineStatus: String = "online",
        val botSettings_activity: String = "none",
        val botSettings_activityMessage: String = "Minecraft",
        val embedSettings_author: String = "YVtils SMP",
        val embedSettings_authorIconURL: String = "URL",
        val general_settings_ignoreBotMessages: Boolean = true,
        val whitelistFeature_channel: String = "CHANNEL ID",
        val whitelistFeature_roles: String = "ROLE ID 1, ROLE ID 2, ROLE ID ...",
        val whitelistFeature_settings_checkMinecraftAccount: Boolean = true,
        val command_serverInfoCommand_permission: String = "PERMISSION",
        val command_whitelistCommand_permission: String = "PERMISSION",
        val syncFeature_chatSync_enabled: Boolean = true,
        val syncFeature_chatSync_permission: String = "PERMISSION",
        val syncFeature_chatSync_channel: String = "CHANNEL ID",
        val syncFeature_chatSync_settings_syncMinecraftMessages: Boolean = true,
        val syncFeature_chatSync_settings_syncDiscordMessages: Boolean = true,
        val syncFeature_chatSync_settings_syncAdvancements: Boolean = true,
        val syncFeature_chatSync_settings_syncJoinLeaveMessages: Boolean = true,
        val syncFeature_consoleSync_enabled: Boolean = true,
        val syncFeature_consoleSync_channel: String = "CHANNEL ID",
        val syncFeature_consoleSync_settings_ignoreBotMessages: Boolean = true,
        val syncFeature_serverStats_enabled: Boolean = true,
        val syncFeature_serverStats_mode: String = "both",
        val syncFeature_serverStats_channel: String = "CHANNEL ID",
        val syncFeature_serverStats_design_status_text: String = "<emoji> | SERVER <status>",
        val syncFeature_serverStats_design_status_emoji_online: String = "\uD83D\uDC9A",
        val syncFeature_serverStats_design_status_emoji_offline: String = "\u2764\uFE0F",
        val syncFeature_serverStats_design_status_emoji_maintenance: String = "\uD83D\uDC9B",
        val syncFeature_serverStats_design_version_text: String = "<emoji> | <version>",
        val syncFeature_serverStats_design_version_emoji: String = "\uD83D\uDEE0\uFE0F",
        val syncFeature_serverStats_design_players_text: String = "<emoji> | <players> / <maxPlayers> Players",
        val syncFeature_serverStats_design_players_emoji: String = "\uD83D\uDC65",
        val syncFeature_serverStats_design_lastRefresh_text: String = "<emoji> | <time>",
        val syncFeature_serverStats_design_lastRefresh_emoji: String = "\u231A",
        val syncFeature_serverStats_settings_showServerStatus: Boolean = true,
        val syncFeature_serverStats_settings_showServerVersion: Boolean = true,
        val syncFeature_serverStats_settings_showServerPlayers: Boolean = true,
        val syncFeature_serverStats_settings_showLastRefresh: Boolean = true,
    )

    data class OldSaveFileStructure(
        val key: String,
        val value: OldSaveFileValueStructure,
    )

    data class OldSaveFileValueStructure(
        val name: String,
        val uuid: String,
    )

    data class OldConfigFileStructure(
        var botToken: String = "",
        var mainGuild: String = "",
        var botSettings_onlineStatus: String = "",
        var botSettings_activity: String = "",
        var botSettings_activityMessage: String = "",
        var embedSettings_author: String = "",
        var embedSettings_authorIconURL: String = "",
        var whitelistFeature_channel: String = "",
        var whitelistFeature_role: String = "",
        var serverInfoCommand_permission: String = "",
        var whitelistCommand_permission: String = "",
        var chatSync_enabled: Boolean = false,
        var chatSync_permission: String = "",
        var chatSync_channel: String = "",
        var consoleSync_enabled: Boolean = false,
        var consoleSync_channel: String = "",
        var serverStats_enabled: Boolean = false,
        var serverStats_mode: String = "",
        var serverStats_channel: String = "",
        var serverStats_layout_serverStatus_text: String = "",
        var serverStats_layout_serverStatus_emoji_online: String = "",
        var serverStats_layout_serverStatus_emoji_offline: String = "",
        var serverStats_layout_serverVersion: String = "",
        var serverStats_layout_lastPlayerCount: String = "",
        var serverStats_layout_lastRefreshed: String = "",
        var logChannel: String = "",
    )
}

// Extension function to map OldConfigFileStructure to NewConfigFileStructure
fun Structures.OldConfigFileStructure.toNewConfig(): Structures.NewConfigFileStructure =
    Structures.NewConfigFileStructure(
        appToken = this.botToken,
        mainGuild = this.mainGuild,
        botSettings_onlineStatus = this.botSettings_onlineStatus,
        botSettings_activity = this.botSettings_activity,
        botSettings_activityMessage = this.botSettings_activityMessage,
        embedSettings_author = this.embedSettings_author,
        embedSettings_authorIconURL = this.embedSettings_authorIconURL,
        whitelistFeature_channel = this.whitelistFeature_channel,
        whitelistFeature_roles = this.whitelistFeature_role,
        command_serverInfoCommand_permission = this.serverInfoCommand_permission,
        command_whitelistCommand_permission = this.whitelistCommand_permission,
        syncFeature_chatSync_enabled = this.chatSync_enabled,
        syncFeature_chatSync_permission = this.chatSync_permission,
        syncFeature_chatSync_channel = this.chatSync_channel,
        syncFeature_consoleSync_enabled = this.consoleSync_enabled,
        syncFeature_consoleSync_channel = this.consoleSync_channel,
        syncFeature_serverStats_enabled = this.serverStats_enabled,
        syncFeature_serverStats_mode = this.serverStats_mode,
        syncFeature_serverStats_channel = this.serverStats_channel,
        syncFeature_serverStats_design_status_text = this.serverStats_layout_serverStatus_text,
        syncFeature_serverStats_design_status_emoji_online = this.serverStats_layout_serverStatus_emoji_online,
        syncFeature_serverStats_design_status_emoji_offline = this.serverStats_layout_serverStatus_emoji_offline,
        syncFeature_serverStats_design_version_text = this.serverStats_layout_serverVersion,
        syncFeature_serverStats_design_players_text = this.serverStats_layout_lastPlayerCount,
        syncFeature_serverStats_design_lastRefresh_text = this.serverStats_layout_lastRefreshed
        // The rest will use defaults
    )

// Utility to convert a data class to a map (for YAML/JSON serialization)
inline fun <reified T: Any> T.toMap(): Map<String, Any?> =
    this::class.members
        .filter { it.parameters.size == 1 }
        .associate { it.name to it.call(this) }
