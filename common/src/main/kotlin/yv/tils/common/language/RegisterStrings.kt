package yv.tils.common.language

import language.BuildLanguage
import language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "plugin.action.start",
            mapOf(
                FileTypes.EN to "<prefix> <green>Plugin starts!",
                FileTypes.DE to "<prefix> <green>Plugin startet!"
            )
        )

        registerNewString(
            "plugin.action.stop",
            mapOf(
                FileTypes.EN to "<prefix> <red>Plugin stops!",
                FileTypes.DE to "<prefix> <red>Plugin stoppt!"
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
