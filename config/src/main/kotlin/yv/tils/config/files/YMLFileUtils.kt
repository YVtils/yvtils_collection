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
            val file = if (overwriteParentDir) File(path) else File(Data.pluginFolder, path)

            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            val yaml = YamlConfiguration.loadConfiguration(file)
            return YAMLFile(file, yaml)
        }

        fun loadYAMLFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<YAMLFile> =
            loadFilesFromFolder(folder, "yml", overwriteParentDir).mapNotNull { it as? YAMLFile }

        fun makeYAMLFile(path: String, content: Map<String, Any>): YAMLFile {
            Logger.debug("Creating YAML file: $path")

            val yaml = makeYAML(content)
            val file = File(Data.pluginFolder, path)

            Logger.debug("YAML object: $yaml", 3)

            return YAMLFile(file, yaml)
        }
    }
}