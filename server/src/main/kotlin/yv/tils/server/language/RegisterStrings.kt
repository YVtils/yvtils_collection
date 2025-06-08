package yv.tils.server.language

import colors.Colors
import language.BuildLanguage
import language.FileTypes

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
