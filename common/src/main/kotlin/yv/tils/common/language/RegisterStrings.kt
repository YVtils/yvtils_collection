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

package yv.tils.common.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

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
                FileTypes.DE to "<prefix> <yellow>Der Befehl wird im Hintergrund verarbeitet. Bitte etwas Geduld...",
            )
        )

        registerNewString(
            LangStrings.CONFIG_ERROR_INVALID_TIMEZONE,
            mapOf(
                FileTypes.EN to "<prefix> <red>Invalid timezone specified in the config file! Please check your settings.",
                FileTypes.DE to "<prefix> <red>Ungültige Zeitzone in der Konfig angegeben! Bitte überprüfe deine Einstellungen."
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
    CONFIG_ERROR_INVALID_TIMEZONE("config.error.invalid.timezone"),
}
