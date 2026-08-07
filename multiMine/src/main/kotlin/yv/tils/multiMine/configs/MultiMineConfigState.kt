/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.multiMine.configs

import org.bukkit.Material
import org.bukkit.Tag
import yv.tils.configv2.data.annotations.BooleanIcon
import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.ConfigIcon
import yv.tils.configv2.data.annotations.DefaultValue
import yv.tils.configv2.data.annotations.MaterialNameList
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * multiMine's `config.yml`, as a plain `data class` persisted via `ObjectMapperFileUtils`
 * and edited via `gui-v2`'s `DataClassConfigGui` (see `ManageGUI`) - replaces the old
 * `ConfigEntry`-list-built-by-hand version of this file.
 */
data class MultiMineConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/multiMine/config.yml",

    @ConfigDescription("Set the default state of multiMine for new players")
    @DefaultValue("true")
    @BooleanIcon(whenTrue = Material.LIME_DYE, whenFalse = Material.RED_DYE)
    var defaultState: Boolean = true,

    @ConfigDescription("Set the animation time in ticks")
    @DefaultValue("3")
    @ConfigIcon(Material.CLOCK)
    var animationTime: Int = 3,

    @ConfigDescription("Set the cooldown time in ticks")
    @DefaultValue("3")
    @ConfigIcon(Material.SNOWBALL)
    var cooldownTime: Int = 3,

    @ConfigDescription("Set the maximum number of blocks that can be broken in one go")
    @DefaultValue("250")
    @ConfigIcon(Material.DIAMOND_PICKAXE)
    var breakLimit: Int = 250,

    @ConfigDescription("Set whether leaves should decay when trees are cut")
    @DefaultValue("true")
    @BooleanIcon(whenTrue = Material.OAK_LEAVES, whenFalse = Material.NETHER_WART_BLOCK)
    var leaveDecay: Boolean = true,

    @ConfigDescription("Set whether only blocks of the same type should be broken")
    @DefaultValue("true")
    @BooleanIcon(whenTrue = Material.HOPPER, whenFalse = Material.RED_DYE)
    var matchBlockTypeOnly: Boolean = true,

    @ConfigDescription("Set whether the used tool can break or not when using multiMine")
    @DefaultValue("true")
    @BooleanIcon(whenTrue = Material.ANVIL, whenFalse = Material.RED_DYE)
    var canToolsBreak: Boolean = true,

    @ConfigDescription("Modify the list of blocks that can be broken using multiMine")
    @ConfigIcon(Material.BUNDLE)
    @MaterialNameList
    var blocks: List<String> = createTemplateBlocks(),
)

// TODO: Test if list gets updated with version updates
private fun createTemplateBlocks(): List<String> {
    val blocks = Tag.LOGS.values.toMutableList()

    val ores = listOf(
        Material.COAL_ORE,
        Material.IRON_ORE,
        Material.GOLD_ORE,
        Material.DIAMOND_ORE,
        Material.EMERALD_ORE,
        Material.LAPIS_ORE,
        Material.REDSTONE_ORE,
        Material.COPPER_ORE,
        Material.DEEPSLATE_COAL_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.NETHER_QUARTZ_ORE,
        Material.NETHER_GOLD_ORE,
        Material.ANCIENT_DEBRIS,
        Material.GLOWSTONE,
    )
    blocks.addAll(ores)

    return blocks.map { it.name }
}
