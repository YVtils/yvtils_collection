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

package yv.tils.common.language

import yv.tils.configv2.language.BuildLanguage
import yv.tils.configv2.language.FileTypes

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
            LangStrings.PLUGIN_VERSION_UP_TO_DATE,
            mapOf(
                FileTypes.EN to "<prefix> <white>You are using the latest version of the plugin.",
                FileTypes.DE to "<prefix> <white>Du verwendest bereits die neueste Plugin-Version."
            )
        )

        registerNewString(
            LangStrings.PLUGIN_VERSION_OUTDATED_PATCH,
            mapOf(
                FileTypes.EN to "<prefix> <yellow>A patch update is available! <newline><white>Current version: <gray><oldVersion><newline><white>Latest version: <gray><newVersion><newline><white>Download: <gray><link>",
                FileTypes.DE to "<prefix> <yellow>Ein Patch-Update ist verfügbar! <newline><white>Aktuelle Version: <gray><oldVersion><newline><white>Neueste Version: <gray><newVersion><newline><white>Download: <gray><link>"
            )
        )

        registerNewString(
            LangStrings.PLUGIN_VERSION_OUTDATED_MINOR,
            mapOf(
                FileTypes.EN to "<prefix> <#FF8349>A new minor version is available! <newline><white>Current version: <gray><oldVersion><newline><white>Latest version: <gray><newVersion><newline><white>Download: <gray><link>",
                FileTypes.DE to "<prefix> <#FF8349>Eine neue Minor-Version ist verfügbar! <newline><white>Aktuelle Version: <gray><oldVersion><newline><white>Neueste Version: <gray><newVersion><newline><white>Download: <gray><link>"
            )
        )

        registerNewString(
            LangStrings.PLUGIN_VERSION_OUTDATED_MAJOR,
            mapOf(
                FileTypes.EN to "<prefix> <red>A new major version is available! <newline><white>Current version: <gray><oldVersion><newline><white>Latest version: <gray><newVersion><newline><white>Download: <gray><link>",
                FileTypes.DE to "<prefix> <red>Eine neue Major-Version ist verfügbar! <newline><white>Aktuelle Version: <gray><oldVersion><newline><white>Neueste Version: <gray><newVersion><newline><white>Download: <gray><link>"
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

        registerNewString(
            LangStrings.COMMAND_EXECUTOR_ASYNC_ACTION,
            mapOf(
                FileTypes.EN to "<prefix> <yellow>The command is processing data in the background. Please be patient...",
                FileTypes.DE to "<prefix> <yellow>Der Befehl wird im Hintergrund verarbeitet. Bitte habe etwas Geduld...",
            )
        )

        registerNewString(
            LangStrings.COMMAND_SUGGESTION_ASYNC_ACTION,
            mapOf(
                FileTypes.EN to "<prefix> <yellow>Suggestions are being loaded in the background. Please be patient...",
                FileTypes.DE to "<prefix> <yellow>Vorschläge werden im Hintergrund geladen. Bitte habe etwas Geduld...",
            )
        )

        registerNewString(
            LangStrings.COMMAND_EXECUTOR_ASYNC_ERROR_PLAYER,
            mapOf(
                FileTypes.EN to "<prefix> <red>An error occurred while trying to resolve the player data for this command. Most likely, the player does not exist or there was a problem with the mojang API. Please try again later.",
                FileTypes.DE to "<prefix> <red>Beim Versuch, die Spielerdaten für diesen Befehl zu verarbeiten, ist ein Fehler aufgetreten. Es ist sehr wahrscheinlich, dass der Spieler nicht existiert oder es ein Problem mit der Mojang API gab. Bitte versuche es später erneut."
            )
        )

        registerNewString(
            LangStrings.CONFIG_ERROR_INVALID_TIMEZONE,
            mapOf(
                FileTypes.EN to "<prefix> <red>Invalid timezone specified in the config file! Please check your settings.",
                FileTypes.DE to "<prefix> <red>Ungültige Zeitzone in der Konfig angegeben! Bitte überprüfe deine Einstellungen."
            )
        )

        registerNewString(
            LangStrings.YVTILS_INFO,
            mapOf(
                FileTypes.EN to "<prefix> <white>YVtils <gray>v<version><newline><prefix> <white>Active modules (<gray><moduleCount><white>): <gray><modules><newline><prefix> <updateStatus>",
                FileTypes.DE to "<prefix> <white>YVtils <gray>v<version><newline><prefix> <white>Aktive Module (<gray><moduleCount><white>): <gray><modules><newline><prefix> <updateStatus>"
            )
        )

        registerNewString(
            LangStrings.YVTILS_INFO_UPDATE_UP_TO_DATE,
            mapOf(
                FileTypes.EN to "<green>You are running the latest version.",
                FileTypes.DE to "<green>Du verwendest bereits die neueste Version."
            )
        )

        registerNewString(
            LangStrings.YVTILS_INFO_UPDATE_AVAILABLE,
            mapOf(
                FileTypes.EN to "<yellow>Update available: <gray><newVersion> <white>(current: <gray><currentVersion><white>)",
                FileTypes.DE to "<yellow>Update verfügbar: <gray><newVersion> <white>(aktuell: <gray><currentVersion><white>)"
            )
        )

        registerNewString(
            LangStrings.YVTILS_INFO_UPDATE_UNKNOWN,
            mapOf(
                FileTypes.EN to "<gray>Update status unknown (check disabled or failed).",
                FileTypes.DE to "<gray>Update-Status unbekannt (Prüfung deaktiviert oder fehlgeschlagen)."
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_GUI_TITLE,
            mapOf(
                FileTypes.EN to "Modules",
                FileTypes.DE to "Module"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_STATUS_ENABLED,
            mapOf(
                FileTypes.EN to "<green>Enabled",
                FileTypes.DE to "<green>Aktiviert"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_STATUS_DISABLED,
            mapOf(
                FileTypes.EN to "<red>Disabled",
                FileTypes.DE to "<red>Deaktiviert"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_STATUS_PENDING_ENABLE,
            mapOf(
                FileTypes.EN to "<yellow>Will be enabled after restart",
                FileTypes.DE to "<yellow>Wird nach Neustart aktiviert"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_STATUS_PENDING_DISABLE,
            mapOf(
                FileTypes.EN to "<yellow>Will be disabled after restart",
                FileTypes.DE to "<yellow>Wird nach Neustart deaktiviert"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_LORE_TOGGLE,
            mapOf(
                FileTypes.EN to "<gray>Left-Click: <white>Enable/Disable",
                FileTypes.DE to "<gray>Linksklick: <white>Aktivieren/Deaktivieren"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_TOGGLE_ENABLE,
            mapOf(
                FileTypes.EN to "<green>Click to enable",
                FileTypes.DE to "<green>Klicke zum Aktivieren"
            )
        )

        registerNewString(
            LangStrings.YVTILS_MODULES_TOGGLE_DISABLE,
            mapOf(
                FileTypes.EN to "<red>Click to disable",
                FileTypes.DE to "<red>Klicke zum Deaktivieren"
            )
        )

        registerNewString(
            LangStrings.YVTILS_CONFIG_GUI_TITLE,
            mapOf(
                FileTypes.EN to "Configs",
                FileTypes.DE to "Konfigurationen"
            )
        )

        registerNewString(
            LangStrings.YVTILS_CONFIG_GUI_LORE_OPEN,
            mapOf(
                FileTypes.EN to "<gray>Left-Click: <white>Open config",
                FileTypes.DE to "<gray>Linksklick: <white>Konfiguration öffnen"
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
    PLUGIN_VERSION_UP_TO_DATE("plugin.version.upToDate"),
    PLUGIN_VERSION_OUTDATED_PATCH("plugin.version.outdated.patch"),
    PLUGIN_VERSION_OUTDATED_MINOR("plugin.version.outdated.minor"),
    PLUGIN_VERSION_OUTDATED_MAJOR("plugin.version.outdated.major"),
    TEXT_COPY("text.action.copy"),
    COMMAND_MISSING_PLAYER("command.missing.player"),
    COMMAND_EXECUTOR_NOT_PLAYER("command.executor.notPlayer"),
    COMMAND_EXECUTOR_MISSING_PERMISSION("command.executor.missingPermission"),
    COMMAND_USAGE("command.usage"),
    COMMAND_EXECUTOR_ASYNC_ACTION("command.executor.asyncAction"),
    COMMAND_SUGGESTION_ASYNC_ACTION("command.suggestion.asyncAction"),
    COMMAND_EXECUTOR_ASYNC_ERROR_PLAYER("command.executor.asyncError.player"),
    CONFIG_ERROR_INVALID_TIMEZONE("config.error.invalid.timezone"),
    YVTILS_INFO("yvtils.info.summary"),
    YVTILS_INFO_UPDATE_UP_TO_DATE("yvtils.info.update.upToDate"),
    YVTILS_INFO_UPDATE_AVAILABLE("yvtils.info.update.available"),
    YVTILS_INFO_UPDATE_UNKNOWN("yvtils.info.update.unknown"),
    YVTILS_MODULES_GUI_TITLE("yvtils.modules.gui.title"),
    YVTILS_MODULES_STATUS_ENABLED("yvtils.modules.status.enabled"),
    YVTILS_MODULES_STATUS_DISABLED("yvtils.modules.status.disabled"),
    YVTILS_MODULES_STATUS_PENDING_ENABLE("yvtils.modules.status.pendingEnable"),
    YVTILS_MODULES_STATUS_PENDING_DISABLE("yvtils.modules.status.pendingDisable"),
    YVTILS_MODULES_LORE_TOGGLE("yvtils.modules.lore.toggle"),
    YVTILS_MODULES_TOGGLE_ENABLE("yvtils.modules.toggle.enable"),
    YVTILS_MODULES_TOGGLE_DISABLE("yvtils.modules.toggle.disable"),
    YVTILS_CONFIG_GUI_TITLE("yvtils.config.gui.title"),
    YVTILS_CONFIG_GUI_LORE_OPEN("yvtils.config.gui.lore.open"),
}
