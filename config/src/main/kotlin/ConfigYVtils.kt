
import data.Data

class ConfigYVtils : Data.YVtilsModule {
    companion object {
        const val MODULENAME = "config"
        const val MODULEVERSION = "1.0.0"
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")
    }

    override fun disablePlugin() {

    }
}