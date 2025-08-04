package yv.tils.essentials.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString("gamemode.survival", mapOf(FileTypes.EN to "Survival", FileTypes.DE to "Überleben"))
        registerNewString("gamemode.creative", mapOf(FileTypes.EN to "Creative", FileTypes.DE to "Kreativ"))
        registerNewString("gamemode.adventure", mapOf(FileTypes.EN to "Adventure", FileTypes.DE to "Abenteuer"))
        registerNewString("gamemode.spectator", mapOf(FileTypes.EN to "Spectator", FileTypes.DE to "Zuschauer"))

        registerNewString(
            "globalmute.try_to_write",
            mapOf(
                FileTypes.EN to "<prefix> <red>Global mute is enabled! You can't write messages!",
                FileTypes.DE to "<prefix> <red>Global mute ist aktiviert! Du kannst keine Nachrichten schreiben!"
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

        registerNewString(
            "command.gamemode.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Your gamemode has been set to <green><gamemode><gray>!",
                FileTypes.DE to "<prefix> <gray>Dein Spielmodus wurde zu <green><gamemode> <gray>geändert!"
            )
        )

        registerNewString(
            "command.gamemode.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player>'s gamemode has been set to <green><gamemode><gray>!",
                FileTypes.DE to "<prefix> <gray>Der Spielmodus von <player> wurde zu <green><gamemode> <gray>geändert!"
            )
        )

        registerNewString(
            "command.globalmute.already",
            mapOf(
                FileTypes.EN to "<prefix> <red>Global mute is already in this state!",
                FileTypes.DE to "<prefix> <red>Global mute ist bereits in diesem Zustand!"
            )
        )

        registerNewString(
            "command.globalmute.enable",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Global mute has been <green>enabled<gray>!",
                FileTypes.DE to "<prefix> <gray>Global mute wurde <green>aktiviert<gray>!"
            )
        )

        registerNewString(
            "command.globalmute.disable",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Global mute has been <red>disabled<gray>!",
                FileTypes.DE to "<prefix> <gray>Global mute wurde <red>deaktiviert<gray>!"
            )
        )

        registerNewString(
            "command.god.enable.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>You are now <green>invulnerable<gray>!",
                FileTypes.DE to "<prefix> <gray>Du bist nun <green>unverwundbar<gray>!"
            )
        )

        registerNewString(
            "command.god.enable.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player> is now <green>invulnerable<gray>!",
                FileTypes.DE to "<prefix> <gray><player> ist nun <green>unverwundbar<gray>!"
            )
        )

        registerNewString(
            "command.god.disable.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>You are no longer <red>invulnerable<gray>!",
                FileTypes.DE to "<prefix> <gray>Du bist nun <red>nicht mehr <gray>unverwundbar!"
            )
        )

        registerNewString(
            "command.god.disable.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player> is no longer <red>invulnerable<gray>!",
                FileTypes.DE to "<prefix> <gray><player> ist nun <red>nicht mehr <gray>unverwundbar!"
            )
        )

        registerNewString(
            "command.heal.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>You have been <green>healed<gray>!",
                FileTypes.DE to "<prefix> <gray>Du wurdest <green>geheilt<gray>!"
            )
        )

        registerNewString(
            "command.heal.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player> has been <green>healed<gray>!",
                FileTypes.DE to "<prefix> <gray><player> wurde <green>geheilt<gray>!"
            )
        )

        registerNewString(
            "command.ping.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Your ping is <green><ping><gray>ms!",
                FileTypes.DE to "<prefix> <gray>Dein Ping ist <green><ping><gray>ms!"
            )
        )

        registerNewString(
            "command.ping.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player>'s ping is <green><ping><gray>ms!",
                FileTypes.DE to "<prefix> <gray>Der Ping von <player> ist <green><ping><gray>ms!"
            )
        )

        registerNewString(
            "command.speed.change.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Your speed has been set to <green><speed><gray>!",
                FileTypes.DE to "<prefix> <gray>Deine Geschwindigkeit wurde auf <green><speed><gray> gesetzt!"
            )
        )

        registerNewString(
            "command.speed.change.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player>'s speed has been set to <green><speed><gray>!",
                FileTypes.DE to "<prefix> <gray>Die Geschwindigkeit von <player> wurde auf <green><speed><gray> gesetzt!"
            )
        )

        registerNewString(
            "command.speed.reset.self",
            mapOf(
                FileTypes.EN to "<prefix> <gray>Your speed has been reset!",
                FileTypes.DE to "<prefix> <gray>Deine Geschwindigkeit wurde zurückgesetzt!"
            )
        )

        registerNewString(
            "command.speed.reset.other",
            mapOf(
                FileTypes.EN to "<prefix> <gray><player>'s speed has been reset!",
                FileTypes.DE to "<prefix> <gray>Die Geschwindigkeit von <player> wurde zurückgesetzt!"
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
