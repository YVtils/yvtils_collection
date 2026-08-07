/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.regions.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable
import yv.tils.regions.data.Flag

/**
 * regions' `config.yml`, as a plain (nested) `data class` persisted via
 * `ObjectMapperFileUtils` - replaces the old `ConfigEntry`-list-built-by-hand version of this
 * file. The per-flag defaults ([Flags]) mirror `Flag.entries`' own `defaultGroup`/
 * `defaultValue` and are what [ConfigFile.getFlags]/[ConfigFile.getFlagTypes] now read from
 * directly, instead of dotted `"flags.global.PVP"`-style string keys into a flat map.
 *
 * No GUI is wired up for this module yet - the `@ConfigDescription` annotations are already
 * in place so one can be added later with zero further changes to this file.
 */
data class RegionsConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/modules/regions/config.yml",

    var settings: Settings = Settings(),
    var flags: Flags = Flags(),
) {
    data class Settings(
        var player: PlayerSettings = PlayerSettings(),
        var region: RegionSettings = RegionSettings(),
    ) {
        data class PlayerSettings(
            var max: Max = Max(),
        ) {
            data class Max(
                @ConfigDescription("Max owned regions per player")
                var own: Int = 5,

                @ConfigDescription("Max member regions per player")
                var member: Int = -1,
            )
        }

        data class RegionSettings(
            var max: Max = Max(),
            var min: Min = Min(),
        ) {
            data class Max(
                @ConfigDescription("Max region size")
                var size: Int = 1000,

                @ConfigDescription("Max members per region")
                var members: Int = -1,
            )

            data class Min(
                @ConfigDescription("Min region size")
                var size: Int = 1,
            )
        }
    }

    /**
     * Per-[Flag] defaults, keyed by the flag itself rather than a dotted string path.
     * `global`/`locked.global` hold booleans (allowed/denied); `role_based`/`locked.role_based`
     * hold the minimum [yv.tils.regions.data.RegionRoles] name required to use the flag.
     *
     * Field names (including the `role_based`/`locked` nesting) intentionally match the
     * original dotted-key paths (`"flags.role_based.PLACE"`, `"flags.locked.global.PVP"`,
     * ...) exactly, so an already-deployed `config.yml` keeps loading correctly.
     */
    data class Flags(
        var global: MutableMap<Flag, Boolean> = mutableMapOf(Flag.PVP to true),
        var role_based: MutableMap<Flag, String> = mutableMapOf(
            Flag.PLACE to "MEMBER",
            Flag.DESTROY to "MEMBER",
            Flag.CONTAINER to "MEMBER",
            Flag.INTERACT to "MEMBER",
            Flag.TELEPORT to "MEMBER",
        ),
        var locked: Locked = Locked(),
    ) {
        data class Locked(
            var global: MutableMap<Flag, Boolean> = mutableMapOf(),
            var role_based: MutableMap<Flag, String> = mutableMapOf(),
        )
    }
}
