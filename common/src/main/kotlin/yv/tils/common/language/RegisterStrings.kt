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

        registerNewString(
            "text.action.copy",
            mapOf(
                FileTypes.EN to "<gray>Click to copy!",
                FileTypes.DE to "<gray>Klicke um zu kopieren!"
            )
        )

        registerNewString(
            "command.missing.player",
            mapOf(
                FileTypes.EN to "<prefix> <red>To execute this command here, a player must be specified!",
                FileTypes.DE to "<prefix> <red>Um diesen Command hier auszuführen, musst ein Spieler angeben werden!"
            )
        )

        registerNewString(
            "command.executor.notPlayer",
            mapOf(
                FileTypes.EN to "<prefix> <white>This command can only be executed by a player!",
                FileTypes.DE to "<prefix> <white>Dieser Befehl kann nur von einem Spieler ausgeführt werden!",
            )
        )

        registerNewString(
            "command.usage",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Usage: <white><command>",
                FileTypes.DE to "<prefix> <gray>Benutze: <white><command>"
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
