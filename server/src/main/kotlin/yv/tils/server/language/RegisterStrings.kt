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

package yv.tils.server.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes
import yv.tils.utils.colors.Colors

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "maintenance.player.join.unallowed",
            mapOf(
                FileTypes.EN to "<prefix> <red>The server is currently under maintenance. Please try again later.",
                FileTypes.DE to "<prefix> <red>Der Server befindet sich derzeit im Wartungsmodus. Bitte versuche es später erneut.",
            )
        )
        registerNewString(
            "maintenance.state.on",
            mapOf(FileTypes.EN to "<red>enabled</red>", FileTypes.DE to "<red>aktiviert</red>")
        )
        registerNewString(
            "maintenance.state.off",
            mapOf(FileTypes.EN to "<green>disabled</green>", FileTypes.DE to "<green>deaktiviert</green>")
        )
        registerNewString(
            "maintenance.announcement.global",
            mapOf(
                FileTypes.EN to "<prefix> Maintenance mode is now <state>.",
                FileTypes.DE to "<prefix> Der Wartungsmodus ist jetzt <state>.",
            )
        )
        registerNewString(
            "maintenance.announcement.sender.changed",
            mapOf(
                FileTypes.EN to "<prefix> Maintenance mode has been <state>.",
                FileTypes.DE to "<prefix> Der Wartungsmodus wurde <state>.",
            )
        )
        registerNewString(
            "maintenance.announcement.sender.already",
            mapOf(
                FileTypes.EN to "<prefix> Maintenance mode is already <state>.",
                FileTypes.DE to "<prefix> Der Wartungsmodus ist bereits <state>.",
            )
        )

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
