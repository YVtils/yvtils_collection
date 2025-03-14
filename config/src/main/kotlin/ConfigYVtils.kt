
import data.Data

class ConfigYVtils {
    companion object {
        const val MODULENAME = "config"
        const val MODULEVERSION = "1.0.0"
    }

    fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")
    }

    fun disablePlugin() {

    }
}