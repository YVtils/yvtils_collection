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

package yv.tils.server.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes
import yv.tils.utils.colors.Colors

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "event.player.join",
            mapOf(
                FileTypes.EN to "<${Colors.GREEN.color}>» <white><player>",
                FileTypes.DE to "<${Colors.GREEN.color}>» <white><player>",
            )
        )

        registerNewString(
            "event.player.quit",
            mapOf(
                FileTypes.EN to "<${Colors.RED.color}>« <white><player>",
                FileTypes.DE to "<${Colors.RED.color}>« <white><player>",
            )
        )
    }

    private fun registerNewString(langKey: String, translations: Map<FileTypes, String>) {
        for ((fileType, value) in translations) {
            registerString(langKey, value, fileType)
        }
    }

    private fun registerString(key: String, value: String, file: FileTypes) {
        BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
    }
}
