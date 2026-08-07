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

import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger

class LanguageProvider {
    companion object {
        private val registeredEnumClasses = mutableSetOf<Class<out Enum<*>>>()

        fun registerNewString(langKey: OLDLangStrings, translations: Map<FileTypes, String>) {
            for ((fileType, value) in translations) {
                registerString(langKey.key, value, fileType)
            }
        }

        fun registerString(langKey: LangStrings) {
            for ((fileType, value) in langKey.translations) {
                registerString(langKey.key, value, fileType)
            }
        }

        fun registerString(key: String, value: String, file: FileTypes) {
            BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
        }

        /**
         * Registers all enum values from an enum class that implements LangStrings.
         * This allows automatic registration of language strings from enum declarations.
         *
         * @param enumClass The enum class containing language strings
         */
        fun registerEnumStrings(enumClass: Class<out Enum<*>>) {
            // Check if already registered to avoid duplicates
            if (registeredEnumClasses.contains(enumClass)) {
                Logger.debug("Enum class ${enumClass.simpleName} already registered, skipping", DEBUG_LEVEL.SPAM)
                return
            }

            Logger.debug("Registering enum strings from: ${enumClass.simpleName}")

            // Get all enum constants
            val enumConstants = enumClass.enumConstants

            if (enumConstants.isEmpty()) {
                Logger.debug("No enum constants found in ${enumClass.simpleName}", DEBUG_LEVEL.SPAM)
                return
            }

            // Check if the enum implements LangStrings interface
            if (!LangStrings::class.java.isAssignableFrom(enumClass)) {
                Logger.debug("Enum class ${enumClass.simpleName} does not implement LangStrings interface, skipping")
                return
            }

            var registeredCount = 0
            enumConstants.forEach { enumConstant ->
                try {
                    if (enumConstant is LangStrings) {
                        registerString(enumConstant)
                        registeredCount++
                        Logger.debug("Registered: ${enumConstant.key}", DEBUG_LEVEL.SPAM)
                    }
                } catch (e: Exception) {
                    Logger.debug("Failed to register enum constant ${enumConstant.name}: ${e.message}")
                }
            }

            registeredEnumClasses.add(enumClass)
            Logger.debug("Successfully registered $registeredCount strings from ${enumClass.simpleName}")
        }

        /**
         * Inline helper function to register enum strings with type safety.
         * Usage: LanguageProvider.registerEnumStrings<YourLangStringsEnum>()
         */
        inline fun <reified T> registerEnumStrings() where T : Enum<T>, T : LangStrings {
            registerEnumStrings(T::class.java)
        }
    }

    interface OLDLangStrings {
        val key: String
    }

    interface LangStrings {
        val key: String
        val translations: Map<FileTypes, String>
    }

    interface RegisterStrings {
        fun registerStrings()
    }
}
