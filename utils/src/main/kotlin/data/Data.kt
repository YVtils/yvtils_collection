package data

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class Data {
    companion object {
        var yvtilsVersion = ""
        lateinit var instance: JavaPlugin
        lateinit var key: NamespacedKey

        var prefix = "<dark_gray>[<blue>YVtils-Collection<dark_gray>]<white>"

        var loadedModules = mutableListOf<String>()
    }
}