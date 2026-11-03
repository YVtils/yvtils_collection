/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.status.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/** Parse player formatting in isolation, never as part of the server-controlled template. */
internal object StatusDisplay {
    private val miniMessage = MiniMessage.miniMessage()

    fun render(template: String, status: String, playerName: String, prefix: String = ""): Component =
        // MiniMessage may put the template's first style on its root. Keep that root
        // inside a neutral parent so callers appending chat text cannot inherit it.
        Component.empty().append(
            miniMessage.deserialize(
                template,
                Placeholder.component("status", miniMessage.deserialize(status)),
                Placeholder.unparsed("playername", playerName),
                Placeholder.component("prefix", miniMessage.deserialize(prefix)),
            )
        )

    /** Keep an already-rendered display intact when embedding it in a localized message. */
    fun message(template: String, display: Component, prefix: String): Component =
        miniMessage.deserialize(
            template,
            Placeholder.component("status", display),
            Placeholder.component("prefix", miniMessage.deserialize(prefix)),
        )
}
