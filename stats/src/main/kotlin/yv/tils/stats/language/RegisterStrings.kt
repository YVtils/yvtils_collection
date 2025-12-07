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

package yv.tils.stats.language

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
    /**
     * Language string keys for the stats module.
     */
    enum class LangStrings(val key: String) {
        OPTIN_ACCEPTED("stats.optin.accepted"),
        OPTIN_DECLINED("stats.optin.declined"),
        OPTIN_ALREADY_SET("stats.optin.already_set"),
        OPTIN_NO_PERMISSION("stats.optin.no_permission"),
        STATS_EXPORTED("stats.exported"),
        STATS_EXPORT_FAILED("stats.export_failed"),
        STATS_CLEARED("stats.cleared"),
        STATS_SAVED("stats.saved"),
        STATS_PUSH_SUCCESS("stats.push.success"),
        STATS_PUSH_FAILED("stats.push.failed"),
    }

    fun registerStrings() {
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
