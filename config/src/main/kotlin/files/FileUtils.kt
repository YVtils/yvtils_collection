package files

import data.Data
import kotlinx.serialization.json.*
import logger.Logger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileNotFoundException

class FileUtils {
    companion object {
        data class JSONFile(val file: File, val content: JsonObject)
        data class YAMLFile(val file: File, val content: YamlConfiguration)

        private fun loadFile(path: String, overwriteParentDir: Boolean = false): Any {
            val file = if (overwriteParentDir) {
                File(path)
            } else {
                File(Data.pluginFolder, path)
            }

            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            Logger.debug("Loading file: $path")

            return when (file.extension.lowercase()) {
                "yml" -> YAMLFile(file, YamlConfiguration.loadConfiguration(file))
                "json" -> JSONFile(file, Json.decodeFromString(file.readText()))
                else -> throw Exception("This file extension is not supported by this function!")
            }
        }

        fun loadYAMLFile(path: String, overwriteParentDir: Boolean = false): YAMLFile =
            loadFile(path, overwriteParentDir) as? YAMLFile
                ?: throw Exception("File is not a YAML file!")

        fun loadJSONFile(path: String, overwriteParentDir: Boolean = false): JSONFile =
            loadFile(path, overwriteParentDir) as? JSONFile
                ?: throw Exception("File is not a JSON file!")

        fun loadYAMLFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<YAMLFile> =
            loadFilesFromFolder(folder, "yml", overwriteParentDir).mapNotNull { it as? YAMLFile }

        fun loadJSONFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<JSONFile> =
            loadFilesFromFolder(folder, "json", overwriteParentDir).mapNotNull { it as? JSONFile }

        private fun loadFilesFromFolder(
            folder: String,
            extension: String,
            overwriteParentDir: Boolean = false,
        ): List<Any> {
            Logger.debug("Loading files from folder: $folder with extension: $extension")

            val directory = if (overwriteParentDir) {
                File(folder)
            } else {
                File(Data.pluginFolder, folder)
            }

            if (!directory.exists() || !directory.isDirectory) return emptyList()

            return directory.listFiles { _, name -> name.endsWith(".$extension", ignoreCase = true) }
                ?.mapNotNull { file ->
                    runCatching { loadFile("$folder/${file.name}", overwriteParentDir) }.getOrNull()
                }
                ?: emptyList()
        }

        fun saveFile(path: String, content: Any) {
            Logger.debug("Saving file: $path")

            val file = File(Data.pluginFolder, path)

            if (!file.exists()) {
                file.parentFile.mkdirs()

                file.createNewFile()
            } else {
                Logger.debug("File ($path) already exists")

                updateFile(path, content)

                return
            }

            when (content) {
                is YAMLFile -> content.content.save(file)
                is JSONFile -> file.writeText(Json.encodeToString(content.content))
                else -> throw Exception("This file extension is not supported by this function!")
            }
        }

        fun updateFile(path: String, content: Any, overwriteExisting: Boolean = false) {
            Logger.debug("Updating file: $path | overwriteExisting: $overwriteExisting")
            Logger.debug("Content: $content", 3)

            val file = File(Data.pluginFolder, path)

            if (!file.exists()) {
                Logger.debug("File doesn't exist, creating new file: $path")
                saveFile(path, content)
                return
            }

            when {
                path.endsWith(".yml", ignoreCase = true) -> {
                    val existingYaml = loadYAMLFile(path)
                    val newContent = (content as? YAMLFile)?.content
                        ?: throw IllegalArgumentException("Content must be YAMLFile for .yml files")

                    // Merge new content with existing, with optional overwrite
                    newContent.getKeys(true).forEach { key ->
                        if (overwriteExisting || !existingYaml.content.contains(key)) {
                            existingYaml.content[key] = newContent[key]
                        }
                    }

                    existingYaml.content.save(file)
                    Logger.debug("YAML file updated: $path")
                }

                path.endsWith(".json", ignoreCase = true) -> {
                    // Avoid overwriting existing data on startup with empty arrays
                    // Only proceed with update if not empty or if explicitly requested
                    val existingJson = loadJSONFile(path)

                    // Convert content to JsonObject
                    val newContent = when (content) {
                        is JSONFile -> content.content
                        else -> {
                            val jsonString = json.encodeToString(content)
                            json.decodeFromString(JsonObject.serializer(), jsonString)
                        }
                    }

                    // Skip updating if new content contains empty arrays and we're not forcing overwrite
                    if (!overwriteExisting && isEmptyArraysOnly(newContent)) {
                        Logger.debug("Skipping update with empty arrays: $path")
                        return
                    }

                    // For complete replacement, just use the new content directly
                    if (overwriteExisting) {
                        file.writeText(Json.encodeToString(newContent))
                        Logger.debug("JSON file completely replaced: $path")
                        return
                    }

                    // Otherwise, do a smart merge of the objects
                    val mergedJson = mergeJsonObjectsWithArrayAppend(existingJson.content, newContent)
                    file.writeText(Json.encodeToString(mergedJson))
                    Logger.debug("JSON file updated with merged content: $path")
                }

                else -> throw IllegalArgumentException("Unsupported file extension: ${file.extension}")
            }
        }

        /**
         * Checks if the JsonObject only contains empty arrays
         */
        private fun isEmptyArraysOnly(jsonObject: JsonObject): Boolean {
            if (jsonObject.isEmpty()) return true

            return jsonObject.all { (_, value) ->
                when (value) {
                    is JsonArray -> value.isEmpty()
                    is JsonObject -> isEmptyArraysOnly(value)
                    else -> false
                }
            }
        }

        /**
         * Recursively merges two JsonObjects, handling arrays by appending items rather than replacing
         */
        private fun mergeJsonObjectsWithArrayAppend(original: JsonObject, update: JsonObject): JsonObject {
            val result = original.toMutableMap()

            update.forEach { (key, updateValue) ->
                if (key in result) {
                    val originalValue = result[key]
                    when {
                        originalValue is JsonObject && updateValue is JsonObject -> {
                            // Recursively merge nested objects
                            result[key] = mergeJsonObjectsWithArrayAppend(originalValue, updateValue)
                        }
                        originalValue is JsonArray && updateValue is JsonArray -> {
                            // For arrays, append new items rather than replacing
                            val combinedArray = JsonArray(originalValue + updateValue)
                            result[key] = combinedArray
                        }
                        else -> {
                            // For other types, replace with new value
                            result[key] = updateValue
                        }
                    }
                } else {
                    // Add new key-value pair
                    result[key] = updateValue
                }
            }

            return JsonObject(result)
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
            val file = File(Data.pluginFolder, path)

            Logger.debug("YAML object: $yaml", 3)

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
            val file = File(Data.pluginFolder, path)

            Logger.debug("JSON object: $jsonObject", 3)

            return JSONFile(file, jsonObject)
        }

        /**
         * Recursively merges two JsonObjects, preserving arrays and nested structures
         */
        private fun mergeJsonObjects(original: JsonObject, update: JsonObject): JsonObject {
            val result = original.toMutableMap()

            update.forEach { (key, value) ->
                if (key in result) {
                    val originalValue = result[key]
                    if (originalValue is JsonObject && value is JsonObject) {
                        // Recursively merge nested objects
                        result[key] = mergeJsonObjects(originalValue, value)
                    } else {
                        // For arrays or other types, replace with new value
                        result[key] = value
                    }
                } else {
                    // Add new key-value pair
                    result[key] = value
                }
            }

            return JsonObject(result)
        }
    }
}
