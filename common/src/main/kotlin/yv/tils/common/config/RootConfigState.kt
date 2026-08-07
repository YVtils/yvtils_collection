/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.common.config

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * The root `/config.yml`, as a plain (nested) `data class` persisted via
 * `ObjectMapperFileUtils` - replaces the old `ConfigEntry`-list-built-by-hand version of
 * this file.
 */
data class RootConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/config.yml",

    @ConfigDescription("Default language")
    var language: String = "en",

    @ConfigDescription("Server IP")
    var serverIP: String = "smp.net",

    @ConfigDescription("Server port")
    var serverPort: Int = -1,

    @ConfigDescription("Timezone")
    var timezone: String = "default",

    var updateCheck: UpdateCheck = UpdateCheck(),
    var debug: Debug = Debug(),
) {
    data class UpdateCheck(
        @ConfigDescription("Update check enabled")
        var enabled: Boolean = true,

        @ConfigDescription("Send updates to ops")
        var sendToOps: Boolean = true,
    )

    data class Debug(
        @ConfigDescription("Debug active")
        var active: Boolean = false,

        @ConfigDescription("Debug level")
        var level: Int = 3,
    )
}
