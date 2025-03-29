package yv.tils.message

import data.Data
import dev.jorel.commandapi.CommandAPI
import yv.tils.message.commands.MSGCommand
import yv.tils.message.commands.ReplyCommand
import yv.tils.message.language.RegisterStrings
import yv.tils.message.listeners.PlayerQuit
import yv.tils.message.logic.MessageHandler

class MessageYVtils : Data.YVtilsModule {
    companion object {
        const val MODULENAME = "message"
        const val MODULEVERSION = "1.0.0"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        unregisterCommands()

        registerCommands()
        registerListeners()
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