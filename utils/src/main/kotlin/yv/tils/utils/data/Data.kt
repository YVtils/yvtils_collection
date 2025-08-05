package yv.tils.utils.data

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.utils.colors.Colors
import yv.tils.utils.logger.Logger
import java.io.File

class Data {
    companion object {
        var yvtilsVersion = ""
        lateinit var instance: JavaPlugin
        lateinit var key: NamespacedKey

        lateinit var core: YVtilsCore

        /**
         * The folder where the plugin is located.
         * This is used to store plugin data and configuration files.
         */
        var pluginFolder = File("plugins/yvtils")

        var pluginURL = "https://modrinth.com/organization/yvtils"
        var pluginName = "YVtils Null"
        var pluginShortName = "null"
        var prefix: String
            set(value) {
                "<dark_gray>[<${Colors.MAIN.color}>YVtils $value<dark_gray>]<white>"
            }
            get() {
                return "<dark_gray>[<${Colors.MAIN.color}>YVtils $pluginName<dark_gray>]<white>"
            }

        fun initCore(core: YVtilsCore) {
            yvtilsVersion = core.version
            instance = core.instance
            key = core.key
            pluginName = core.name
            pluginShortName = core.pluginShort
            pluginURL = core.url

            this.core = core
        }

        private val loadedYVtilsModules = mutableListOf<YVtilsModuleData>()
        fun addModule(module: YVtilsModuleData): Boolean {
            return loadedYVtilsModules.add(module)
        }

        fun removeModule(module: YVtilsModuleData): Boolean {
            Logger.Companion.dev("Removing module: ${module.name} v${module.version}")
            return loadedYVtilsModules.remove(module)
        }

        fun getModules(): List<YVtilsModuleData> {
            return loadedYVtilsModules
        }

        fun getModuleNames(sorted: Boolean = false): List<String> {
            return if (sorted) {
                loadedYVtilsModules.sortedBy { it.name }.map { it.name }
            } else {
                loadedYVtilsModules.map { it.name }
            }
        }

        fun getModule(name: String): YVtilsModuleData? {
            return loadedYVtilsModules.find { it.name.equals(name, ignoreCase = true) }
        }

        fun getModulesAsString(sorted: Boolean = false): String {
            return if (sorted) {
                loadedYVtilsModules.sortedBy { it.name }.joinToString(", ") { "${it.name} v${it.version}" }
            } else {
                loadedYVtilsModules.joinToString(", ") { "${it.name} v${it.version}" }
            }
        }
    }

    /**
     * Data class representing the core of YVtils.
     *
     * This class contains metadata about the YVtils core, such as its name, version, description, color, instance,
     * key, plugin short name, URL, and dependencies.
     * It is used to initialize the YVtils framework.
     * @param name The name of the YVtils core.
     * @param version The version of the YVtils core.
     * @param description A brief description of the YVtils core (optional).
     * @param colorHex The color hex code for the YVtils core (default is Colors.MAIN.color).
     * @param instance The JavaPlugin instance of the YVtils core.
     * @param key The NamespacedKey for the YVtils core.
     * @param pluginShort The short name of the plugin.
     * @param url The URL to the YVtils core (default is "https://modrinth.com/organization/yvtils").
     * @param dependencies A list of dependencies (yvtils modules) for the YVtils core (default is an empty list).
     */
    data class YVtilsCore(
        val name: String,
        val version: String,
        val description: String = "",
        val colorHex: String = Colors.MAIN.color,
        val instance: JavaPlugin,
        val key: NamespacedKey,
        val pluginShort: String,
        val url: String = "https://modrinth.com/organization/yvtils",
        val dependencies: List<String> = listOf(),
    )

    /**
     * Data class representing a YVtils module.
     *
     * This class contains metadata about the module, such as its name, version, description, author, documentation URL.
     * It is used to register and manage modules within the YVtils framework.
     * @param name The name of the module.
     * @param version The version of the module.
     * @param description A brief description of the module (optional).
     * @param author The author of the module (default is "YVtils").
     * @param documentation The URL to the module's documentation (default is "https://docs.yvtils.net").
     */
    data class YVtilsModuleData(
        val name: String,
        val version: String,
        val description: String = "",
        val author: String = "YVtils",
        val documentation: String = "https://docs.yvtils.net",
    )

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
