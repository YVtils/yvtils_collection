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

package yv.tils.discord.language

import yv.tils.config.language.FileTypes
import yv.tils.config.language.LanguageProvider
import yv.tils.config.language.LanguageProvider.Companion.registerNewString

class RegisterStrings : LanguageProvider.RegisterStrings {
    override fun registerStrings() {
        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Send a whitelist request to the Minecraft server.",
                FileTypes.DE to "Sendet eine Whitelist-Anfrage an den Minecraft-Server.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Force-add a player to the whitelist and optionally link their Discord account.",
                FileTypes.DE to "Fügt einen Spieler zwangsweise zur Whitelist hinzu und verknüpft optional Discord.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_MINECRAFTNAME_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Minecraft name of the player to force-add.",
                FileTypes.DE to "Minecraft-Name des Spielers, der zwangsweise hinzugefügt werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_DISCORDUSER_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Discord user to link with the Minecraft player.",
                FileTypes.DE to "Discord-Benutzer, der mit dem Spieler verknüpft werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Force-remove a player from the whitelist. Dropdown shown if no args are given.",
                FileTypes.DE to "Entfernt Spieler von der Whitelist. Auswahlmenü bei fehlenden Argumenten.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_SITE_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Enter whitelist page (25 per page). Leave blank for page one.",
                FileTypes.DE to "Whitelist-Seite angeben (25 pro Seite). Leer lassen für Seite 1.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_DISCORDUSER_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Discord user to remove from the whitelist.",
                FileTypes.DE to "Discord-Benutzer, der von der Whitelist entfernt werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_MINECRAFTNAME_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Minecraft name of the player to remove.",
                FileTypes.DE to "Minecraft-Name des Spielers, der entfernt werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Check if a player is whitelisted.",
                FileTypes.DE to "Prüft, ob ein Spieler auf der Whitelist ist.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_MINECRAFTNAME_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Minecraft name of the player to check.",
                FileTypes.DE to "Minecraft-Name des Spielers, der geprüft werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_DISCORDUSER_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Discord user to check.",
                FileTypes.DE to "Discord-Benutzer, der geprüft werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_MCINFO_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Get information about the Minecraft server.",
                FileTypes.DE to "Erhalte Informationen über den Minecraft-Server.",
            )
        )

        registerNewString(
            LangStrings.BOT_START_SUCCESS,
            mapOf(
                FileTypes.EN to "Discord app started successfully.",
                FileTypes.DE to "Discord-App wurde erfolgreich gestartet.",
            )
        )

        registerNewString(
            LangStrings.BOT_STOP_SHUTDOWN,
            mapOf(
                FileTypes.EN to "Shutting down Discord app...",
                FileTypes.DE to "Discord-App wird heruntergefahren...",
            )
        )

        registerNewString(
            LangStrings.BOT_STOP_FAILED,
            mapOf(
                FileTypes.EN to "The discord app could not be stopped. Error: <error>",
                FileTypes.DE to "Die Discord-App konnte nicht gestoppt werden. Fehler: <error>",
            )
        )

        registerNewString(
            LangStrings.BOT_STOP_SUCCESS,
            mapOf(
                FileTypes.EN to "Discord app stopped successfully.",
                FileTypes.DE to "Discord-App wurde erfolgreich gestoppt.",
            )
        )

        registerNewString(
            LangStrings.BOT_STOP_NOT_RUNNING,
            mapOf(
                FileTypes.EN to "Can not shutdown Discord app, because it is not running.",
                FileTypes.DE to "Die Discord-App kann nicht heruntergefahren werden, da sie nicht läuft.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_ADD_TITLE,
            mapOf(
                FileTypes.EN to "The Minecraft account has been successfully added to the whitelist!",
                FileTypes.DE to "Der Minecraft Account wurde erfolgreich zur Whitelist hinzugefügt!",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_ADD_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Account: <accountName>",
                FileTypes.DE to "Account: <accountName>",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_ALREADY_LISTED_TITLE,
            mapOf(
                FileTypes.EN to "The account is already whitelisted",
                FileTypes.DE to "Der Account ist bereits auf der Whitelist",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_ALREADY_LISTED_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Account: <accountName> • This account is already whitelisted!",
                FileTypes.DE to "Account: <accountName> • Dieser Account ist bereits auf der Whitelist!",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_INVALID_TITLE,
            mapOf(
                FileTypes.EN to "The account is invalid or does not exist",
                FileTypes.DE to "Der Account ist ungültig oder existiert nicht",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_INVALID_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Account: <accountName> • The account is invalid or does not exist.",
                FileTypes.DE to "Account: <accountName> • Der Account ist ungültig oder existiert nicht.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_PROMPT_TITLE,
            mapOf(
                FileTypes.EN to "Are you sure you want to change the account?",
                FileTypes.DE to "Bist du sicher, dass du den Account ändern möchtest?",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_PROMPT_DESCRIPTION,
            mapOf(
                FileTypes.EN to "You will replace the current whitelisted account (<oldName>) with the new one (<newName>).",
                FileTypes.DE to "Du wirst den aktuellen Whitelist-Account (<oldName>) mit dem neuen Account (<newName>) ersetzen.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_TITLE,
            mapOf(
                FileTypes.EN to "The account has been successfully changed!",
                FileTypes.DE to "Der Account wurde erfolgreich geändert!",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Old Account: <oldName> • New Account: <newName>",
                FileTypes.DE to "Alter Account: <oldName> • Neuer Account: <newName>",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ERROR_TITLE,
            mapOf(
                FileTypes.EN to "An error occurred while processing your request",
                FileTypes.DE to "Ein Fehler ist bei der Verarbeitung deiner Anfrage aufgetreten",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_ERROR_DESCRIPTION,
            mapOf(
                FileTypes.EN to "An error occurred while processing your request. Please try again later. Error: <error>",
                FileTypes.DE to "Ein Fehler ist bei der Verarbeitung deiner Anfrage aufgetreten. Bitte versuche es später erneut. Fehler: <error>",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_TITLE,
            mapOf(
                FileTypes.EN to "Which whitelist entries do you want to remove?",
                FileTypes.DE to "Welche Whitelist-Einträge möchtest du entfernen?",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Select the entries you want to remove from the whitelist. You can select multiple entries.",
                FileTypes.DE to "Wähle die Einträge aus, die du von der Whitelist entfernen möchtest. Du kannst mehrere Einträge auswählen.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_NO_ENTRIES_DESCRIPTION,
            mapOf(
                FileTypes.EN to "No entries found. Please try again later.",
                FileTypes.DE to "Keine Einträge gefunden. Bitte versuche es später erneut.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_REMOVED_FIELD_NAME,
            mapOf(
                FileTypes.EN to "Removed Accounts:",
                FileTypes.DE to "Entfernte Accounts:",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_WHITELIST_CHECK_TITLE,
            mapOf(
                FileTypes.EN to "Whitelist Check",
                FileTypes.DE to "Whitelist Überprüfung",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_ACTION_CANCELLED_TITLE,
            mapOf(
                FileTypes.EN to "Action Cancelled",
                FileTypes.DE to "Aktion Abgebrochen",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_ACTION_CANCELLED_DESCRIPTION,
            mapOf(
                FileTypes.EN to "The action <action> has been cancelled.",
                FileTypes.DE to "Die Aktion <action> wurde abgebrochen.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_ACTION_CANCELLED_ACTION_ACCOUNT_REPLACE,
            mapOf(
                FileTypes.EN to "Account Replacement",
                FileTypes.DE to "Account Ersetzung",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_SYNC_JOIN_LEAVE_TEXT,
            mapOf(
                FileTypes.EN to "<player> has <action> the server.",
                FileTypes.DE to "<player> hat den Server <action>.",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_SYNC_JOIN_LEAVE_ACTION_JOIN,
            mapOf(
                FileTypes.EN to "joined",
                FileTypes.DE to "betreten",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_SYNC_JOIN_LEAVE_ACTION_LEAVE,
            mapOf(
                FileTypes.EN to "left",
                FileTypes.DE to "verlassen",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_SYNC_ADVANCEMENT_NO_DESCRIPTION,
            mapOf(
                FileTypes.EN to "No description available",
                FileTypes.DE to "Keine Beschreibung verfügbar",
            )
        )

        registerNewString(
            LangStrings.COMPONENT_SYNC_DEATH_TEXT,
            mapOf(
                FileTypes.EN to "<player> has died",
                FileTypes.DE to "<player> ist gestorben",
            )
        )

        registerNewString(
            LangStrings.CONSOLE_WHITELIST_ACCOUNT_ADDED,
            mapOf(
                FileTypes.EN to "Discord Account <discordAccount> has been linked to Minecraft account <minecraftAccount> and added to the whitelist by <user>.",
                FileTypes.DE to "Discord Account <discordAccount> wurde mit dem Minecraft Account <minecraftAccount> verknüpft und von <user> zur Whitelist hinzugefügt.",
            )
        )

        registerNewString(
            LangStrings.CONSOLE_WHITELIST_ACCOUNT_REMOVED,
            mapOf(
                FileTypes.EN to "Discord Account <discordAccount> has been unlinked from Minecraft account <minecraftAccount> and removed from the whitelist by <user>.",
                FileTypes.DE to "Discord Account <discordAccount> wurde vom Minecraft Account <minecraftAccount> getrennt und von <user> von der Whitelist entfernt.",
            )
        )

        registerNewString(
            LangStrings.CONSOLE_WHITELIST_ACCOUNT_REPLACED,
            mapOf(
                FileTypes.EN to "User <user> replaced Minecraft account <oldAccount> with <newAccount> for Discord account <discordAccount>.",
                FileTypes.DE to "Benutzer <user> hat den Minecraft-Account <oldAccount> durch <newAccount> für den Discord-Account <discordAccount> ersetzt.",
            )
        )

        registerNewString(
            LangStrings.CONSOLE_WHITELIST_ACCOUNT_ALREADY_LISTED,
            mapOf(
                FileTypes.EN to "Discord User <user> tried to link Discord account <discordAccount> with Minecraft account <minecraftAccount>, but it is already whitelisted.",
                FileTypes.DE to "Discord-Benutzer <user> hat versucht, den Discord-Account <discordAccount> mit dem Minecraft-Account <minecraftAccount> zu verknüpfen, aber dieser ist bereits auf der Whitelist.",
            )
        )

        registerNewString(
            LangStrings.CONSOLE_WHITELIST_ACCOUNT_INVALID,
            mapOf(
                FileTypes.EN to "Discord User <user> tried to link Discord account <discordAccount> with Minecraft account <minecraftAccount>, but the account is invalid or does not exist.",
                FileTypes.DE to "Discord-Benutzer <user> hat versucht, den Discord-Account <discordAccount> mit dem Minecraft-Account <minecraftAccount> zu verknüpfen, aber der Account ist ungültig oder existiert nicht.",
            )
        )

        registerNewString(
            LangStrings.CONSOLE_WHITELIST_ACCOUNT_ERROR,
            mapOf(
                FileTypes.EN to "An error occurred while processing the whitelist request by <user> for Discord account <discordAccount> and Minecraft account <minecraftAccount>. Error: <error>",
                FileTypes.DE to "Ein Fehler ist bei der Verarbeitung der Whitelist-Anfrage von <user> für den Discord-Account <discordAccount> und den Minecraft-Account <minecraftAccount> aufgetreten. Fehler: <error>",
            )
        )

        registerNewString(
            LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED,
            mapOf(
                FileTypes.EN to "No account found in cache for user <user> during account replacement.",
                FileTypes.DE to "Es konnte kein Account im Cache für den Benutzer <user> während dem ersetzen des Accounts gefunden werden.",
            )
        )

        registerNewString(
            LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_ALREADY_CACHED,
            mapOf(
                FileTypes.EN to "There is already an account cached for user <user>. Please finish or cancel the current process before starting a new one.",
                FileTypes.DE to "Es ist bereits ein Account für den Benutzer <user> im Cache gespeichert. Bitte beende oder breche den aktuellen Prozess ab, bevor du einen neuen startest.",
            )
        )

        registerNewString(
            LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_EXCEPTION,
            mapOf(
                FileTypes.EN to "An exception occurred while replacing the account for user <user>: <error>",
                FileTypes.DE to "Es ist ein Fehler aufgetreten, während der Account für den Benutzer <user> ersetzt wurde: <error>",
            )
        )

        registerNewString(
            LangStrings.ERROR_WHITELIST_FORCE_REMOVE_NO_ENTRY_DISCORD,
            mapOf(
                FileTypes.EN to "No whitelist entry found for Discord user <discordUser>.",
                FileTypes.DE to "Kein Whitelist-Eintrag für den Discord-Benutzer <discordUser> gefunden.",
            )
        )

        registerNewString(
            LangStrings.ERROR_WHITELIST_FORCE_REMOVE_NO_ENTRY_MINECRAFT,
            mapOf(
                FileTypes.EN to "No whitelist entry found for Minecraft user <minecraftName>.",
                FileTypes.DE to "Kein Whitelist-Eintrag für den Minecraft-Benutzer <minecraftName> gefunden.",
            )
        )

        registerNewString(
            LangStrings.ERROR_WHITELIST_FORCE_REMOVE_ENTRIES_NOT_EQUAL,
            mapOf(
                FileTypes.EN to "Discord entry and Minecraft entry do not match: <discordEntry> vs <minecraftEntry>.",
                FileTypes.DE to "Discord-Eintrag und Minecraft-Eintrag stimmen nicht überein: <discordEntry> vs <minecraftEntry>.",
            )
        )
    }

    enum class LangStrings(override val key: String) : LanguageProvider.LangStrings {
        SLASHCOMMANDS_WHITELIST_DESCRIPTION("discord.slashcommands.whitelist.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceAdd.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_MINECRAFTNAME_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceAdd.args.minecraftName.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_DISCORDUSER_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceAdd.args.discordUser.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_SITE_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.args.site.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_DISCORDUSER_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.args.discordUser.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_MINECRAFTNAME_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.args.minecraftName.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_DESCRIPTION("discord.slashcommands.whitelist.subcommands.check.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_MINECRAFTNAME_DESCRIPTION("discord.slashcommands.whitelist.subcommands.check.args.minecraftName.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_DISCORDUSER_DESCRIPTION("discord.slashcommands.whitelist.subcommands.check.args.discordUser.description"),
        SLASHCOMMANDS_MCINFO_DESCRIPTION("discord.slashcommands.mcinfo.description"),

        BOT_START_SUCCESS("discord.bot.start.success"),

        BOT_STOP_SHUTDOWN("discord.bot.stop.shutdown"),
        BOT_STOP_FAILED("discord.bot.stop.failed"),
        BOT_STOP_SUCCESS("discord.bot.stop.success"),
        BOT_STOP_NOT_RUNNING("discord.bot.stop.notRunning"),

        COMPONENT_WHITELIST_ACCOUNT_ADD_TITLE("discord.component.whitelist.account.add.title"),
        COMPONENT_WHITELIST_ACCOUNT_ADD_DESCRIPTION("discord.component.whitelist.account.add.description"),
        COMPONENT_WHITELIST_ACCOUNT_ALREADY_LISTED_TITLE("discord.component.whitelist.account.alreadyListed.title"),
        COMPONENT_WHITELIST_ACCOUNT_ALREADY_LISTED_DESCRIPTION("discord.component.whitelist.account.alreadyListed.description"),
        COMPONENT_WHITELIST_ACCOUNT_INVALID_TITLE("discord.component.whitelist.account.invalid.title"),
        COMPONENT_WHITELIST_ACCOUNT_INVALID_DESCRIPTION("discord.component.whitelist.account.invalid.description"),
        COMPONENT_WHITELIST_ACCOUNT_CHANGE_PROMPT_TITLE("discord.component.whitelist.account.change.prompt.title"),
        COMPONENT_WHITELIST_ACCOUNT_CHANGE_PROMPT_DESCRIPTION("discord.component.whitelist.account.change.prompt.description"),
        COMPONENT_WHITELIST_ACCOUNT_CHANGE_TITLE("discord.component.whitelist.account.change.title"),
        COMPONENT_WHITELIST_ACCOUNT_CHANGE_DESCRIPTION("discord.component.whitelist.account.change.description"),
        COMPONENT_WHITELIST_ERROR_TITLE("discord.component.whitelist.error.title"),
        COMPONENT_WHITELIST_ERROR_DESCRIPTION("discord.component.whitelist.error.description"),
        COMPONENT_WHITELIST_FORCE_REMOVE_TITLE("discord.component.whitelist.force.remove.title"),
        COMPONENT_WHITELIST_FORCE_REMOVE_DESCRIPTION("discord.component.whitelist.force.remove.description"),
        COMPONENT_WHITELIST_FORCE_REMOVE_NO_ENTRIES_DESCRIPTION("discord.component.whitelist.force.remove.noEntriesDescription"),
        COMPONENT_WHITELIST_FORCE_REMOVE_REMOVED_FIELD_NAME("discord.component.whitelist.force.remove.removedFieldName"),

        COMPONENT_WHITELIST_CHECK_TITLE("discord.component.whitelist.check.title"),

        COMPONENT_ACTION_CANCELLED_TITLE("discord.component.actionCancelled.title"),
        COMPONENT_ACTION_CANCELLED_DESCRIPTION("discord.component.actionCancelled.description"),
        COMPONENT_ACTION_CANCELLED_ACTION_ACCOUNT_REPLACE("discord.component.actionCancelled.action.accountReplace"),

        COMPONENT_SYNC_JOIN_LEAVE_TEXT("discord.component.sync.joinLeave.text"),
        COMPONENT_SYNC_JOIN_LEAVE_ACTION_JOIN("discord.component.sync.joinLeave.action.join"),
        COMPONENT_SYNC_JOIN_LEAVE_ACTION_LEAVE("discord.component.sync.joinLeave.action.leave"),
        COMPONENT_SYNC_ADVANCEMENT_NO_DESCRIPTION("discord.component.sync.advancement.noDescription"),
        COMPONENT_SYNC_DEATH_TEXT("discord.component.sync.death.text"),

        CONSOLE_WHITELIST_ACCOUNT_ADDED("discord.console.whitelist.account.added"),
        CONSOLE_WHITELIST_ACCOUNT_REMOVED("discord.console.whitelist.account.removed"),
        CONSOLE_WHITELIST_ACCOUNT_REPLACED("discord.console.whitelist.account.replaced"),
        CONSOLE_WHITELIST_ACCOUNT_ALREADY_LISTED("discord.console.whitelist.account.alreadyListed"),
        CONSOLE_WHITELIST_ACCOUNT_INVALID("discord.console.whitelist.account.invalid"),
        CONSOLE_WHITELIST_ACCOUNT_ERROR("discord.console.whitelist.account.error"),

        ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED("discord.error.whitelist.account.replace.notCached"),
        ERROR_WHITELIST_ACCOUNT_REPLACE_ALREADY_CACHED("discord.error.whitelist.account.replace.alreadyCached"),

        ERROR_WHITELIST_ACCOUNT_REPLACE_EXCEPTION("discord.error.whitelist.account.replace.exception"),
        ERROR_WHITELIST_FORCE_REMOVE_NO_ENTRY_DISCORD("discord.error.whitelist.force.remove.noEntry.discord"),
        ERROR_WHITELIST_FORCE_REMOVE_NO_ENTRY_MINECRAFT("discord.error.whitelist.force.remove.noEntry.minecraft"),
        ERROR_WHITELIST_FORCE_REMOVE_ENTRIES_NOT_EQUAL("discord.error.whitelist.force.remove.entriesNotEqual")
        ;
    }
}
