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

package yv.tils.message

import dev.jorel.commandapi.CommandAPI
import yv.tils.message.commands.MSGCommand
import yv.tils.message.commands.ReplyCommand
import yv.tils.message.language.RegisterStrings
import yv.tils.message.listeners.PlayerQuit
import yv.tils.message.logic.MessageHandler
import yv.tils.utils.data.Data

class MessageYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "message",
            "1.0.0",
            "Message module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/message/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        unregisterCommands()

        registerCommands()
        registerListeners()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        MessageHandler().clearSessions()
    }

    private fun registerCommands() {
        MSGCommand()
        ReplyCommand()
    }

    private fun unregisterCommands() {
        CommandAPI.unregister("w")
        CommandAPI.unregister("whisper")
        CommandAPI.unregister("msg")
        CommandAPI.unregister("tell")
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerQuit(), plugin)
    }
}
