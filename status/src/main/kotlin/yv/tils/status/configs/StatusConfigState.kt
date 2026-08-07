/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.status.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * status' `config.yml`, as a plain `data class` persisted via `ObjectMapperFileUtils` -
 * replaces the old `ConfigEntry`-list-built-by-hand version of this file. No GUI is wired up
 * yet - the `@ConfigDescription` annotations are already in place so one can be added later
 * with zero further changes to this file.
 */
data class StatusConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/status/config.yml",

    @ConfigDescription("Display format")
    var display: String = "<dark_gray>[<white><status><dark_gray>] |<white> <playerName>",

    @ConfigDescription("Maximum display length")
    var maxLength: Int = 20,

    @ConfigDescription("Default statuses")
    var defaultStatus: List<String> = listOf("<green>Online", "<yellow>Away", "<red>Busy"),

    @ConfigDescription("Blacklisted statuses")
    var blacklist: List<String> = emptyList(),
)
