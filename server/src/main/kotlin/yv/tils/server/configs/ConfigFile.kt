package yv.tils.server.configs

import files.FileUtils
import logger.Logger

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
        val file = FileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        if (content.isEmpty()) {
            content["documentation"] = "https://docs.yvtils.net/server/config.yml"
            content["motd.enabled"] = true
            content["info.maxPlayers"] = 0
            content["motd.entries.top"] = listOf(
                "Welcome to the server!",
                "This server is running YVtils v1.0.0",
                "Server version: <version>",
                "Online players: <onlinePlayers>",
                "Max players: <maxPlayers>",
                "Date: <date>",
            )
            content["motd.entries.bottom"] = listOf(
                "Have fun!",
                "Enjoy your stay!",
                "See you next time!",
            )
            content["hoverMOTD.enabled"] = true
            content["hoverMOTD.entries"] = listOf(
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
            )
            content["maintenance.enabled"] = false
        }

        val ymlFile = FileUtils.makeYAMLFile(filePath, content)
        FileUtils.saveFile(filePath, ymlFile)
    }
}