package language

import files.FileUtils
import logger.Logger
import java.util.Locale

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
                Logger.debug("Processing key: $key")
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
}