package yv.tils.server.configs

import yv.tils.config.files.YMLFileUtils
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.utils.logger.Logger

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()

        fun get(key: String): Any? {
            return config[key]
        }

        fun set(key: String, value: Any) {
            config[key] = value
            ConfigFile().registerStrings(config)
        }
    }

    private val filePath = "/server/config.yml"

    fun loadConfig() {
    val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        val entries = mutableListOf<ConfigEntry>()

        if (content.isEmpty()) {
            entries.add(ConfigEntry("documentation", EntryType.STRING, null, "https://docs.yvtils.net/server/config.yml", "Documentation URL"))
            entries.add(ConfigEntry("motd.enabled", EntryType.BOOLEAN, null, true, "Enable MOTD"))
            entries.add(ConfigEntry("info.maxPlayers", EntryType.INT, null, 0, "Max players override"))
            entries.add(ConfigEntry("motd.entries.top", EntryType.LIST, null, listOf(
                "Welcome to the server!",
                "This server is running YVtils v1.0.0",
                "Server version: <version>",
                "Online players: <onlinePlayers>",
                "Max players: <maxPlayers>",
                "Date: <date>",
            ), "Top MOTD entries"))
            entries.add(ConfigEntry("motd.entries.bottom", EntryType.LIST, null, listOf(
                "Have fun!",
                "Enjoy your stay!",
                "See you next time!",
            ), "Bottom MOTD entries"))
            entries.add(ConfigEntry("hoverMOTD.enabled", EntryType.BOOLEAN, null, true, "Enable hover MOTD"))
            entries.add(ConfigEntry("hoverMOTD.entries", EntryType.LIST, null, listOf("a","b","c","d","e","f","g","h","i","j"), "Hover entries"))
            entries.add(ConfigEntry("maintenance.enabled", EntryType.BOOLEAN, null, false, "Maintenance mode"))
        } else {
            for ((k, v) in content) {
                val type = when (v) {
                    is Boolean -> EntryType.BOOLEAN
                    is Int -> EntryType.INT
                    is Double -> EntryType.DOUBLE
                    is List<*> -> EntryType.LIST
                    is Map<*, *> -> EntryType.MAP
                    is String -> EntryType.STRING
                    else -> EntryType.UNKNOWN
                }
                entries.add(ConfigEntry(k, type, null, v, null))
            }
        }

        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries(filePath, entries)
        yv.tils.config.files.FileUtils.saveFile(filePath, ymlFile)
    }
}
