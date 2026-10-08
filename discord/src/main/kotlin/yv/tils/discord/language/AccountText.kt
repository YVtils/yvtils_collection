package yv.tils.discord.language

import org.bukkit.command.CommandSender
import yv.tils.configv2.language.FileTypes
import yv.tils.configv2.language.LanguageProvider
import yv.tils.configv2.language.LanguageHandler
import yv.tils.discord.logic.whitelist.AccountService
import yv.tils.utils.logger.Logger
import net.kyori.adventure.text.minimessage.MiniMessage
import yv.tils.utils.colors.Colors

/** Uses the same registered language keys and player/console resolution as other modules. */
object AccountText {
    private val strings = mapOf(
        "panel" to ("SMP registration\nRegister your Minecraft Java account using the button below." to "SMP-Registrierung\nRegistriere deinen Minecraft-Java-Account über den Button unten."),
        "panelDescription" to ("Click **Register** below to open a form for your Minecraft Java username. Once your account is validated, it will be linked to Discord, added to the server whitelist, and assigned the configured Discord roles." to "Klicke unten auf **Registrieren**, um ein Formular für deinen Minecraft-Java-Benutzernamen zu öffnen. Nach der Prüfung wird dein Account mit Discord verknüpft, zur Server-Whitelist hinzugefügt und du erhältst die konfigurierten Discord-Rollen."),
        "panelHelp" to ("Help needed?" to "Hilfe benötigt?"),
        "tutorialLabel" to ("Tutorial" to "Tutorial"),
        "supportLabel" to ("Support" to "Support"),
        "tutorialOption" to ("Optional tutorial URL, channel/thread ID or channel mention" to "Optionale Tutorial-URL, Kanal-/Thread-ID oder Kanal-Erwähnung"),
        "supportOption" to ("Optional support URL, channel/thread ID or channel mention" to "Optionale Support-URL, Kanal-/Thread-ID oder Kanal-Erwähnung"),
        "invalidHelpLink" to ("Provide a channel/thread ID, channel mention, or valid HTTP/HTTPS URL without spaces or parentheses." to "Gib eine Kanal-/Thread-ID, Kanal-Erwähnung oder gültige HTTP-/HTTPS-URL ohne Leerzeichen oder Klammern an."),
        "register" to ("Register" to "Registrieren"),
        "replacementConfirmTitle" to ("Confirm account change" to "Account-Wechsel bestätigen"),
        "replacementConfirmDescription" to ("Change your linked Minecraft account from **<old>** to **<new>**? Your previous account will lose whitelist access and will be disconnected if online. Confirm within five minutes. Nothing changes until you confirm." to "Möchtest du deinen verknüpften Minecraft-Account von **<old>** zu **<new>** ändern? Dein bisheriger Account verliert den Whitelist-Zugriff und wird getrennt, falls er online ist. Bestätige innerhalb von fünf Minuten. Bis zur Bestätigung wird nichts geändert."),
        "replacementConfirmButton" to ("Confirm account change" to "Account-Wechsel bestätigen"),
        "replacementSuccessTitle" to ("Account changed" to "Account gewechselt"),
        "replacementSuccess" to ("Your new Minecraft account is linked and whitelisted. Your previous account's whitelist access was removed and Discord roles were updated." to "Dein neuer Minecraft-Account ist verknüpft und auf der Whitelist. Der Whitelist-Zugriff deines bisherigen Accounts wurde entfernt und die Discord-Rollen wurden aktualisiert."),
        "replacementCancelled" to ("Account change cancelled. Your existing account remains linked." to "Account-Wechsel abgebrochen. Dein bisheriger Account bleibt verknüpft."),
        "registerButton" to ("Register now" to "Jetzt registrieren"),
        "registrationCallToAction" to ("Start your registration here" to "Starte hier deine Registrierung"),
        "registrationSuccessTitle" to ("Registration successful" to "Registrierung erfolgreich"),
        "registrationPartialTitle" to ("Registered — Discord roles need attention" to "Registriert — Discord-Rollen benötigen Aufmerksamkeit"),
        "registrationAlreadyTitle" to ("Already registered" to "Bereits registriert"),
        "registrationErrorTitle" to ("Registration unsuccessful" to "Registrierung nicht erfolgreich"),
        "username" to ("Minecraft Java username" to "Minecraft-Java-Benutzername"),
        "setup" to ("Post a registration panel" to "Ein Registrierungspanel veröffentlichen"),
        "channel" to ("Text channel for the registration panel" to "Textkanal für das Registrierungspanel"),
        "posted" to ("Registration panel posted." to "Registrierungspanel veröffentlicht."),
        "postFailed" to ("The registration panel could not be posted. Check channel permissions." to "Das Registrierungspanel konnte nicht veröffentlicht werden. Prüfe die Kanalrechte."),
        "permission" to ("You do not have permission for this action." to "Du hast keine Berechtigung für diese Aktion."),
        "guild" to ("Registration is only available in the configured server." to "Die Registrierung ist nur auf dem konfigurierten Discord-Server verfügbar."),
        "success" to ("Account registered and whitelisted. Discord roles assigned." to "Account registriert und zur Whitelist hinzugefügt. Discord-Rollen zugewiesen."),
        "completed" to ("Account operation completed, including Discord roles." to "Account-Aktion einschließlich Discord-Rollen abgeschlossen."),
        "roles" to ("The account/whitelist operation succeeded, but Discord roles could not be updated. Contact an administrator; do not repeat the account operation." to "Die Account-/Whitelist-Aktion war erfolgreich, aber Discord-Rollen konnten nicht aktualisiert werden. Kontaktiere die Administration; wiederhole die Account-Aktion nicht."),
        "invalid" to ("Invalid or nonexistent Minecraft Java account. Use 3–16 letters, digits or underscores." to "Ungültiger oder nicht existierender Minecraft-Java-Account. Verwende 3–16 Buchstaben, Ziffern oder Unterstriche."),
        "lookup" to ("Account lookup is unavailable. Please try again later." to "Die Account-Abfrage ist nicht verfügbar. Versuche es später erneut."),
        "already" to ("You are already registered with this Minecraft account." to "Du bist bereits mit diesem Minecraft-Account registriert."),
        "different" to ("Your Discord account is already linked to another account. Use the existing account-change process in the configured whitelist channel or contact an administrator." to "Dein Discord-Account ist bereits mit einem anderen Account verknüpft. Nutze den bestehenden Account-Wechsel im konfigurierten Whitelist-Kanal oder kontaktiere die Administration."),
        "claimed" to ("This Minecraft account is already linked to another Discord user." to "Dieser Minecraft-Account ist bereits mit einem anderen Discord-Benutzer verknüpft."),
        "unlinkedWhitelist" to ("This account is already whitelisted without a saved link. An administrator must resolve this before registration." to "Dieser Account steht bereits ohne gespeicherte Verknüpfung auf der Whitelist. Die Administration muss dies vor der Registrierung klären."),
        "storage" to ("Whitelist or persistence failed. Registration was not completed. Contact an administrator." to "Whitelist oder Speicherung fehlgeschlagen. Die Registrierung wurde nicht abgeschlossen. Kontaktiere die Administration."),
        "unexpected" to ("The action failed unexpectedly. Contact an administrator." to "Die Aktion ist unerwartet fehlgeschlagen. Kontaktiere die Administration."),
        "missing" to ("No saved account link found." to "Keine gespeicherte Account-Verknüpfung gefunden."),
        "stale" to ("This entry has changed. Refresh the list before trying again." to "Dieser Eintrag wurde geändert. Aktualisiere die Liste vor einer erneuten Aktion."),
        "playerOnly" to ("Only players can open this GUI." to "Nur Spieler können dieses Menü öffnen."),
        "help" to ("/discordaccounts list [page] | inspect <name/UUID/Discord ID> | add <name/UUID> <Discord ID> | remove <entry> | replace <entry> <name/UUID> | gui [entry] [replacement]" to "/discordaccounts list [Seite] | inspect <Name/UUID/Discord-ID> | add <Name/UUID> <Discord-ID> | remove <Eintrag> | replace <Eintrag> <Name/UUID> | gui [Eintrag] [Ersatz]"),
        "list" to ("Linked accounts — page <page>/<pages>" to "Verknüpfte Accounts — Seite <page>/<pages>"),
        "empty" to ("No linked accounts." to "Keine verknüpften Accounts."),
        "entry" to ("Minecraft: <name>\nUUID: <uuid>\nDiscord: <discord> (<display>)" to "Minecraft: <name>\nUUID: <uuid>\nDiscord: <discord> (<display>)"),
        "unavailable" to ("Display name unavailable" to "Anzeigename nicht verfügbar"),
        "discordNameLoading" to ("Loading Discord name…" to "Discord-Name wird geladen…"),
        "noDiscordLink" to ("No linked Discord user" to "Kein verknüpfter Discord-Benutzer"),
        "title" to ("Linked Discord accounts" to "Verknüpfte Discord-Accounts"),
        "guiInspectHint" to ("Click to inspect and manage this account." to "Klicke, um diesen Account anzusehen und zu verwalten."),
        "guiClose" to ("Close" to "Schließen"),
        "guiBack" to ("Back to account list" to "Zurück zur Account-Liste"),
        "guiRefresh" to ("Refresh accounts" to "Accounts aktualisieren"),
        "guiRefreshHint" to ("Click to reload the current saved links." to "Klicke, um die aktuellen gespeicherten Verknüpfungen zu laden."),
        "guiListHint" to ("Select a player head to view account details." to "Wähle einen Spielerkopf, um Account-Details anzusehen."),
        "guiPage" to ("Page <page> / <pages>" to "Seite <page> / <pages>"),
        "guiRemoveHint" to ("Click to review removal.\nRemoves the link, whitelist access and Discord roles." to "Klicke, um die Entfernung zu prüfen.\nEntfernt Verknüpfung, Whitelist-Zugriff und Discord-Rollen."),
        "guiReplaceHint" to ("Click to review the proposed account change." to "Klicke, um den vorgeschlagenen Account-Wechsel zu prüfen."),
        "guiReplaceInputHint" to ("Enter a Minecraft username or UUID in the anvil.\nReview and confirm before any account is changed." to "Gib einen Minecraft-Benutzernamen oder eine UUID im Amboss ein.\nPrüfe und bestätige, bevor ein Account geändert wird."),
        "guiReplacementInputTitle" to ("New Minecraft account" to "Neuer Minecraft-Account"),
        "guiReviewReplacement" to ("Validate and review" to "Prüfen und ansehen"),
        "guiResolving" to ("Checking the Minecraft account…" to "Minecraft-Account wird geprüft…"),
        "guiRemoveWarning" to ("Confirming removes this link and whitelist access.\nThe previous player will be disconnected if online." to "Die Bestätigung entfernt Verknüpfung und Whitelist-Zugriff.\nDer bisherige Spieler wird getrennt, falls er online ist."),
        "guiReplaceWarning" to ("New Minecraft account: <replacement>\nConfirming removes the previous account's whitelist access." to "Neuer Minecraft-Account: <replacement>\nDie Bestätigung entfernt den Whitelist-Zugriff des bisherigen Accounts."),
        "guiCancelHint" to ("Return without changing the account." to "Kehre zurück, ohne den Account zu ändern."),
        "guiConfirmHint" to ("Click to execute this account action." to "Klicke, um diese Account-Aktion auszuführen."),
        "details" to ("Account details" to "Account-Details"),
        "previous" to ("Previous page" to "Vorherige Seite"),
        "next" to ("Next page" to "Nächste Seite"),
        "back" to ("Back / refresh" to "Zurück / aktualisieren"),
        "remove" to ("Remove account and whitelist access" to "Account und Whitelist-Zugriff entfernen"),
        "replace" to ("Replace account" to "Account ersetzen"),
        "input" to ("Use /discordaccounts gui <entry> <new name/UUID> to confirm replacement here, or /discordaccounts replace <entry> <new name/UUID>." to "Nutze /discordaccounts gui <Eintrag> <neuer Name/UUID> zur Bestätigung hier oder /discordaccounts replace <Eintrag> <neuer Name/UUID>."),
        "confirm" to ("Confirm destructive account action" to "Destruktive Account-Aktion bestätigen"),
        "cancel" to ("Cancel" to "Abbrechen")
    )

    fun register() = strings.forEach { (id, text) ->
        LanguageProvider.registerNewString(
            Key(id),
            mapOf(FileTypes.EN to text.first, FileTypes.DE to text.second)
        )
        // Chat gets its own presentation keys: Discord text and inventory lore stay prefix-free.
        LanguageProvider.registerNewString(
            Key("chat.$id"),
            mapOf(FileTypes.EN to chatStyle(id, text.first), FileTypes.DE to chatStyle(id, text.second))
        )
    }

    private val errors = setOf(
        "permission", "guild", "invalid", "lookup", "different", "claimed", "unlinkedWhitelist",
        "storage", "unexpected", "missing", "stale", "playerOnly", "invalidHelpLink", "postFailed"
    )

    private fun chatStyle(id: String, value: String): String {
        val color = when {
            id in errors -> Colors.RED
            id == "roles" -> Colors.YELLOW
            id in setOf("completed", "success", "replacementSuccess") -> Colors.GREEN
            id == "list" || id == "help" -> Colors.MAIN
            else -> Colors.NEUTRAL
        }
        val base = "<${color.color}>"
        var body = value
        for (placeholder in listOf("name", "uuid", "discord", "display", "page", "pages")) {
            body = body.replace("<$placeholder>", "<${Colors.SECONDARY.color}><$placeholder>$base")
        }
        return "<prefix> $base" + body.replace("\n", "<newline><prefix> $base")
    }

    private data class Key(val id: String) : LanguageProvider.OLDLangStrings {
        override val key = "discord.accounts.$id"
    }

    fun raw(id: String, sender: CommandSender? = null, params: Map<String, Any> = emptyMap()): String =
        if (sender == null) LanguageHandler.getCleanMessage("discord.accounts.$id", params = params)
        else LanguageHandler.getCleanMessage("discord.accounts.$id", sender, params)

    fun send(sender: CommandSender, id: String, params: Map<String, Any> = emptyMap()) =
        sender.sendMessage(
            LanguageHandler.getMessage(
                "discord.accounts.chat.$id", sender,
                params.mapValues { (_, value) ->
                    if (value is String) MiniMessage.miniMessage().escapeTags(value) else value
                })
        )

    fun reason(error: Exception): String = when (error) {
        is AccountService.Failure -> error.reason
        is AccountService.RoleFailure -> "roles"
        else -> {
            Logger.error("Account action failed: ${error.stackTraceToString()}"); "unexpected"
        }
    }

    fun error(error: Exception) = raw(reason(error))
}
