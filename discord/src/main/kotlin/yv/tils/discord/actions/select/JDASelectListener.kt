/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.actions.select

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.actions.select.handler.JDAAccountRemove

class JDASelectListener : ListenerAdapter() {
    override fun onStringSelectInteraction(e: StringSelectInteractionEvent) {
        JDAAccountRemove().handleForceRemove(e)
    }
}
