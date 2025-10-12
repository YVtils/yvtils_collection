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
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }
}
