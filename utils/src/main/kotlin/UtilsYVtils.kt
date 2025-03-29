import coroutine.CoroutineHandler
import data.Data
import serverVersion.VersionUtils

class UtilsYVtils : Data.YVtilsModule {
    companion object {
        const val MODULENAME = "utils"
        const val MODULEVERSION = "1.0.0"
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        VersionUtils().loadServerVersion()
    }

    override fun disablePlugin() {
        CoroutineHandler.cancelAllTasks()
    }
}