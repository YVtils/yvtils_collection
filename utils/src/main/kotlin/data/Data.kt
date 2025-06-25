package data

import colors.Colors
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class Data {
    companion object {
        var yvtilsVersion = ""
        lateinit var instance: JavaPlugin
        lateinit var key: NamespacedKey

        var pluginURL = "https://modrinth.com/organization/yvtils"
        var pluginName = "null"
        var pluginShortName = "null"
        var prefix: String
            set(value) {
                "<dark_gray>[<${Colors.MAIN.color}>YVtils $value<dark_gray>]<white>"
            }
            get() {
                return "<dark_gray>[<${Colors.MAIN.color}>YVtils $pluginName<dark_gray>]<white>"
            }

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
        /**
         * onLoad is called, when the plugin is loaded.
         * Extends the default JavaPlugin.onLoad() method.
         */
        fun onLoad()

        /**
         * onEnable is called, when the plugin is enabled.
         * Extends the default JavaPlugin.onEnable() method.
         */
        fun enablePlugin()

        /**
         * Called when the plugin is enabled after all modules have been loaded.
         * This is useful for modules that need to wait for other modules to be loaded.
         */
        fun onLateEnablePlugin()

        /**
         * onDisable is called, when the plugin is disabled.
         * Extends the default JavaPlugin.onDisable() method.
         */
        fun disablePlugin()
    }
}