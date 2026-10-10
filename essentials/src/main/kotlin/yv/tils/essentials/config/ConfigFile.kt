/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.essentials.config

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

class ConfigFile {
    companion object {
        private const val FILE_PATH = "/essentials/config.yml"
        var state: EssentialsConfigState = EssentialsConfigState()
    }

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(FILE_PATH, EssentialsConfigState(), format = ConfigFormat.YAML)
        ObjectMapperFileUtils.save(FILE_PATH, state, format = ConfigFormat.YAML)
    }
}

data class EssentialsConfigState(
    var spawnElytra: SpawnElytraConfig = SpawnElytraConfig(),
)

data class SpawnElytraConfig(
    @ConfigDescription("Enable double-jump gliding near world spawn for Survival players")
    var enabled: Boolean = false,
    @ConfigDescription("World names where Spawn Elytra can be launched")
    var worlds: List<String> = listOf("world"),
    @ConfigDescription("Launch radius in blocks, measured in 3D from each world's spawn")
    var radius: Double = 100.0,
    @ConfigDescription("Velocity multiplier for the single swap-offhand boost per flight")
    var boostStrength: Double = 2.0,
)
