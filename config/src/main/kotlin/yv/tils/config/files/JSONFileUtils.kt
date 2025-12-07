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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import yv.tils.config.files.FileUtils.Companion.loadFilesFromFolder
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.io.File
import java.io.FileNotFoundException

class JSONFileUtils {
    companion object {
        data class JSONFile(val file: File, val content: JsonObject)

        fun loadJSONFile(path: String, overwriteParentDir: Boolean = false): JSONFile {
            val file = if (overwriteParentDir) {
                File(path)
            } else {
                val relPath = path.trimStart('/','\\')
                File(Data.pluginFolder, relPath)
            }

            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            val content = json.decodeFromString(kotlinx.serialization.json.JsonObject.serializer(), file.readText())
            return JSONFile(file, content)
        }

        fun loadJSONFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<JSONFile> =
            loadFilesFromFolder(folder, "json", overwriteParentDir).mapNotNull { it as? JSONFile }

        /**
         * Checks if the JsonObject only contains empty arrays
         */
        fun isEmptyArraysOnly(jsonObject: JsonObject): Boolean {
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
        fun mergeJsonObjectsWithArrayAppend(original: JsonObject, update: JsonObject): JsonObject {
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
            val relPath = path.trimStart('/','\\')
            val file = File(Data.pluginFolder, relPath)

            Logger.debug("JSON object: $jsonObject", 3)

            return JSONFile(file, jsonObject)
        }

        /**
         * Recursively merges two JsonObjects, preserving arrays and nested structures
         */
        fun mergeJsonObjects(original: JsonObject, update: JsonObject): JsonObject {
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