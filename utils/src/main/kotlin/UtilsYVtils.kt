import coroutine.CoroutineHandler
import data.Data

class UtilsYVtils {
    companion object {
        const val MODULENAME = "utils"
        const val MODULEVERSION = "1.0.0"
    }

    fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")


    }

    fun disablePlugin() {
        CoroutineHandler.cancelAllTasks()
    }
}