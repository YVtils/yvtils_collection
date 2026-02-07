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

package yv.tils.moderation.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes
import yv.tils.config.language.LanguageProvider.Companion.registerNewString
import yv.tils.utils.colors.Colors

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.moderation.broadcast.message.permanent",
            mapOf(
                FileTypes.EN to "<prefix> <${Colors.TERTIARY.color}><target> <white>has been <action><white> for <${Colors.TERTIARY.color}><reason><white> by <${Colors.TERTIARY.color}><sender><white>.",
                FileTypes.DE to "<prefix> <${Colors.TERTIARY.color}><target> <white>wurde von <${Colors.TERTIARY.color}><sender><white> wegen <${Colors.TERTIARY.color}><reason><white> <action><white>.",
            )
        )

        registerNewString(
            "command.moderation.broadcast.message.temporary",
            mapOf(
                FileTypes.EN to "<prefix> <${Colors.TERTIARY.color}><target> <white>has been <action><white> for <${Colors.TERTIARY.color}><reason><white> for <${Colors.TERTIARY.color}><duration><white> by <${Colors.TERTIARY.color}><sender><white>.",
                FileTypes.DE to "<prefix> <${Colors.TERTIARY.color}><target> <white>wurde von <${Colors.TERTIARY.color}><sender><white> wegen <${Colors.TERTIARY.color}><reason><white> für <${Colors.TERTIARY.color}><duration><white> <action><white>.",
            )
        )

        registerNewString(
            "command.moderation.target.message.permanent",
            mapOf(
                FileTypes.EN to "<prefix> <white>You have been <action><white> for <${Colors.TERTIARY.color}><reason><white>.",
                FileTypes.DE to "<prefix> <white>Du wurdest wegen <${Colors.TERTIARY.color}><reason><white> <action><white>.",
            )
        )

        registerNewString(
            "command.moderation.target.message.temporary",
            mapOf(
                FileTypes.EN to "<prefix> <white>You have been <action> for <${Colors.TERTIARY.color}><reason><white> for <${Colors.TERTIARY.color}><duration><white>.",
                FileTypes.DE to "<prefix> <white>Du wurdest wegen <${Colors.TERTIARY.color}><reason><white> für <${Colors.TERTIARY.color}><duration><white> <action>.",
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.ban",
            mapOf(
                FileTypes.EN to "<red>banned",
                FileTypes.DE to "<red>gebannt"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.tempban",
            mapOf(
                FileTypes.EN to "<red>temporarily banned",
                FileTypes.DE to "<red>vorübergehend gebannt"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.unban",
            mapOf(
                FileTypes.EN to "<red>unbanned",
                FileTypes.DE to "<red>entbannt"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.mute",
            mapOf(
                FileTypes.EN to "<red>muted",
                FileTypes.DE to "<red>gemutet"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.tempmute",
            mapOf(
                FileTypes.EN to "<red>temporarily muted",
                FileTypes.DE to "<red>vorübergehend gemutet"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.unmute",
            mapOf(
                FileTypes.EN to "<red>unmuted",
                FileTypes.DE to "<red>entmutet"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.kick",
            mapOf(
                FileTypes.EN to "<yellow>kicked",
                FileTypes.DE to "<yellow>gekickt"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.warn",
            mapOf(
                FileTypes.EN to "<yellow>warned",
                FileTypes.DE to "<yellow>verwarnt"
            )
        )

        registerNewString(
            "command.moderation.broadcast.action.other",
            mapOf(
                FileTypes.EN to "<dark_gray>...",
                FileTypes.DE to "<dark_gray>..."
            )
        )

        registerNewString(
            "command.moderation.player.already.banned",
            mapOf(
                FileTypes.EN to "<prefix> <red>The player is already banned!",
                FileTypes.DE to "<prefix> <red>Der Spieler ist bereits gebannt!",
            )
        )

        registerNewString(
            "command.moderation.player.not.banned",
            mapOf(
                FileTypes.EN to "<prefix> <red>The player is not banned!",
                FileTypes.DE to "<prefix> <red>Der Spieler ist nicht gebannt!",
            )
        )

        registerNewString(
            "command.moderation.player.already.muted",
            mapOf(
                FileTypes.EN to "<prefix> <red>The player is already muted!",
                FileTypes.DE to "<prefix> <red>Der Spieler ist bereits gemutet!",
            )
        )

        registerNewString(
            "command.moderation.player.not.muted",
            mapOf(
                FileTypes.EN to "<prefix> <red>The player is not muted!",
                FileTypes.DE to "<prefix> <red>Der Spieler ist nicht gemutet!",
            )
        )

        registerNewString(
            "command.moderation.player.not.online",
            mapOf(
                FileTypes.EN to "<prefix> <red>The player is not online!",
                FileTypes.DE to "<prefix> <red>Der Spieler ist nicht online!",
            )
        )

        registerNewString(
            "moderation.placeholder.reason.none",
            mapOf(
                FileTypes.EN to "<white>No reason was defined!",
                FileTypes.DE to "<white>Es wurde kein Grund angegeben!",
            )
        )

        registerNewString(
            "moderation.placeholder.duration.none",
            mapOf(
                FileTypes.EN to "Never",
                FileTypes.DE to "Nie",
            )
        )

        registerNewString(
            "moderation.placeholder.duration.expires",
            mapOf(
                FileTypes.EN to "Expires in <yellow><duration>",
                FileTypes.DE to "Läuft ab in <yellow><duration>",
            )
        )

        registerNewString(
            "moderation.target.muted.chat.player",
            mapOf(
                FileTypes.EN to "<prefix> <white>You are muted! Reason: <${Colors.TERTIARY.color}><reason><white>; Duration: <${Colors.TERTIARY.color}><duration>",
                FileTypes.DE to "<prefix> <white>Du bist gemutet! Grund: <${Colors.TERTIARY.color}><reason><white>; Dauer: <${Colors.TERTIARY.color}><duration>",
            )
        )

        registerNewString(
            "moderation.target.muted.chat.console",
            mapOf(
                FileTypes.EN to "<prefix> <${Colors.TERTIARY.color}><player> <white>tried to chat while muted. Message: <${Colors.TERTIARY.color}><message><white>; Reason: <${Colors.TERTIARY.color}><reason><white>; Duration: <${Colors.TERTIARY.color}><duration>",
                FileTypes.DE to "<prefix> <${Colors.TERTIARY.color}><player> <white>hat versucht zu schreiben, während er gemutet ist. Nachricht: <${Colors.TERTIARY.color}><message><white>; Grund: <${Colors.TERTIARY.color}><reason><white>; Dauer: <${Colors.TERTIARY.color}><duration>",
            )
        )

        registerNewString(
            "moderation.target.disconnected",
            mapOf(
                FileTypes.EN to "<red>You have been disconnected from the server",
                FileTypes.DE to "<red>Du wurdest vom Server getrennt",
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
