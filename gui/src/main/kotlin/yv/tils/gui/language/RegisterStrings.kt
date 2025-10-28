package yv.tils.gui.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

object RegisterStrings {
    fun registerStrings() {
        register("action.gui.cancelled",
            FileTypes.EN to "<prefix> <white>Action has been <yellow>cancelled<white>!",
            FileTypes.DE to "<prefix> <white>Aktion wurde <yellow>abgebrochen<white>!"
        )

        register("action.gui.itemAdded",
            FileTypes.EN to "<prefix> <white>Item <yellow><item> <white>has been <yellow>added<white>!",
            FileTypes.DE to "<prefix> <white>Item <yellow><item> <white>wurde <yellow>hinzugefügt<white>!"
        )

        register("action.gui.invalidItem",
            FileTypes.EN to "<prefix> <white><item> is not a valid item!",
            FileTypes.DE to "<prefix> <white><item> ist kein bekanntes Item!"
        )

        register("action.gui.enterValue.prompt",
            FileTypes.EN to "<prefix> <white>Please enter a new value in chat. Type <yellow><click:suggest_command:'cancel'>cancel</click> <white>to cancel.",
            FileTypes.DE to "<prefix> <white>Bitte gib einen neuen Wert im Chat ein. Gib <yellow><click:suggest_command:'cancel'>cancel</click> <white>ein, um abzubrechen."
        )

        register("action.gui.enterValue.promptList",
            FileTypes.EN to "<prefix> <green>Type block Material name to add (e.g. <white>OAK_LOG<green>)\n<yellow>Type <red>cancel<yellow> to abort.",
            FileTypes.DE to "<prefix> <green>Gib einen Block-Material-Namen ein (z.B. <white>OAK_LOG<green>)\n<yellow>Gib <red>cancel<yellow> ein, um abzubrechen."
        )

        register("action.gui.configSaved",
            FileTypes.EN to "<prefix> <white>Config saved for <yellow><config><white>.",
            FileTypes.DE to "<prefix> <white>Konfiguration für <yellow><config> <white>gespeichert."
        )

        register("action.gui.configSaveFailed",
            FileTypes.EN to "<prefix> <red>Failed to save config for <yellow><config><red>: <white><error>",
            FileTypes.DE to "<prefix> <red>Fehler beim Speichern der Konfiguration für <yellow><config><red>: <white><error>"
        )

        register("action.gui.settingInfo",
            FileTypes.EN to "<prefix> <white>Setting <yellow><key><white>: <green><value>",
            FileTypes.DE to "<prefix> <white>Einstellung <yellow><key><white>: <green><value>"
        )

        register("action.gui.valueInfo",
            FileTypes.EN to "<prefix> <white>Value: <green><value>",
            FileTypes.DE to "<prefix> <white>Wert: <green><value>"
        )
    }

    private fun register(langKey: String, vararg translations: Pair<FileTypes, String>) {
        translations.forEach { (fileType, value) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(fileType, langKey, value))
        }
    }
}
