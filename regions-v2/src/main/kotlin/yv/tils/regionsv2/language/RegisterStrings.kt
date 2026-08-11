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

package yv.tils.regionsv2.language

import yv.tils.configv2.language.BuildLanguage
import yv.tils.configv2.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.regionsv2.example",
            mapOf(
                FileTypes.EN to "<prefix> <white>Hello from regions-v2!",
                FileTypes.DE to "<prefix> <white>Hallo von regions-v2!",
            )
        )

        registerNewString(
            "command.regionsv2.worldguard.none",
            mapOf(
                FileTypes.EN to "<prefix> <white>Hooked into WorldGuard - no regions at your current location.",
                FileTypes.DE to "<prefix> <white>Mit WorldGuard verbunden - an deiner Position gibt es keine Regionen.",
            )
        )

        registerNewString(
            "command.regionsv2.worldguard.found",
            mapOf(
                FileTypes.EN to "<prefix> <white>Regions at your location: <regions>",
                FileTypes.DE to "<prefix> <white>Regionen an deiner Position: <regions>",
            )
        )
    }

    private fun registerNewString(langKey: String, translations: Map<FileTypes, String>) {
        translations.forEach { (fileType, value) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(fileType, langKey, value))
        }
    }
}
