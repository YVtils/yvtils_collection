package yv.tils.config.files

import org.bukkit.configuration.file.YamlConfiguration
import org.codehaus.plexus.util.FileUtils.loadFile
import yv.tils.config.files.FileUtils.Companion.YAMLFile
import yv.tils.config.files.FileUtils.Companion.loadFile
import yv.tils.config.files.FileUtils.Companion.loadFilesFromFolder
import yv.tils.config.files.FileUtils.Companion.makeYAML
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.io.File

class YMLFileUtils {
    companion object {
        data class YAMLFile(val file: File, val content: YamlConfiguration)

        fun loadYAMLFile(path: String, overwriteParentDir: Boolean = false): FileUtils.Companion.YAMLFile =
            loadFile(path, overwriteParentDir) as? FileUtils.Companion.YAMLFile
                ?: throw Exception("File is not a YAML file!")

        fun loadYAMLFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<FileUtils.Companion.YAMLFile> =
            loadFilesFromFolder(folder, "yml", overwriteParentDir).mapNotNull { it as? FileUtils.Companion.YAMLFile }

        fun makeYAMLFile(path: String, content: Map<String, Any>): YAMLFile {
            Logger.debug("Creating YAML file: $path")

            val yaml = makeYAML(content)
            val file = File(Data.pluginFolder, path)

            Logger.debug("YAML object: $yaml", 3)

            return YAMLFile(file, yaml)
        }
    }
}