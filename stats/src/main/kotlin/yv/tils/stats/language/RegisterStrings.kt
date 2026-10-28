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

package yv.tils.stats.language

import net.kyori.adventure.text.Component
import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

/**
 * Language strings for the stats module.
 *
 * Provides localized messages for:
 * - Opt-in prompts and responses
 * - Status messages
 * - Error messages
 */
class RegisterStrings {
    companion object {
        /** Also space out older saved translations without overwriting the language files. */
        internal fun formatOptInPrompt(message: Component): Component = message.replaceText { replacement ->
            replacement.match("(?<=[.!?])[ \\t]+").replacement(Component.newline())
        }
    }

    /**
     * Language string keys for the stats module.
     */
    enum class LangStrings(val key: String) {
        OPTIN_PROMPT("stats.optin.prompt"),
        OPTIN_ACCEPTED("stats.optin.accepted"),
        OPTIN_DECLINED("stats.optin.declined"),
        OPTIN_ALREADY_SET("stats.optin.already_set"),
        OPTIN_NO_PERMISSION("stats.optin.no_permission"),
        CONFIG_SAVE_FAILED("stats.config.save_failed"),
        GUI_TITLE("stats.gui.title"),
        GUI_SETTINGS("stats.gui.settings"),
        GUI_ACCEPT("stats.gui.accept"),
        GUI_DECLINE("stats.gui.decline"),
        GUI_ENABLED("stats.gui.enabled"),
        GUI_DISABLED("stats.gui.disabled"),
        GUI_UNDECIDED("stats.gui.undecided"),
        GUI_PRIVACY("stats.gui.privacy"),
        STATS_EXPORTED("stats.exported"),
        STATS_EXPORT_FAILED("stats.export_failed"),
        STATS_CLEARED("stats.cleared"),
        STATS_SAVED("stats.saved"),
        STATS_PUSH_SUCCESS("stats.push.success"),
        STATS_PUSH_FAILED("stats.push.failed"),
    }

    fun registerStrings() {
        registerNewString(
            LangStrings.OPTIN_PROMPT.key,
            mapOf(
                FileTypes.EN to "<gold>[YVtils Stats]</gold><newline><newline><white>Optional usage telemetry sends server/plugin versions, loaded YVtils modules,<newline>module-published usage stats, a random installation ID and (optionally) player count<newline>to https://api.yvtils.net/stats.<newline><newline>Default metrics contain no player names, player UUIDs or world seeds.<newline>The receiving service can see your connection IP.<newline>Custom metric authors must avoid personal data.<newline>Nothing is sent before acceptance.<newline><newline>Choose below or open /yvtilsstats (also /yvtils config stats).<newline>Consent and settings can be changed in game at any time; no restart is needed.<newline></white>",
                FileTypes.DE to "<gold>[YVtils Stats]</gold><newline><newline><white>Die optionale Nutzungsstatistik sendet Server-/Plugin-Versionen, geladene YVtils-Module,<newline>von Modulen veröffentlichte Nutzungsdaten, eine zufällige Installations-ID<newline>und (optional) die Spieleranzahl an https://api.yvtils.net/stats.<newline><newline>Standardmetriken enthalten keine Spielernamen, Spieler-UUIDs oder Welt-Seeds.<newline>Der Empfänger sieht die Verbindungs-IP.<newline>Eigene Metriken dürfen keine persönlichen Daten enthalten.<newline>Vor der Zustimmung wird nichts gesendet.<newline><newline>Wähle unten oder öffne /yvtilsstats (auch /yvtils config stats).<newline>Zustimmung und Einstellungen können jederzeit im Spiel geändert werden; kein Neustart nötig.<newline></white>",
            ),
        )

        registerNewString(LangStrings.CONFIG_SAVE_FAILED.key, mapOf(
            FileTypes.EN to "<prefix> <red>Could not save stats settings. Check the console and storage permissions. Withdrawal still applies now, but may not survive restart until saving succeeds.",
            FileTypes.DE to "<prefix> <red>Statistik-Einstellungen konnten nicht gespeichert werden. Prüfe Konsole und Schreibrechte. Ein Widerruf gilt jetzt trotzdem, ist aber erst nach erfolgreichem Speichern neustartfest.",
        ))
        registerNewString(LangStrings.GUI_TITLE.key, mapOf(FileTypes.EN to "Usage statistics", FileTypes.DE to "Nutzungsstatistik"))
        registerNewString(LangStrings.GUI_SETTINGS.key, mapOf(FileTypes.EN to "<aqua>[Settings]", FileTypes.DE to "<aqua>[Einstellungen]"))
        registerNewString(LangStrings.GUI_ACCEPT.key, mapOf(FileTypes.EN to "<green>[Accept telemetry]", FileTypes.DE to "<green>[Telemetrie erlauben]"))
        registerNewString(LangStrings.GUI_DECLINE.key, mapOf(FileTypes.EN to "<red>[Decline / withdraw]", FileTypes.DE to "<red>[Ablehnen / widerrufen]"))
        registerNewString(LangStrings.GUI_ENABLED.key, mapOf(FileTypes.EN to "<green>Telemetry enabled", FileTypes.DE to "<green>Telemetrie aktiviert"))
        registerNewString(LangStrings.GUI_DISABLED.key, mapOf(FileTypes.EN to "<red>Telemetry declined", FileTypes.DE to "<red>Telemetrie abgelehnt"))
        registerNewString(LangStrings.GUI_UNDECIDED.key, mapOf(FileTypes.EN to "<yellow>No decision — telemetry off", FileTypes.DE to "<yellow>Noch keine Entscheidung — Telemetrie aus"))
        registerNewString(LangStrings.GUI_PRIVACY.key, mapOf(
            FileTypes.EN to "<gray>Optional YVtils usage telemetry.\n<gray>Versions, modules, module usage stats,\n<gray>random installation ID, optional player count.\n<gray>Sent to https://api.yvtils.net/stats.\n<gray>The receiver sees your connection IP.\n<gray>No default player identities or world seeds.\n<gray>Custom metrics must avoid personal data.\n<gray>Accept explicitly; withdraw here at any time.",
            FileTypes.DE to "<gray>Optionale YVtils-Nutzungstelemetrie.\n<gray>Versionen, Module, Modul-Nutzungsdaten,\n<gray>zufällige ID, optionale Spieleranzahl.\n<gray>Ziel: https://api.yvtils.net/stats.\n<gray>Der Empfänger sieht deine Verbindungs-IP.\n<gray>Standardmäßig keine Spieler-IDs oder Seeds.\n<gray>Eigene Metriken ohne persönliche Daten.\n<gray>Explizit erlauben; Widerruf jederzeit hier.",
        ))

        registerNewString(
            LangStrings.OPTIN_ACCEPTED.key,
            mapOf(
                FileTypes.EN to "<prefix> <green>Thank you! Anonymous stats collection has been enabled.",
                FileTypes.DE to "<prefix> <green>Danke! Die anonyme Statistiksammlung wurde aktiviert.",
            )
        )

        registerNewString(
            LangStrings.OPTIN_DECLINED.key,
            mapOf(
                FileTypes.EN to "<prefix> <yellow>Stats collection has been disabled. You can enable it later in the config.",
                FileTypes.DE to "<prefix> <yellow>Die Statistiksammlung wurde deaktiviert. Du kannst sie später in der Konfiguration aktivieren.",
            )
        )

        registerNewString(
            LangStrings.OPTIN_ALREADY_SET.key,
            mapOf(
                FileTypes.EN to "<prefix> <gray>Stats collection preference has already been set.",
                FileTypes.DE to "<prefix> <gray>Die Statistiksammlung-Einstellung wurde bereits festgelegt.",
            )
        )

        registerNewString(
            LangStrings.OPTIN_NO_PERMISSION.key,
            mapOf(
                FileTypes.EN to "<prefix> <red>You don't have permission to change stats settings.",
                FileTypes.DE to "<prefix> <red>Du hast keine Berechtigung, Statistik-Einstellungen zu ändern.",
            )
        )

        registerNewString(
            LangStrings.STATS_EXPORTED.key,
            mapOf(
                FileTypes.EN to "<prefix> <green>Stats exported successfully.",
                FileTypes.DE to "<prefix> <green>Statistiken erfolgreich exportiert.",
            )
        )

        registerNewString(
            LangStrings.STATS_EXPORT_FAILED.key,
            mapOf(
                FileTypes.EN to "<prefix> <red>Failed to export stats: <error>",
                FileTypes.DE to "<prefix> <red>Fehler beim Exportieren der Statistiken: <error>",
            )
        )

        registerNewString(
            LangStrings.STATS_CLEARED.key,
            mapOf(
                FileTypes.EN to "<prefix> <yellow>All stats have been cleared.",
                FileTypes.DE to "<prefix> <yellow>Alle Statistiken wurden gelöscht.",
            )
        )

        registerNewString(
            LangStrings.STATS_SAVED.key,
            mapOf(
                FileTypes.EN to "<prefix> <green>Stats saved to disk.",
                FileTypes.DE to "<prefix> <green>Statistiken auf Festplatte gespeichert.",
            )
        )

        registerNewString(
            LangStrings.STATS_PUSH_SUCCESS.key,
            mapOf(
                FileTypes.EN to "<prefix> <green>Stats successfully pushed to remote server.",
                FileTypes.DE to "<prefix> <green>Statistiken erfolgreich an Remote-Server gesendet.",
            )
        )

        registerNewString(
            LangStrings.STATS_PUSH_FAILED.key,
            mapOf(
                FileTypes.EN to "<prefix> <red>Failed to push stats to remote server: <error>",
                FileTypes.DE to "<prefix> <red>Fehler beim Senden der Statistiken an Remote-Server: <error>",
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
