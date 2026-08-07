/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.server.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * server's `config.yml`, as a plain (nested) `data class` persisted via
 * `ObjectMapperFileUtils` - replaces the old `ConfigEntry`-list-built-by-hand version of this
 * file. No GUI is wired up yet - the `@ConfigDescription` annotations are already in place so
 * one can be added later with zero further changes to this file.
 */
data class ServerConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/server/config.yml",

    var motd: Motd = Motd(),
    var info: Info = Info(),
    var hoverMOTD: HoverMotd = HoverMotd(),
    var maintenance: Maintenance = Maintenance(),
) {
    data class Motd(
        @ConfigDescription("Enable MOTD")
        var enabled: Boolean = true,

        var entries: Entries = Entries(),
    ) {
        data class Entries(
            @ConfigDescription("Top MOTD entries")
            var top: List<String> = listOf(
                "Welcome to the server!",
                "This server is running YVtils v1.0.0",
                "Server version: <version>",
                "Online players: <onlinePlayers>",
                "Max players: <maxPlayers>",
                "Date: <date>",
            ),

            @ConfigDescription("Bottom MOTD entries")
            var bottom: List<String> = listOf(
                "Have fun!",
                "Enjoy your stay!",
                "See you next time!",
            ),
        )
    }

    data class Info(
        @ConfigDescription("Max players override")
        var maxPlayers: Int = 0,
    )

    data class HoverMotd(
        @ConfigDescription("Enable hover MOTD")
        var enabled: Boolean = true,

        @ConfigDescription("Hover entries")
        var entries: List<String> = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
    )

    data class Maintenance(
        @ConfigDescription("Maintenance mode")
        var enabled: Boolean = false,
    )
}
