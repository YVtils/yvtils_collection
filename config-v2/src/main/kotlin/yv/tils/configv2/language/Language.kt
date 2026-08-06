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

package yv.tils.configv2.language

import org.spongepowered.configurate.ConfigurationNode
import yv.tils.configv2.files.YamlFileUtils
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Language {
    companion object {
        /**
         * `dotted.key -> LangString`. A [ConcurrentHashMap] (unlike the
         * original module's plain `mutableMapOf`) since [LanguageHandler]
         * reads from this on player-facing threads (chat/commands) while a
         * reload could in principle be rebuilding it concurrently.
         */
        val langStrings = ConcurrentHashMap<String, LangString>()

        val validLanguages: MutableSet<String> = ConcurrentHashMap.newKeySet()

        /**
         * Was an instance method for no reason in the original module -
         * these don't touch any instance state, so they're plain companion
         * functions here instead of requiring a throwaway `Language()`.
         */
        fun localeToLanguage(locale: Locale): String {
            Logger.debug("Converting locale to language: $locale")
            return "${locale.country}_${locale.language}"
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

        /**
         * Flattens a nested map-of-maps [node] into `"a.b.c" -> value`
         * entries for every leaf (non-map) node - the Configurate-based
         * replacement for the original module's `getKeys(true)` +
         * "keep only the deepest keys" pass over a Bukkit
         * `YamlConfiguration`.
         */
        private fun flatten(node: ConfigurationNode, prefix: String = ""): Map<String, String> {
            if (!node.isMap) {
                val value = node.string ?: return emptyMap()
                return mapOf(prefix to value)
            }

            val result = mutableMapOf<String, String>()
            node.childrenMap().forEach { (key, child) ->
                val path = if (prefix.isEmpty()) key.toString() else "$prefix.$key"
                result.putAll(flatten(child, path))
            }
            return result
        }
    }

    /**
     * Represents a language string.
     * @param key The key of the string.
     * @param value The value of the string, split by language.
     */
    data class LangString(
        val key: String,
        val value: MutableMap<String, String> = ConcurrentHashMap(),
    )

    fun loadLanguageFiles() {
        Logger.debug("Loading language files...")
        BuildLanguage.buildFiles()

        YamlFileUtils.loadYamlFilesFromFolder("/languages").forEach { file ->
            val lang = file.file.nameWithoutExtension
            Logger.debug("Loading language file: $lang")
            validLanguages.add(lang)

            flatten(file.node).forEach { (key, value) ->
                Logger.debug("Processing key: $key", DEBUG_LEVEL.SPAM)
                langStrings.getOrPut(key) { LangString(key) }.value[lang] = value
            }
        }
    }
}
