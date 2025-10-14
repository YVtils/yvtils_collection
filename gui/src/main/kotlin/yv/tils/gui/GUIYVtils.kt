package yv.tils.gui

import yv.tils.utils.data.Data

class GUIYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "gui",
            "1.0.0",
            "GUI module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/gui/"
        )
    }

    override fun onLoad() {
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        registerListeners()

    }

    override fun onLateEnablePlugin() {

    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager
        pm.registerEvents(yv.tils.gui.listeners.InventoryClickListener(), plugin)
        pm.registerEvents(yv.tils.gui.listeners.InventoryCloseListener(), plugin)
        pm.registerEvents(yv.tils.gui.listeners.PlayerChatListener(), plugin)
    }

    override fun disablePlugin() {

    }
}
