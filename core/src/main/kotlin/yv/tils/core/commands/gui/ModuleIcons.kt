/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.core.commands.gui

import org.bukkit.Material
import yv.tils.gui.utils.Heads

/**
 * Identity icons per module, shared between `/yvtils modules` ([YVtilsModulesGui]) and
 * `/yvtils config` ([YVtilsConfigGui]) so both GUIs render the same module the same way.
 */
object ModuleIcons {
    val ICONS: Map<String, Heads> = mapOf(
        "discord" to Heads.DISCORD_LOGO,
        "multiMine" to Heads.PICKAXE,
        "essentials" to Heads.TOOLBOX,
        "sit" to Heads.OAK_STOOL,
        "status" to Heads.HEART_RED,
        "message" to Heads.ENVELOPE,
        "moderation" to Heads.SHIELD,
        "stats" to Heads.CHART,
        "server" to Heads.SERVER_RACK,
    )

    /** Material used when [ICONS] has no dedicated identity head for a module. */
    fun fallbackMaterial(name: String): Material = when (name) {
        "regions" -> Material.FILLED_MAP
        "common" -> Material.COMMAND_BLOCK
        else -> Material.PAPER
    }
}
