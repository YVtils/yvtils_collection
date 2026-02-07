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

package yv.tils.config.language

import yv.tils.config.files.FileUtils
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import java.util.*

class Language {
    companion object {
        var langStrings = mutableMapOf<String, LangString>()
        var validLanguages = mutableSetOf<String>()
    }

    /**
     * Represents a language string.
     * @param key The key of the string.
     * @param value The value of the string, split by language.
     */
    data class LangString(
        val key: String,
        val value: MutableMap<String, String>
    )

    fun loadLanguageFiles() {
        Logger.debug("Loading language files...")
        BuildLanguage.buildFiles()

        FileUtils.loadYAMLFilesFromFolder("/languages").forEach { file ->
            Logger.debug("Loading language file: ${file.file.nameWithoutExtension}")

            val tempLangStrings = langStrings
            val lang = file.file.nameWithoutExtension
            validLanguages.add(lang)

            // Get all keys
            val allKeys = file.content.getKeys(true)

            // Filter to only keep the deepest keys
            val deepestKeys = allKeys.filter { key ->
                allKeys.none { it.startsWith("$key.") }
            }

            // Process only deepest keys
            deepestKeys.forEach { key ->
                Logger.debug("Processing key: $key", DEBUGLEVEL.SPAM)
                val string = tempLangStrings[key]

                if (string == null) {
                    tempLangStrings[key] = LangString(key, mutableMapOf())
                }

                tempLangStrings[key]!!.value[lang] = file.content.getString(key)!!
            }

            langStrings = tempLangStrings
        }
    }

    fun localeToLanguage(locale: Locale): String {
        Logger.debug("Converting locale to language: $locale")
        return locale.country + "_" + locale.language
    }

    fun localeToShortLanguage(locale: Locale): String {
        Logger.debug("Converting locale to short language: $locale")
        return locale.language
    }

    fun stringToLocale(language: String): Locale {
        Logger.debug("Converting string to locale: $language")
        return if (language.contains("_")) {
            val parts = language.split("_")
            Locale.of(parts[1], parts[0])
        } else {
            Locale.of(language)
        }
    }
}
