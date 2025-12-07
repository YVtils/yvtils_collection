/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

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
            FileTypes.EN to "<prefix> <white>Please enter a new value in chat. Type <red><click:suggest_command:'cancel'>cancel</click> <white>to cancel.",
            FileTypes.DE to "<prefix> <white>Bitte gib einen neuen Wert im Chat ein. Gib <red><click:suggest_command:'cancel'>cancel</click> <white>ein, um abzubrechen."
        )

        register("action.gui.enterValue.promptList",
            FileTypes.EN to "<prefix> <green>Type block Material name to add (e.g. <white>OAK_LOG<green>) <white>Type <red><click:suggest_command:'cancel'>cancel</click><yellow> to abort.",
            FileTypes.DE to "<prefix> <green>Gib einen Block-Material-Namen ein (z.B. <white>OAK_LOG<green>) <white>Gib <red><click:suggest_command:'cancel'>cancel</click><yellow> ein, um abzubrechen."
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

        // ClickAction descriptions
        register("action.gui.click.openSetting",
            FileTypes.EN to "Open settings menu",
            FileTypes.DE to "Einstellungsmenü öffnen"
        )

        register("action.gui.click.toggleOption",
            FileTypes.EN to "Toggle an option",
            FileTypes.DE to "Option umschalten"
        )

        register("action.gui.click.incrementValue",
            FileTypes.EN to "Increment a value",
            FileTypes.DE to "Wert erhöhen"
        )

        register("action.gui.click.incrementValueShift",
            FileTypes.EN to "Increment a value by 10",
            FileTypes.DE to "Wert um 10 erhöhen"
        )

        register("action.gui.click.decrementValue",
            FileTypes.EN to "Decrement a value",
            FileTypes.DE to "Wert verringern"
        )

        register("action.gui.click.decrementValueShift",
            FileTypes.EN to "Decrement a value by 10",
            FileTypes.DE to "Wert um 10 verringern"
        )

        register("action.gui.click.modifyText",
            FileTypes.EN to "Modify text value",
            FileTypes.DE to "Text ändern"
        )

        // ClickAction names
        register("action.gui.click.name.openSetting",
            FileTypes.EN to "Right-Click",
            FileTypes.DE to "Rechtsklick"
        )

        register("action.gui.click.name.toggleOption",
            FileTypes.EN to "Left-Click",
            FileTypes.DE to "Linksklick"
        )

        register("action.gui.click.name.incrementValue",
            FileTypes.EN to "Left-Click",
            FileTypes.DE to "Linksklick"
        )

        register("action.gui.click.name.incrementValueShift",
            FileTypes.EN to "Shift + Left-Click",
            FileTypes.DE to "Shift + Linksklick"
        )

        register("action.gui.click.name.decrementValue",
            FileTypes.EN to "Right-Click",
            FileTypes.DE to "Rechtsklick"
        )

        register("action.gui.click.name.decrementValueShift",
            FileTypes.EN to "Shift + Right-Click",
            FileTypes.DE to "Shift + Rechtsklick"
        )

        register("action.gui.click.name.modifyText",
            FileTypes.EN to "Left-Click",
            FileTypes.DE to "Linksklick"
        )

        // GUI lore labels
        register("action.gui.lore.value",
            FileTypes.EN to "Value",
            FileTypes.DE to "Wert"
        )

        register("action.gui.lore.default",
            FileTypes.EN to "Default",
            FileTypes.DE to "Standard"
        )

        register("action.gui.lore.actions",
            FileTypes.EN to "Actions",
            FileTypes.DE to "Aktionen"
        )

        // Navigation
        register("action.gui.nav.previousPage",
            FileTypes.EN to "<yellow>Previous page",
            FileTypes.DE to "<yellow>Vorherige Seite"
        )

        register("action.gui.nav.nextPage",
            FileTypes.EN to "<yellow>Next page",
            FileTypes.DE to "<yellow>Nächste Seite"
        )

        register("action.gui.nav.quit",
            FileTypes.EN to "<red>Quit",
            FileTypes.DE to "<red>Beenden"
        )

        register("action.gui.nav.addItem",
            FileTypes.EN to "<green>Add item",
            FileTypes.DE to "<green>Item hinzufügen"
        )

        // List item actions
        register("action.gui.list.actions",
            FileTypes.EN to "Actions",
            FileTypes.DE to "Aktionen"
        )

        register("action.gui.list.remove",
            FileTypes.EN to "Right-click: <red>Remove",
            FileTypes.DE to "Rechtsklick: <red>Entfernen"
        )
    }

    private fun register(langKey: String, vararg translations: Pair<FileTypes, String>) {
        translations.forEach { (fileType, value) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(fileType, langKey, value))
        }
    }
}
