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

package yv.tils.discord.actions.commands

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import yv.tils.discord.actions.commands.handler.JDAServerInfo
import yv.tils.discord.actions.commands.handler.JDAWhitelist
import yv.tils.discord.logic.AppLogic
import yv.tils.utils.logger.Logger

class JDACommandsRegister {

    fun registerCommands() {
        val commandData: MutableList<CommandData> = mutableListOf()
        serverInfoCMD(commandData)
        whitelistCMD(commandData)
        try {
            AppLogic.getJDA().updateCommands().addCommands(commandData).queue()
        } catch (e: IllegalStateException) {
            Logger.error("Failed to register commands: ${e.message}")
        }
    }

    private fun serverInfoCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAServerInfo().registerCommand())
    }

    private fun whitelistCMD(commandData: MutableList<CommandData>) {
        commandData.add(JDAWhitelist().registerCommand())
    }
}
