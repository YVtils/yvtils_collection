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

package yv.tils.message.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes
import yv.tils.utils.colors.Colors

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.reply.noSession",
            mapOf(
                FileTypes.EN to "<prefix> <red>You have no message history!",
                FileTypes.DE to "<prefix> <red>Du hast keinen Nachrichtenverlauf!",
            )
        )

        registerNewString(
            "command.msg.note",
            mapOf(
                FileTypes.EN to "<${Colors.MAIN.color}>[<${Colors.SECONDARY.color}>Note<${Colors.MAIN.color}>]<white> <message>",
                FileTypes.DE to "<${Colors.MAIN.color}>[<${Colors.SECONDARY.color}>Notiz<${Colors.MAIN.color}>]<white> <message>",
            )
        )

        registerNewString(
            "command.msg.message",
            mapOf(
                FileTypes.EN to "<${Colors.MAIN.color}>[<${Colors.SECONDARY.color}><sender> <${Colors.MAIN.color}>-> <${Colors.SECONDARY.color}><receiver><${Colors.MAIN.color}>]<white> <message>",
                FileTypes.DE to "<${Colors.MAIN.color}>[<${Colors.SECONDARY.color}><sender> <${Colors.MAIN.color}>-> <${Colors.SECONDARY.color}><receiver><${Colors.MAIN.color}>]<white> <message>",
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
