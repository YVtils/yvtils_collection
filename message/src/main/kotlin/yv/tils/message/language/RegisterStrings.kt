package yv.tils.message.language

import colors.ColorUtils
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
                FileTypes.EN to "<#${ColorUtils.MAIN.color}>[<#${ColorUtils.SECONDARY.color}>Note<#${ColorUtils.MAIN.color}>]<white> <message>",
                FileTypes.DE to "<#${ColorUtils.MAIN.color}>[<#${ColorUtils.SECONDARY.color}>Notiz<#${ColorUtils.MAIN.color}>]<white> <message>",
            )
        )

        registerNewString(
            "command.msg.message",
            mapOf(
                FileTypes.EN to "<#${ColorUtils.MAIN.color}>[<#${ColorUtils.SECONDARY.color}><sender> <#${ColorUtils.MAIN.color}>-> <#${ColorUtils.SECONDARY.color}><receiver><#${ColorUtils.MAIN.color}>]<white> <message>",
                FileTypes.DE to "<#${ColorUtils.MAIN.color}>[<#${ColorUtils.SECONDARY.color}><sender> <#${ColorUtils.MAIN.color}>-> <#${ColorUtils.SECONDARY.color}><receiver><#${ColorUtils.MAIN.color}>]<white> <message>",
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
