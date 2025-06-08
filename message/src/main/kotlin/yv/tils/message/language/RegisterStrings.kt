package yv.tils.message.language

import colors.Colors
import language.BuildLanguage
import language.FileTypes

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
