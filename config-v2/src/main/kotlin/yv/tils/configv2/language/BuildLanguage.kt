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

import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.YamlFileUtils
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Collects every translation string registered via [LanguageProvider] and
 * writes them out to `/languages/<lang>.yml`.
 *
 * [buildFiles] is simpler than the original module's version: that one
 * already computed `registeredStrings.groupBy { it.file }` but then
 * re-derived the same grouping by hand with a mutable `currentLanguage`
 * variable and a "flush when the file type changes" state machine. Since
 * `groupBy` already produces exactly the grouping needed, iterating it
 * directly removes ~30 lines with no behavior change.
 *
 * Registered keys may contain dots (e.g. `"action.gui.configSaved"`), which
 * must nest rather than become a literal dotted top-level key - see
 * [ConfigEntryFileUtils][yv.tils.configv2.data.ConfigEntryFileUtils]'s class
 * doc for why Configurate needs this handled explicitly.
 */
class BuildLanguage {
    companion object {
        private val registeredStrings = CopyOnWriteArrayList<RegisteredString>()

        fun registerString(registeredString: RegisteredString) {
            registeredStrings.add(registeredString)

            Logger.debug(
                "Registered string: ${registeredString.key} -> ${registeredString.value} in ${registeredString.file.name}",
                DEBUG_LEVEL.SPAM
            )
            Logger.debug("Registered strings: ${registeredStrings.size}")
        }

        fun buildFiles() {
            Logger.debug("Building language files...")

            registeredStrings.groupBy { it.file }.forEach { (fileType, strings) ->
                Logger.debug("Processing file type: ${fileType.name}")

                val path = "/languages/${fileType.name.lowercase(Locale.ROOT)}.yml"
                val configFile = YamlFileUtils.makeYamlFile(path, emptyMap())

                for (registeredString in strings) {
                    Logger.debug(
                        "Registering string: ${registeredString.key} -> ${registeredString.value} in ${fileType.name}",
                        DEBUG_LEVEL.SPAM
                    )

                    configFile.node.node(*registeredString.key.split(".").toTypedArray())
                        .raw(registeredString.value)
                }

                ConfigurateFileUtils.save(configFile)
            }
        }
    }

    data class RegisteredString(val file: FileTypes, val key: String, val value: String)
}
