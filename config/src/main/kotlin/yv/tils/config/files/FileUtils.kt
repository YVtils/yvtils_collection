/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.config.files

import org.bukkit.configuration.file.YamlConfiguration
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.io.File
import java.io.FileNotFoundException
import yv.tils.config.files.YMLFileUtils.Companion.YAMLFile
import yv.tils.config.files.JSONFileUtils.Companion.JSONFile

class FileUtils {
    companion object {
        fun loadFile(path: String, overwriteParentDir: Boolean = false): Any {
            val file = if (overwriteParentDir) {
                File(path)
            } else {
                // Normalize path to avoid leading slash creating absolute paths on Windows
                val relPath = path.trimStart('/','\\')
                File(Data.pluginFolder, relPath)
            }

            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            Logger.debug("Loading file: $path")

            return when (file.extension.lowercase()) {
                "yml" -> YMLFileUtils.loadYAMLFile(path, overwriteParentDir)
                "json" -> JSONFileUtils.loadJSONFile(path, overwriteParentDir)
                else -> throw Exception("This file extension is not supported by this function!")
            }
        }

        fun loadYAMLFile(path: String, overwriteParentDir: Boolean = false): YAMLFile =
            YMLFileUtils.loadYAMLFile(path, overwriteParentDir)

        fun loadJSONFile(path: String, overwriteParentDir: Boolean = false): JSONFile =
            JSONFileUtils.loadJSONFile(path, overwriteParentDir)

        fun loadYAMLFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<YAMLFile> =
            YMLFileUtils.loadYAMLFilesFromFolder(folder, overwriteParentDir)

        fun loadJSONFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<JSONFile> =
            JSONFileUtils.loadJSONFilesFromFolder(folder, overwriteParentDir)

        fun loadFilesFromFolder(
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
                is JSONFile -> file.writeText(JSONFileUtils.json.encodeToString(content.content))
                else -> throw Exception("This file extension is not supported by this function!")
            }
        }

        fun updateFile(path: String, content: Any, overwriteExisting: Boolean = false) {
            Logger.debug("Updating file: $path | overwriteExisting: $overwriteExisting")
            Logger.debug("Content: $content", 3)

            // Normalize path to avoid leading slash creating absolute paths on Windows
            val relPath = path.trimStart('/','\\')
            val file = File(Data.pluginFolder, relPath)

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

                    Logger.debug("FileUtils.updateFile: updating YAML with ${newContent.getKeys(false).size} top-level keys", 2)
                    Logger.debug("FileUtils.updateFile: overwriteExisting = $overwriteExisting", 2)
                    
                    // Merge new content with existing, with optional overwrite
                    newContent.getKeys(true).forEach { key ->
                        if (overwriteExisting || !existingYaml.content.contains(key)) {
                            val value = newContent[key]
                            Logger.debug("FileUtils.updateFile: setting key '$key' = $value", 3)
                            existingYaml.content[key] = value
                        }
                    }

                    existingYaml.content.save(file)
                    Logger.debug("YAML file saved to: ${file.absolutePath}", 2)
                }

                path.endsWith(".json", ignoreCase = true) -> {
                    // Avoid overwriting existing data on startup with empty arrays
                    // Only proceed with update if not empty or if explicitly requested
                    val existingJson = loadJSONFile(path)

                    // Convert content to JsonObject
                    val newContent = when (content) {
                        is JSONFile -> content.content
                        else -> {
                            val jsonString = JSONFileUtils.json.encodeToString(content)
                            JSONFileUtils.json.decodeFromString(kotlinx.serialization.json.JsonObject.serializer(), jsonString)
                        }
                    }

                    // Skip updating if new content contains empty arrays and we're not forcing overwrite
                    if (!overwriteExisting && JSONFileUtils.isEmptyArraysOnly(newContent)) {
                        Logger.debug("Skipping update with empty arrays: $path")
                        return
                    }

                    // For complete replacement, just use the new content directly
                    if (overwriteExisting) {
                        file.writeText(JSONFileUtils.json.encodeToString(newContent))
                        Logger.debug("JSON file completely replaced: $path")
                        return
                    }

                    // Otherwise, do a smart merge of the objects
                    val mergedJson = JSONFileUtils.mergeJsonObjectsWithArrayAppend(existingJson.content, newContent)
                    file.writeText(JSONFileUtils.json.encodeToString(mergedJson))
                    Logger.debug("JSON file updated with merged content: $path")
                }

                else -> throw IllegalArgumentException("Unsupported file extension: ${file.extension}")
            }
        }

        fun makeYAML(content: Map<String, Any>): YamlConfiguration {
            Logger.debug("Creating YAML configuration...")

            val yaml = YamlConfiguration()
            content.forEach { (key, value) -> yaml[key] = value }

            return yaml
        }
    }
}
