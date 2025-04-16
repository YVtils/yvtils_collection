package data

import colors.ColorUtils
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class Data {
    companion object {
        var yvtilsVersion = ""
        lateinit var instance: JavaPlugin
        lateinit var key: NamespacedKey

        var prefix = "<dark_gray>[<${ColorUtils.MAIN.color}>YVtils-Collection<dark_gray>]<white>"

        private val loadedModules = mutableListOf<String>()
        fun addModule(module: String) {
            loadedModules.add(module)
        }

        fun getModules(sorted: Boolean = false): String {
            return if (sorted) {
                loadedModules.sortedBy { it }.joinToString(", ")
            } else {
                loadedModules.joinToString(", ")
            }
        }
    }

    interface YVtilsModule {
        fun onLoad()
        fun enablePlugin()
        fun disablePlugin()
    }
}