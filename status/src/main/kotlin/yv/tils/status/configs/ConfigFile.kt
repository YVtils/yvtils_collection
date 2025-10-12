package yv.tils.status.configs

import yv.tils.config.files.YMLFileUtils
import yv.tils.utils.logger.Logger

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
    }

    private val filePath = "/status/config.yml"

    fun loadConfig() {
    val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        if (content.isEmpty()) {
            content["documentation"] = "https://docs.yvtils.net/status/config.yml"
            content["display"] = "<dark_gray>[<white><status><dark_gray>] |<white> <playerName>"
            content["maxLength"] = 20
            content["defaultStatus"] = defaultStatus()
            content["blacklist"] = blacklist()
        }

    val ymlFile = YMLFileUtils.makeYAMLFile(filePath, content)
    yv.tils.config.files.FileUtils.saveFile(filePath, ymlFile)
    }

    private fun defaultStatus(): List<String> {
        val list: MutableList<String> = mutableListOf()

        list.add("<green>Online")
        list.add("<yellow>Away")
        list.add("<red>Busy")

        return list
    }

    private fun blacklist(): List<String> {
        val list: MutableList<String> = mutableListOf()

        return list
    }
}
