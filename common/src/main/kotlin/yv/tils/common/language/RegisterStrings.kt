package yv.tils.common.language

import language.BuildLanguage
import language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            LangStrings.PLUGIN_START,
            mapOf(
                FileTypes.EN to "<prefix> <green>Plugin starts!",
                FileTypes.DE to "<prefix> <green>Plugin startet!"
            )
        )

        registerNewString(
            LangStrings.PLUGIN_STOP,
            mapOf(
                FileTypes.EN to "<prefix> <red>Plugin stops!",
                FileTypes.DE to "<prefix> <red>Plugin stoppt!"
            )
        )

        registerNewString(
            LangStrings.TEXT_COPY,
            mapOf(
                FileTypes.EN to "<gray>Click to copy!",
                FileTypes.DE to "<gray>Klicke um zu kopieren!"
            )
        )

        registerNewString(
            LangStrings.COMMAND_MISSING_PLAYER,
            mapOf(
                FileTypes.EN to "<prefix> <red>To execute this command here, a player must be specified!",
                FileTypes.DE to "<prefix> <red>Um diesen Command hier auszuführen, musst ein Spieler angeben werden!"
            )
        )

        registerNewString(
            LangStrings.COMMAND_EXECUTOR_NOT_PLAYER,
            mapOf(
                FileTypes.EN to "<prefix> <red>This command can only be executed by a player!",
                FileTypes.DE to "<prefix> <red>Dieser Befehl kann nur von einem Spieler ausgeführt werden!",
            )
        )

        registerNewString(
            LangStrings.COMMAND_EXECUTOR_MISSING_PERMISSION,
            mapOf(
                FileTypes.EN to "<prefix> <red>You do not have permission to execute this command!",
                FileTypes.DE to "<prefix> <red>Du hast keine Berechtigung, diesen Befehl auszuführen!"
            )
        )

        registerNewString(
            LangStrings.COMMAND_USAGE,
            mapOf(
                FileTypes.EN to "<prefix> <gray>Usage: <white><command>",
                FileTypes.DE to "<prefix> <gray>Benutze: <white><command>"
            )
        )
    }

    private fun registerNewString(langKey: LangStrings, translations: Map<FileTypes, String>) {
        for ((fileType, value) in translations) {
            registerString(langKey.key, value, fileType)
        }
    }

    private fun registerString(key: String, value: String, file: FileTypes) {
        BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
    }
}

enum class LangStrings(val key: String) {
    PLUGIN_START("plugin.action.start"),
    PLUGIN_STOP("plugin.action.stop"),
    TEXT_COPY("text.action.copy"),
    COMMAND_MISSING_PLAYER("command.missing.player"),
    COMMAND_EXECUTOR_NOT_PLAYER("command.executor.notPlayer"),
    COMMAND_EXECUTOR_MISSING_PERMISSION("command.executor.missingPermission"),
    COMMAND_USAGE("command.usage")
}