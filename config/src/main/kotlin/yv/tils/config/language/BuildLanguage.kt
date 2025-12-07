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

package yv.tils.config.language

import yv.tils.config.files.FileUtils
import yv.tils.config.files.YMLFileUtils
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import java.util.*

class BuildLanguage {
    companion object {
        private val registeredStrings = mutableListOf<RegisteredString>()

        fun registerString(registeredString: RegisteredString) {
            registeredStrings.add(registeredString)

            Logger.debug("Registered string: ${registeredString.key} -> ${registeredString.value} in ${registeredString.file.name}",DEBUGLEVEL.SPAM)

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
                    Logger.debug("Registering string: ${registeredString.key} -> ${registeredString.value} in ${fileType.name}",DEBUGLEVEL.SPAM)
                    val lang = registeredString.file
                    val key = registeredString.key
                    val value = registeredString.value

                    if (currentLanguage == null) {
                        Logger.debug("Creating new language file for ${lang.name}")
                        currentLanguage = lang
                    } else if (currentLanguage != lang) {
                        Logger.debug("Saving language file for ${currentLanguage!!.name}")

                        val ymlConfig = YMLFileUtils.makeYAMLFile(
                            "/languages/${currentLanguage !!.name.lowercase(Locale.getDefault())}.yml",
                            languageMap
                        )

                        FileUtils.saveFile(
                            "/languages/${currentLanguage !!.name.lowercase(Locale.getDefault())}.yml",
                            ymlConfig
                        )

                        languageMap.clear()
                        currentLanguage = lang
                    }

                    languageMap[key] = value
                }
            }

            if (currentLanguage != null) {
                Logger.debug("Saving last language file for ${currentLanguage.name}")
                val ymlConfig = YMLFileUtils.makeYAMLFile(
                    "/languages/${currentLanguage.name.lowercase(Locale.getDefault())}.yml",
                    languageMap
                )
                FileUtils.saveFile(
                    "/languages/${currentLanguage.name.lowercase(Locale.getDefault())}.yml",
                    ymlConfig
                )
            }
        }
    }

    data class RegisteredString(val file: FileTypes, val key: String, val value: String)
}


enum class FileTypes {
    EN,
    DE,
}
