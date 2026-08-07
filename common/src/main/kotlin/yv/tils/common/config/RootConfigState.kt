/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.common.config

import org.bukkit.Material
import yv.tils.configv2.data.annotations.BooleanIcon
import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.ConfigIcon
import yv.tils.configv2.data.annotations.DefaultValue
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
    @DefaultValue("en")
    @ConfigIcon(Material.WRITABLE_BOOK)
    var language: String = "en",

    @ConfigDescription("Server IP")
    @DefaultValue("smp.net")
    @ConfigIcon(Material.COMPASS)
    var serverIP: String = "smp.net",

    @ConfigDescription("Server port")
    @DefaultValue("-1")
    @ConfigIcon(Material.REDSTONE)
    var serverPort: Int = -1,

    @ConfigDescription("Timezone")
    @DefaultValue("default")
    @ConfigIcon(Material.CLOCK)
    var timezone: String = "default",

    @ConfigIcon(Material.RECOVERY_COMPASS)
    var updateCheck: UpdateCheck = UpdateCheck(),

    @ConfigIcon(Material.COMMAND_BLOCK)
    var debug: Debug = Debug(),
) {
    data class UpdateCheck(
        @ConfigDescription("Update check enabled")
        @DefaultValue("true")
        @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.RED_DYE)
        var enabled: Boolean = true,

        @ConfigDescription("Send updates to ops")
        @DefaultValue("true")
        @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.RED_DYE)
        var sendToOps: Boolean = true,
    )

    data class Debug(
        @ConfigDescription("Debug active")
        @DefaultValue("false")
        @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.RED_DYE)
        var active: Boolean = false,

        @ConfigDescription("Debug level")
        @DefaultValue("3")
        @ConfigIcon(Material.HOPPER)
        var level: Int = 3,
    )
}
