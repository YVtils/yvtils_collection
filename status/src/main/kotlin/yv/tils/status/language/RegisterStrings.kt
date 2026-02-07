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

package yv.tils.status.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.status.set",
            mapOf(
                FileTypes.EN to "<prefix> <white>New status: <status>",
                FileTypes.DE to "<prefix> <white>Neuer Status: <status>",
            )
        )

        registerNewString(
            "command.status.clear.cleared.self",
            mapOf(
                FileTypes.EN to "<prefix> <white>You successfully cleared your status!",
                FileTypes.DE to "<prefix> <white>Du hast deinen Status erfolgreich gelöscht!"
            )
        )

        registerNewString(
            "command.status.clear.cleared.other",
            mapOf(
                FileTypes.EN to "<prefix> <white>You successfully cleared the status of <yellow><player> <white>!",
                FileTypes.DE to "<prefix> <white>Du hast den Status von <yellow><player> <white>erfolgreich gelöscht!",
            )
        )

        registerNewString(
            "command.status.clear.notAllowed",
            mapOf(
                FileTypes.EN to "<prefix> <white>You are not allowed to clear the status of others!",
                FileTypes.DE to "<prefix> <white>Du darfst den Status von anderen nicht löschen!"
            )
        )

        registerNewString(
            "command.status.input.invalid",
            mapOf(
                FileTypes.EN to "<prefix> <white>Invalid status!",
                FileTypes.DE to "<prefix> <white>Ungültiger Status!"
            )
        )

        registerNewString(
            "command.status.input.tooLong",
            mapOf(
                FileTypes.EN to "<prefix> <white>Your status is too long! <gray>(max. <maxLength>)",
                FileTypes.DE to "<prefix> <white>Dein Status ist zu lang! <gray>(max. <maxLength>)"
            )
        )

        registerNewString(
            "command.status.default.notFound",
            mapOf(
                FileTypes.EN to "<prefix> <white>Status <status> not found!",
                FileTypes.DE to "<prefix> <white>Status <status> nicht gefunden!"
            )
        )

        registerNewString(
            "status.server.join",
            mapOf(
                FileTypes.EN to "<prefix> <gold>Welcome back!<newline><gray>Current status: <white><status>",
                FileTypes.DE to "<prefix> <gold>Willkommen zurück!<newline><gray>Aktueller Status: <white><status>"
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
