package yv.tils.common.config

import files.FileUtils
import logger.Logger

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()

        fun getValue(key: String): Any? {
            return config[key]
        }

        fun getValueAsString(key: String): String? {
            return config[key]?.toString()
        }

        fun getValueAsInt(key: String): Int? {
            return config[key]?.toString()?.toIntOrNull()
        }

        fun getValueAsBoolean(key: String): Boolean? {
            return config[key]?.toString()?.toBoolean()
        }
    }

    private val filePath = "/config.yml"

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
            content["documentation"] = "https://docs.yvtils.net/config.yml"

            content["language"] = "en"

            content["serverIP"] = "smp.net"
            content["serverPort"] = -1

            content["timezone"] = "default"

            content["updateCheck.enabled"] = true
            content["updateCheck.sendToOps"] = true

            content["debug.active"] = false
            content["debug.level"] = 3
        }

        val ymlFile = FileUtils.makeYAMLFile(filePath, content)
        FileUtils.saveFile(filePath, ymlFile)
    }
}