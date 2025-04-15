package yv.tils.claim

import data.Data
import yv.tils.claim.language.RegisterStrings

class ClaimYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "claim"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        unregisterCommands()

        registerCommands()
        registerListeners()
    }

    override fun disablePlugin() {
    }

    private fun registerCommands() {

    }

    private fun unregisterCommands() {

    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

    }
}