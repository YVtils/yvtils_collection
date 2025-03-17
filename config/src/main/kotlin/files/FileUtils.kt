package files

import data.Data
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import logger.Logger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileNotFoundException

class FileUtils {
    companion object {
        data class JSONFile(val file: File, val content: JsonObject)
        data class YAMLFile(val file: File, val content: YamlConfiguration)

        private fun loadFile(path: String): Any {
            val file = File(Data.instance.dataFolder, path)

            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            Logger.debug("Loading file: $path")

            return when (file.extension.lowercase()) {
                "yml" -> YAMLFile(file, YamlConfiguration.loadConfiguration(file))
                "json" -> JSONFile(file, Json.decodeFromString(file.readText()))
                else -> throw Exception("This file extension is not supported by this function!")
            }
        }

        fun loadYAMLFile(path: String): YAMLFile =
            loadFile(path) as? YAMLFile
                ?: throw Exception("File is not a YAML file!")

        fun loadJSONFile(path: String): JSONFile =
            loadFile(path) as? JSONFile
                ?: throw Exception("File is not a JSON file!")

        fun loadYAMLFilesFromFolder(folder: String): List<YAMLFile> =
            loadFilesFromFolder(folder, "yml").mapNotNull { it as? YAMLFile }

        fun loadJSONFilesFromFolder(folder: String): List<JSONFile> =
            loadFilesFromFolder(folder, "json").mapNotNull { it as? JSONFile }

        private fun loadFilesFromFolder(folder: String, extension: String): List<Any> {
            Logger.debug("Loading files from folder: $folder with extension: $extension")

            val directory = File(Data.instance.dataFolder, folder)

            if (!directory.exists() || !directory.isDirectory) return emptyList()

            return directory.listFiles { _, name -> name.endsWith(".$extension", ignoreCase = true) }
                ?.mapNotNull { file ->
                    runCatching { loadFile("$folder/${file.name}") }.getOrNull()
                }
                ?: emptyList()
        }

        fun saveFile(path: String, content: Any) {
            Logger.debug("Saving file: $path")

            val file = File(Data.instance.dataFolder, path)

            if (!file.exists()) {
                file.parentFile.mkdirs()

                file.createNewFile()
            } else {
                Logger.debug("File already exists")
                return
            }

            when (content) {
                is YAMLFile -> content.content.save(file)
                is JSONFile -> file.writeText(Json.encodeToString(content.content))
                else -> throw Exception("This file extension is not supported by this function!")
            }
        }

        fun makeYAML(content: Map<String, Any>): YamlConfiguration {
            Logger.debug("Creating YAML configuration...")

            val yaml = YamlConfiguration()
            content.forEach { (key, value) -> yaml[key] = value }

            return yaml
        }

        fun makeYAMLFile(path: String, content: Map<String, Any>): YAMLFile {
            Logger.debug("Creating YAML file: $path")

            val yaml = makeYAML(content)
            val file = File(Data.instance.dataFolder, path)

            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            yaml.save(file)

            return YAMLFile(file, yaml)
        }

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        inline fun <reified T> makeJSON(content: T): JsonObject {
            Logger.debug("Creating JSON object...")

            val jsonString = json.encodeToString(content)
            return json.decodeFromString(JsonObject.serializer(), jsonString)
        }

        inline fun <reified T> makeJSONFile(path: String, content: T): JSONFile {
            Logger.debug("Creating JSON file: $path")

            val jsonString = json.encodeToString(content)
            val jsonObject = json.decodeFromString(JsonObject.serializer(), jsonString)
            val file = File(Data.instance.dataFolder, path)

            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            file.writeText(jsonString)

            return JSONFile(file, jsonObject)
        }
    }
}
