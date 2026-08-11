/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.yv_smp.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * YV_SMP's `config.yml`, as a plain (nested) `data class` persisted via
 * `ObjectMapperFileUtils` - replaces the old `ConfigEntry`-list-built-by-hand version of
 * this file.
 *
 * Field names (including the `snake_case` ones like `master_volume`) intentionally match the
 * original dotted-key paths exactly, so an already-deployed `config.yml` keeps loading
 * correctly.
 */
data class YVSMPConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL for YV_SMP configuration")
    val documentation: String = "https://docs.yvtils.net/yv_smp/config.yml",

    var music: Music = Music(),
) {
    data class Music(
        @ConfigDescription("Enable or disable the music system (requires Simple Voice Chat)")
        var enabled: Boolean = true,

        @ConfigDescription(
            "Master volume for all audio playback (0.0 = mute, 1.0 = full volume). This " +
                "affects all music played via /music commands. Default: 0.5 (50%)"
        )
        var master_volume: Double = 0.5,

        @ConfigDescription(
            "Default volume for individual audio tracks before master volume is applied " +
                "(0.0 - 1.0). Default: 1.0 (100%)"
        )
        var default_volume: Double = 1.0,

        @ConfigDescription("Allow players to override master volume with their own volume settings (future feature)")
        var allow_player_override: Boolean = false,
    )
}
