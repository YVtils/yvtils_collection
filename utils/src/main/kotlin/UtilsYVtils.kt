import coroutine.CoroutineHandler
import data.Data
import server.VersionUtils

class UtilsYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "utils"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        VersionUtils().loadServerVersion()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        CoroutineHandler.cancelAllTasks()
    }
}