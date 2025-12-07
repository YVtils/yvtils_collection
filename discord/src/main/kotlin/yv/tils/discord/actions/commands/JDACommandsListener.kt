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

package yv.tils.discord.actions.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.actions.commands.handler.JDAServerInfo
import yv.tils.discord.actions.commands.handler.JDAWhitelist

class JDACommandsListener : ListenerAdapter() {
    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        val command = e.name

        when (command) {
            "mcinfo" -> {
                JDAServerInfo().executeCommand(e)
            }
            "whitelist" -> {
                JDAWhitelist().executeCommand(e)
            }
        }
    }
}
