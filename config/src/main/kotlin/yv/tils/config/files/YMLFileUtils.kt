/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
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
import yv.tils.config.files.FileUtils.Companion.loadFilesFromFolder
import yv.tils.config.files.FileUtils.Companion.makeYAML
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.io.File
import java.io.FileNotFoundException

class YMLFileUtils {
    companion object {
        data class YAMLFile(val file: File, val content: YamlConfiguration)

        fun loadYAMLFile(path: String, overwriteParentDir: Boolean = false): YAMLFile {
            val file = if (overwriteParentDir) {
                File(path)
            } else {
                val relPath = path.trimStart('/','\\')
                File(Data.pluginFolder, relPath)
            }

            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            val yaml = YamlConfiguration.loadConfiguration(file)
            return YAMLFile(file, yaml)
        }

        fun loadYAMLFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<YAMLFile> =
            loadFilesFromFolder(folder, "yml", overwriteParentDir).mapNotNull { it as? YAMLFile }

        fun makeYAMLFile(path: String, content: Map<String, Any>): YAMLFile {
            Logger.debug("Creating YAML file: $path")

            val yaml = makeYAML(content)
            val relPath = path.trimStart('/','\\')
            val file = File(Data.pluginFolder, relPath)

            Logger.debug("YAML object: $yaml", 3)

            return YAMLFile(file, yaml)
        }

        fun makeYAMLFileFromEntries(path: String, entries: List<yv.tils.config.data.ConfigEntry>): YAMLFile {
            Logger.debug("Creating YAML file from ConfigEntry list: $path")

            val map = mutableMapOf<String, Any>()

            for (entry in entries) {
                // Prefer explicit value, otherwise defaultValue
                val value = entry.value ?: entry.defaultValue
                if (value != null) map[entry.key] = value
            }

            return makeYAMLFile(path, map)
        }
    }
}