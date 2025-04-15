package yv.tils.claim.configs

import files.FileUtils
import logger.Logger

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
    }

    private val filePath = "/claim/config.yml"

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
            content["documentation"] = "https://docs.yvtils.net/status/config.yml"
        }

        val ymlFile = FileUtils.makeYAMLFile(filePath, content)
        FileUtils.saveFile(filePath, ymlFile)
    }
}