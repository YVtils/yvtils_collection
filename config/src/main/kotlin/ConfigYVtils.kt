
import data.Data

class ConfigYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "config"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")
    }

    override fun disablePlugin() {

    }
}