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

package yv.tils.gui.utils

import org.bukkit.Material
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder

/**
 * Provides filler items for GUI borders/background.
 *
 * With InvUI, filling is usually handled declaratively through the `#`
 * ingredient of a [xyz.xenondevs.invui.gui.Gui] structure, e.g.:
 * ```
 * .addIngredient('#', Filler.item())
 * ```
 */
object Filler {
    fun pane(material: Material = Material.GRAY_STAINED_GLASS_PANE): ItemBuilder =
        ItemBuilder(material).setName(" ").hideTooltip(true)

    fun item(material: Material = Material.GRAY_STAINED_GLASS_PANE): Item =
        Item.simple(pane(material))
}
