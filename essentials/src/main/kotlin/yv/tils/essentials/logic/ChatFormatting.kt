/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.essentials.logic

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

internal object ChatFormatting {
    /** Name formatting must not become the parent style of the player's message. */
    fun render(displayName: Component, message: Component): Component = Component.empty()
        .append(displayName)
        .append(Component.text(": ", NamedTextColor.WHITE))
        .append(Component.empty().color(NamedTextColor.WHITE).append(message))
}
