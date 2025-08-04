package yv.tils.config

import yv.tils.utils.data.Data

class ConfigYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "config",
            "1.0.0",
            "YVtils Config Module",
            "YVtils",
            "https://docs.yvtils.net/config/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }
}
