package language

import files.FileUtils
import logger.Logger
import java.util.*

class BuildLanguage {
    companion object {
        private val registeredStrings = mutableListOf<RegisteredString>()

        fun registerString(registeredString: RegisteredString) {
            registeredStrings.add(registeredString)

            Logger.debug("Registered string: ${registeredString.key} -> ${registeredString.value} in ${registeredString.file.name}")

            Logger.debug("Registered strings: ${registeredStrings.size}")
        }

        fun buildFiles() {
            Logger.debug("Building language files...")

            val languageMap = mutableMapOf<String, String>()
            var currentLanguage: FileTypes? = null
            val groupedStrings = registeredStrings.groupBy { it.file }

            groupedStrings.forEach { (fileType, strings) ->
                Logger.debug("Processing file type: ${fileType.name}")
                strings.forEach { registeredString ->
                    Logger.debug("Registering string: ${registeredString.key} -> ${registeredString.value} in ${fileType.name}")
                    val lang = registeredString.file
                    val key = registeredString.key
                    val value = registeredString.value

                    if (currentLanguage == null) {
                        Logger.debug("Creating new language file for ${lang.name}")
                        currentLanguage = lang
                    } else if (currentLanguage != lang) {
                        Logger.debug("Saving language file for ${currentLanguage!!.name}")

                        val ymlConfig = FileUtils.makeYAMLFile("/languages/${currentLanguage!!.name.lowercase(Locale.getDefault())}.yml", languageMap)

                        FileUtils.saveFile("/languages/${currentLanguage!!.name.lowercase(Locale.getDefault())}.yml", ymlConfig)

                        languageMap.clear()
                        currentLanguage = lang
                    }

                    languageMap[key] = value
                }
            }

            if (currentLanguage != null) {
                Logger.debug("Saving last language file for ${currentLanguage!!.name}")
                val ymlConfig = FileUtils.makeYAMLFile("/languages/${currentLanguage!!.name.lowercase(Locale.getDefault())}.yml", languageMap)
                FileUtils.saveFile("/languages/${currentLanguage!!.name.lowercase(Locale.getDefault())}.yml", ymlConfig)
            }
        }
    }

    data class RegisteredString(val file: FileTypes, val key: String, val value: String)
}

enum class FileTypes {
    EN,
    DE,
}