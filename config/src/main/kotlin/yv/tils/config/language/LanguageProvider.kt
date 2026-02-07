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

class LanguageProvider {
    companion object {
        fun registerNewString(langKey: LangStrings, translations: Map<FileTypes, String>) {
            for ((fileType, value) in translations) {
                registerString(langKey.key, value, fileType)
            }
        }

        fun registerString(key: String, value: String, file: FileTypes) {
            BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
        }
    }

    interface LangStrings {
        val key: String
    }

    interface RegisterStrings {
        fun registerStrings()
    }
}
