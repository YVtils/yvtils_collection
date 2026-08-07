/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.discord.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * discord's `config.yml`, as a plain (nested) `data class` persisted via
 * `ObjectMapperFileUtils` - replaces the old `ConfigEntry`-list-built-by-hand version of
 * this file. Nesting here mirrors the dotted keys the old version used
 * (`"botSettings.onlineStatus"` etc.) as real nested objects instead of dot-path strings.
 *
 * No GUI is wired up for this module yet (see `DataClassConfigGui`/multiMine's `ManageGUI`
 * for the reference implementation) - the `@ConfigDescription` annotations are already in
 * place so one can be added later with zero further changes to this file.
 */
data class DiscordConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/discord/config.yml",

    @ConfigDescription("Discord bot token")
    var appToken: String = "YOUR TOKEN HERE",

    @ConfigDescription("Main guild ID")
    var mainGuild: String = "GUILD ID",

    var botSettings: BotSettings = BotSettings(),
    var general: General = General(),
    var whitelistFeature: WhitelistFeature = WhitelistFeature(),
    var commands: Commands = Commands(),
    var syncFeature: SyncFeature = SyncFeature(),
) {
    data class BotSettings(
        @ConfigDescription("Bot online status")
        var onlineStatus: String = "online",

        @ConfigDescription("Bot activity")
        var activity: String = "none",

        @ConfigDescription("Bot activity message")
        var activityMessage: String = "Minecraft",
    )

    data class General(
        var settings: Settings = Settings(),
    ) {
        data class Settings(
            @ConfigDescription("Ignore bot messages")
            var ignoreBotMessages: Boolean = true,
        )
    }

    data class WhitelistFeature(
        @ConfigDescription("Whitelist channel")
        var channel: String = "CHANNEL ID",

        @ConfigDescription("Whitelist roles")
        var roles: String = "ROLE ID 1, ROLE ID 2, ROLE ID ...",

        var settings: Settings = Settings(),
    ) {
        data class Settings(
            @ConfigDescription("Check minecraft accounts")
            var checkMinecraftAccount: Boolean = true,
        )
    }

    data class Commands(
        var serverInfoCommand: PermissionHolder = PermissionHolder(),
        var whitelistCommand: PermissionHolder = PermissionHolder(),
    ) {
        data class PermissionHolder(
            @ConfigDescription("Permission required to use this command")
            var permission: String = "PERMISSION",
        )
    }

    data class SyncFeature(
        var chatSync: ChatSync = ChatSync(),
        var consoleSync: ConsoleSync = ConsoleSync(),
        var serverStats: ServerStats = ServerStats(),
    ) {
        data class ChatSync(
            @ConfigDescription("Chat sync enabled")
            var enabled: Boolean = true,

            @ConfigDescription("Chat sync permission")
            var permission: String = "PERMISSION",

            @ConfigDescription("Chat sync channel")
            var channel: String = "CHANNEL ID",

            var embedIcon: EmbedIcon = EmbedIcon(),
            var settings: Settings = Settings(),
        ) {
            data class EmbedIcon(
                @ConfigDescription(
                    "Set the maximum number of custom emojis the Discord app can upload. Discord " +
                        "allows up to 2000; the default of 1800 leaves room for server and user emoji usage."
                )
                var limit: Int = 1800,
            )

            data class Settings(
                @ConfigDescription("Sync MC messages")
                var syncMinecraftMessages: Boolean = true,

                @ConfigDescription("Sync Discord messages")
                var syncDiscordMessages: Boolean = true,

                @ConfigDescription("Sync advancements")
                var syncAdvancements: Boolean = true,

                @ConfigDescription("Sync join/leave")
                var syncJoinLeaveMessages: Boolean = true,

                @ConfigDescription("Sync deaths")
                var syncDeaths: Boolean = true,
            )
        }

        data class ConsoleSync(
            @ConfigDescription("Console sync enabled")
            var enabled: Boolean = true,

            @ConfigDescription("Console sync channel")
            var channel: String = "CHANNEL ID",

            var settings: Settings = Settings(),
        ) {
            data class Settings(
                @ConfigDescription("Ignore bot messages in console sync")
                var ignoreBotMessages: Boolean = true,
            )
        }

        data class ServerStats(
            @ConfigDescription("Server stats enabled")
            var enabled: Boolean = true,

            @ConfigDescription("Server stats mode")
            var mode: String = "both",

            @ConfigDescription("Server stats channel")
            var channel: String = "CHANNEL ID",

            var design: Design = Design(),
            var settings: Settings = Settings(),
        ) {
            data class Design(
                var status: StatusDesign = StatusDesign(),
                var version: VersionDesign = VersionDesign(),
                var players: PlayersDesign = PlayersDesign(),
                var lastRefresh: LastRefreshDesign = LastRefreshDesign(),
            ) {
                data class StatusDesign(
                    @ConfigDescription("Status text")
                    var text: String = "<emoji> | SERVER <status>",
                    var emoji: Emoji = Emoji(),
                ) {
                    data class Emoji(
                        @ConfigDescription("Online emoji")
                        var online: String = "\uD83D\uDC9A",

                        @ConfigDescription("Offline emoji")
                        var offline: String = "\u2764\uFE0F",

                        @ConfigDescription("Maintenance emoji")
                        var maintenance: String = "\uD83D\uDC9B",
                    )
                }

                data class VersionDesign(
                    @ConfigDescription("Version text")
                    var text: String = "<emoji> | <version>",

                    @ConfigDescription("Version emoji")
                    var emoji: String = "\uD83D\uDEE0\uFE0F",
                )

                data class PlayersDesign(
                    @ConfigDescription("Players text")
                    var text: String = "<emoji> | <players> / <maxPlayers> Players",

                    @ConfigDescription("Players emoji")
                    var emoji: String = "\uD83D\uDC65",
                )

                data class LastRefreshDesign(
                    @ConfigDescription("Last refresh text")
                    var text: String = "<emoji> | <time>",

                    @ConfigDescription("Last refresh emoji")
                    var emoji: String = "\u231A",
                )
            }

            data class Settings(
                @ConfigDescription("Show server status")
                var showServerStatus: Boolean = true,

                @ConfigDescription("Show server version")
                var showServerVersion: Boolean = true,

                @ConfigDescription("Show server players")
                var showServerPlayers: Boolean = true,

                @ConfigDescription("Show last refresh")
                var showLastRefresh: Boolean = true,
            )
        }
    }
}
