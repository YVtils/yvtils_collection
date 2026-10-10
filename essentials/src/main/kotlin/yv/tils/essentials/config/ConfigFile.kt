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
import yv.tils.configv2.data.annotations.ConfigIcon
import yv.tils.configv2.data.annotations.BooleanIcon
import org.bukkit.Material
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

    /** Save a validated draft before publishing it to gameplay listeners. */
    fun applyState(updated: EssentialsConfigState) {
        require(updated.spawnElytra.radius.isFinite() && updated.spawnElytra.radius >= 0) {
            "Spawn Elytra radius must be finite and non-negative"
        }
        require(updated.spawnElytra.boostStrength.isFinite() && updated.spawnElytra.boostStrength > 0) {
            "Spawn Elytra boost strength must be finite and positive"
        }
        require(updated.spawnElytra.worlds.all { it.isNotBlank() }) { "World names must not be blank" }
        val snapshot =
            updated.copy(spawnElytra = updated.spawnElytra.copy(worlds = updated.spawnElytra.worlds.toList()))
        ObjectMapperFileUtils.save(FILE_PATH, snapshot, format = ConfigFormat.YAML)
        state = snapshot
    }
}

data class EssentialsConfigState(
    @ConfigDescription("Allow MiniMessage colors and formatting in player chat")
    @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.GRAY_DYE)
    var allowChatColors: Boolean = true,
    @ConfigDescription("Remove the anvil Too Expensive limit; normal XP costs still apply")
    @BooleanIcon(whenTrue = Material.ANVIL, whenFalse = Material.DAMAGED_ANVIL)
    var disableTooExpensive: Boolean = true,
    @ConfigDescription("Show the real XP level cost in chat for anvil operations costing 40 or more levels")
    @BooleanIcon(whenTrue = Material.EXPERIENCE_BOTTLE, whenFalse = Material.GLASS_BOTTLE)
    var showAnvilCost: Boolean = true,
    @ConfigDescription("Spawn gliding settings")
    @ConfigIcon(Material.ELYTRA)
    var spawnElytra: SpawnElytraConfig = SpawnElytraConfig(),
)

data class SpawnElytraConfig(
    @ConfigDescription("Enable double-jump gliding near world spawn for Survival players")
    @BooleanIcon(whenTrue = Material.ELYTRA, whenFalse = Material.FEATHER)
    var enabled: Boolean = false,
    @ConfigDescription("World names where Spawn Elytra can be launched")
    @ConfigIcon(Material.GRASS_BLOCK)
    var worlds: List<String> = listOf("world"),
    @ConfigDescription("Launch radius in blocks, measured in 3D from each world's spawn")
    @ConfigIcon(Material.COMPASS)
    var radius: Double = 100.0,
    @ConfigDescription("Velocity multiplier for the single swap-offhand boost per flight")
    @ConfigIcon(Material.FIREWORK_ROCKET)
    var boostStrength: Double = 2.0,
)
