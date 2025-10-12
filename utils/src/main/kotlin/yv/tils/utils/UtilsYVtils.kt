package yv.tils.utils

import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.server.VersionUtils

class UtilsYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "utils",
            "1.0.0",
            "Utils module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/utils/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule(MODULE)

        VersionUtils().loadServerVersion()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        CoroutineHandler.cancelAllTasks()
    }
}
