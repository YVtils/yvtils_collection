/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.gui.language

import yv.tils.configv2.language.BuildLanguage
import yv.tils.configv2.language.FileTypes

object RegisterStrings {
    fun registerStrings() {
        register(
            "action.gui.cancelled",
            FileTypes.EN to "<prefix> <white>Action has been <yellow>cancelled<white>!",
            FileTypes.DE to "<prefix> <white>Aktion wurde <yellow>abgebrochen<white>!"
        )

        register(
            "action.gui.itemAdded",
            FileTypes.EN to "<prefix> <white>Item <yellow><item> <white>has been <yellow>added<white>!",
            FileTypes.DE to "<prefix> <white>Item <yellow><item> <white>wurde <yellow>hinzugefügt<white>!"
        )

        register(
            "action.gui.invalidItem",
            FileTypes.EN to "<prefix> <white><item> is not a valid item!",
            FileTypes.DE to "<prefix> <white><item> ist kein bekanntes Item!"
        )

        register(
            "action.gui.configSaved",
            FileTypes.EN to "<prefix> <white>Config saved for <yellow><config><white>.",
            FileTypes.DE to "<prefix> <white>Konfiguration für <yellow><config> <white>gespeichert."
        )

        register(
            "action.gui.configSaveFailed",
            FileTypes.EN to "<prefix> <red>Failed to save config for <yellow><config><red>: <white><error>",
            FileTypes.DE to "<prefix> <red>Fehler beim Speichern der Konfiguration für <yellow><config><red>: <white><error>"
        )

        register(
            "action.gui.valueInfo",
            FileTypes.EN to "<prefix> <white>Value: <green><value>",
            FileTypes.DE to "<prefix> <white>Wert: <green><value>"
        )

        // GUI lore labels
        register(
            "action.gui.lore.value",
            FileTypes.EN to "Value",
            FileTypes.DE to "Wert"
        )

        register(
            "action.gui.lore.default",
            FileTypes.EN to "Default",
            FileTypes.DE to "Standard"
        )

        register(
            "action.gui.lore.controls.boolean",
            FileTypes.EN to "<gray>Left-Click: <white>Toggle",
            FileTypes.DE to "<gray>Linksklick: <white>Umschalten"
        )

        register(
            "action.gui.lore.controls.number",
            FileTypes.EN to "<gray>Left-Click: <white>+1  <gray>Shift+Left: <white>+10<newline><gray>Right-Click: <white>-1  <gray>Shift+Right: <white>-10",
            FileTypes.DE to "<gray>Linksklick: <white>+1  <gray>Shift+Links: <white>+10<newline><gray>Rechtsklick: <white>-1  <gray>Shift+Rechts: <white>-10"
        )

        register(
            "action.gui.lore.controls.text",
            FileTypes.EN to "<gray>Left-Click: <white>Edit value",
            FileTypes.DE to "<gray>Linksklick: <white>Wert bearbeiten"
        )

        register(
            "action.gui.lore.controls.list",
            FileTypes.EN to "<gray>Left-Click: <white>Open list editor",
            FileTypes.DE to "<gray>Linksklick: <white>Listen-Editor öffnen"
        )

        register(
            "action.gui.lore.controls.nested",
            FileTypes.EN to "<gray>Left-Click: <white>Open",
            FileTypes.DE to "<gray>Linksklick: <white>Öffnen"
        )

        register(
            "action.gui.lore.list.remove",
            FileTypes.EN to "<gray>Right-Click: <red>Remove",
            FileTypes.DE to "<gray>Rechtsklick: <red>Entfernen"
        )

        // Navigation
        register(
            "action.gui.nav.previousPage",
            FileTypes.EN to "<yellow>Previous page",
            FileTypes.DE to "<yellow>Vorherige Seite"
        )

        register(
            "action.gui.nav.nextPage",
            FileTypes.EN to "<yellow>Next page",
            FileTypes.DE to "<yellow>Nächste Seite"
        )

        register(
            "action.gui.nav.back",
            FileTypes.EN to "<yellow>Back",
            FileTypes.DE to "<yellow>Zurück"
        )

        register(
            "action.gui.nav.confirm",
            FileTypes.EN to "<green>Confirm",
            FileTypes.DE to "<green>Bestätigen"
        )

        register(
            "action.gui.nav.addItem",
            FileTypes.EN to "<green>Add item",
            FileTypes.DE to "<green>Item hinzufügen"
        )

        register(
            "action.gui.enterValue.prompt",
            FileTypes.EN to "<prefix> <white>Enter the new value in the anvil above.",
            FileTypes.DE to "<prefix> <white>Gib den neuen Wert im Amboss oben ein."
        )

        register(
            "action.gui.enterValue.promptList",
            FileTypes.EN to "<prefix> <green>Type a block Material name (e.g. <white>OAK_LOG<green>) and confirm.",
            FileTypes.DE to "<prefix> <green>Gib einen Block-Material-Namen ein (z.B. <white>OAK_LOG<green>) und bestätige."
        )
    }

    private fun register(langKey: String, vararg translations: Pair<FileTypes, String>) {
        translations.forEach { (fileType, value) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(fileType, langKey, value))
        }
    }
}
