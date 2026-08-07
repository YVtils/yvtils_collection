/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.stats.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * stats' `config.yml`, as a plain (nested) `data class` persisted via
 * `ObjectMapperFileUtils` - replaces the old `ConfigEntry`-list-built-by-hand version of
 * this file.
 *
 * Field names (including the `snake_case` ones like `opt_in`/`server_name`) intentionally
 * match the original dotted-key paths exactly, so an already-deployed `config.yml` keeps
 * loading correctly.
 *
 * [opt_in] is nullable and defaults to `null` - matching this file's original stated intent
 * ("No default - must be explicitly set") rather than the `true` the old `ConfigEntry` code
 * actually passed as `defaultValue` (which, combined with `onLoad()` persisting defaults
 * *before* ever reading the real file - the same root cause fixed project-wide earlier in
 * this migration - meant `needsOptInPrompt()` could never actually return `true`: the very
 * first `registerStrings()` call always wrote `opt_in: true` to disk before the real value
 * was ever loaded). This restores the intended behavior: the consent prompt now genuinely
 * shows once, for real, on a fresh install.
 */
data class StatsConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/stats/config.yml",

    @ConfigDescription(
        "Whether you have opted in to anonymous stats collection. Set to true to enable, " +
            "false to disable. Stats are sent to api.yvtils.net/stats"
    )
    var opt_in: Boolean? = null,

    var metadata: Metadata = Metadata(),

    @ConfigDescription("Maximum size for list-type stats to prevent memory issues.")
    var max_list_size: Int = 1000,

    @ConfigDescription("Maximum number of stats to prevent high cardinality issues.")
    var max_stats_count: Int = 10000,
) {
    data class Metadata(
        @ConfigDescription("Optional human-readable name for the server shown in developer stats.")
        var server_name: String = "",

        @ConfigDescription("Whether to include current player count in exported stats.")
        var collect_player_count: Boolean = true,
    )
}
