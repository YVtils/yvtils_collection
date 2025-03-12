package yv.tils.essentials.language

import language.BuildLanguage
import language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.missing.player",
            mapOf(
                FileTypes.EN to "<prefix> <red>To execute this command here, a player must be specified!",
                FileTypes.DE to "<prefix> <red>Um diesen Command hier auszuführen, musst ein Spieler angeben werden!"
            )
        )
        registerNewString(
            "command.fly.enable.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>You can <green>now <gray>fly!",
                FileTypes.DE to "<prefix> <gray>Du kannst <green>nun <gray>fliegen!"
            )
        )
        registerNewString(
            "command.fly.enable.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player> can <green>now <gray>fly!",
                FileTypes.DE to "<prefix> <gray><player> kann <green>nun <gray>fliegen!"
            )
        )
        registerNewString(
            "command.fly.disable.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>You can <red>no longer <gray>fly!",
                FileTypes.DE to "<prefix> <gray>Du kannst nun <red>nicht mehr <gray>fliegen!"
            )
        )
        registerNewString(
            "command.fly.disable.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player> can <red>no longer <gray>fly!",
                FileTypes.DE to "<prefix> <gray><player> kann nun <red>nicht mehr <gray>fliegen!"
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
