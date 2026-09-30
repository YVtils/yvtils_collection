/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.regionsv2.language

import yv.tils.configv2.language.FileTypes
import yv.tils.configv2.language.LanguageProvider

/** Stable, descriptive keys and named placeholders, following the other modules. */
enum class LangStrings(override val key: String, val english: String, val german: String) :
    LanguageProvider.LangStrings {
    PREVIOUS("action.gui.nav.previousPage", "Previous page", "Vorherige Seite"),
    NEXT("action.gui.nav.nextPage", "Next page", "Nächste Seite"),
    BACK("action.gui.nav.back", "Back", "Zurück"),
    CLOSE("action.regionsv2.gui.close", "Close", "Schließen"),
    CANCEL("action.gui.nav.cancel", "Cancel", "Abbrechen"),
    CONFIRM("action.gui.nav.confirm", "Confirm", "Bestätigen"),
    INPUT("action.regionsv2.gui.input", "Enter a value", "Wert eingeben"),
    CLAIMS("action.regionsv2.gui.claims", "Your claims", "Deine Gebiete"),
    CREATE("action.regionsv2.gui.create", "Create a claim", "Gebiet erstellen"),
    CREATE_LORE(
        "action.regionsv2.gui.create.lore",
        "Select two corners, preview and name your claim.<newline>Protects the entire world height.",
        "Zwei Ecken wählen, Vorschau anzeigen und benennen.<newline>Schützt die gesamte Welthöhe."
    ),
    SERVER("action.regionsv2.gui.server", "Server controls", "Serververwaltung"),
    INSPECT("action.regionsv2.gui.inspect", "Inspect current region", "Aktuelles Gebiet ansehen"),
    PREVIEW("action.regionsv2.gui.preview", "Preview boundary", "Grenze anzeigen"),
    CLEAR("action.regionsv2.gui.clear", "Clear selection", "Auswahl löschen"),
    NAME_INPUT("action.regionsv2.gui.name", "Claim name", "Gebietsname"),
    CORNER("action.regionsv2.gui.corner", "Set position <position>", "Position <position> setzen"),
    CORNER_LORE(
        "action.regionsv2.gui.corner.lore",
        "<position><newline>Click to use your current X/Z position.",
        "<position><newline>Klicken, um deine aktuelle X/Z-Position zu verwenden."
    ),
    UNSELECTED("action.regionsv2.gui.unselected", "Not selected", "Nicht ausgewählt"),
    CREATE_CONFIRM("action.regionsv2.gui.create.confirm", "Confirm claim: <region>", "Gebiet bestätigen: <region>"),
    BOUNDS(
        "action.regionsv2.gui.bounds",
        "World: <world><newline>Size: <x> × <z><newline>Full height: <height> blocks",
        "Welt: <world><newline>Größe: <x> × <z><newline>Gesamthöhe: <height> Blöcke"
    ),
    CLAIM("action.regionsv2.gui.claim", "Claim: <region>", "Gebiet: <region>"),
    CLAIM_LORE(
        "action.regionsv2.gui.claim.lore",
        "World: <world><newline>Role: <role>",
        "Welt: <world><newline>Rolle: <role>"
    ),
    INFORMATION("action.regionsv2.gui.information", "Information", "Informationen"),
    PEOPLE("action.regionsv2.gui.people", "Owners and members", "Besitzer und Mitglieder"),
    ADD_PLAYER("action.regionsv2.gui.player.add", "Add player", "Spieler hinzufügen"),
    PLAYER_INPUT("action.regionsv2.gui.player.input", "Player name / UUID", "Spielername / UUID"),
    CHOOSE_ROLE("action.regionsv2.gui.role.choose", "Choose role", "Rolle wählen"),
    ROLE_OWNER("regionsv2.role.owner", "Owner", "Besitzer"),
    ROLE_MEMBER("regionsv2.role.member", "Member", "Mitglied"),
    ROLE_VISITOR("regionsv2.role.visitor", "Visitor", "Besucher"),
    ROLE_LORE(
        "action.regionsv2.gui.role.lore",
        "Role: <role><newline>Click to change role",
        "Rolle: <role><newline>Klicken, um die Rolle zu ändern"
    ),
    OWNER_LORE(
        "action.regionsv2.gui.role.owner.lore",
        "Owners manage flags, roles and can delete the claim.",
        "Besitzer verwalten Flags und Rollen und können das Gebiet löschen."
    ),
    VISITOR_LORE("action.regionsv2.gui.role.visitor.lore", "Remove explicit membership", "Mitgliedschaft entfernen"),
    FLAGS("action.regionsv2.gui.flags", "<role> flags", "Flags für <role>"),
    GLOBAL("regionsv2.scope.global", "Global", "Global"),
    ROLE_SCOPE("regionsv2.scope.role", "Per role", "Pro Rolle"),
    VALUE(
        "action.regionsv2.gui.value",
        "Value: <value><newline>Click to edit",
        "Wert: <value><newline>Klicken zum Bearbeiten"
    ),
    UNSET("regionsv2.value.unset", "Unset (WorldGuard default)", "Nicht gesetzt (WorldGuard-Standard)"),
    YES("regionsv2.value.yes", "Yes", "Ja"),
    NO("regionsv2.value.no", "No", "Nein"),
    ALLOW("regionsv2.value.allow", "Allow", "Erlauben"),
    DENY("regionsv2.value.deny", "Deny", "Verweigern"),
    DELETE("action.regionsv2.gui.delete", "Delete claim", "Gebiet löschen"),
    DELETE_CONFIRM("action.regionsv2.gui.delete.confirm", "Delete <region>?", "<region> löschen?"),
    DELETE_LORE(
        "action.regionsv2.gui.delete.lore",
        "Removes protection permanently.",
        "Entfernt den Schutz dauerhaft."
    ),
    RENAME("action.regionsv2.gui.rename", "Rename claim", "Gebiet umbenennen"),
    RESIZE("action.regionsv2.gui.resize", "Resize claim", "Gebiet anpassen"),
    RESIZE_LORE(
        "action.regionsv2.gui.resize.lore",
        "Uses your pos1/pos2 selection.",
        "Verwendet deine pos1/pos2-Auswahl."
    ),
    MERGE("action.regionsv2.gui.merge", "Merge claims", "Gebiete zusammenführen"),
    MERGE_INPUT("action.regionsv2.gui.merge.input", "Other claim UUID / name", "UUID / Name des anderen Gebiets"),
    MERGE_LORE(
        "action.regionsv2.gui.merge.lore",
        "Keeps this claim's UUID and name.<newline>Other claim: <region>",
        "Behält UUID und Namen dieses Gebiets.<newline>Anderes Gebiet: <region>"
    ),
    WELCOME("action.regionsv2.gui.welcome", "Welcome action bar", "Willkommensnachricht"),
    GOODBYE("action.regionsv2.gui.goodbye", "Goodbye action bar", "Abschiedsnachricht"),
    MESSAGE_LORE(
        "action.regionsv2.gui.message.lore",
        "Use {region} and {player}.<newline>unset restores the translated default.",
        "Verwende {region} und {player}.<newline>unset stellt den übersetzten Standard wieder her."
    ),
    CONFIG("action.regionsv2.gui.config", "Region configuration", "Gebietskonfiguration"),
    MIN_AREA("action.regionsv2.gui.config.minArea", "Minimum area", "Mindestfläche"),
    MAX_TOTAL("action.regionsv2.gui.config.maxTotal", "Owned claims (all worlds)", "Eigene Gebiete (alle Welten)"),
    MAX_WORLD("action.regionsv2.gui.config.maxWorld", "Owned claims (per world)", "Eigene Gebiete (pro Welt)"),
    MAX_MEMBERS("action.regionsv2.gui.config.maxMembers", "Members per claim", "Mitglieder pro Gebiet"),
    MAX_MEMBERSHIPS(
        "action.regionsv2.gui.config.maxMemberships",
        "Memberships per player",
        "Mitgliedschaften pro Spieler"
    ),
    MAX_VOLUME("action.regionsv2.gui.config.maxVolume", "Maximum volume", "Maximales Volumen"),
    MAX_SIDE("action.regionsv2.gui.config.maxSide", "Maximum side", "Maximale Seitenlänge"),
    LIMIT_LORE(
        "action.regionsv2.gui.config.limit.lore",
        "Value: <value><newline>-1 means unlimited (maximum limits only).",
        "Wert: <value><newline>-1 bedeutet unbegrenzt (nur Maximalgrenzen)."
    ),
    ENABLED("action.regionsv2.gui.config.enabled", "Enabled", "Aktiviert"),
    SURVIVAL("action.regionsv2.gui.config.survival", "Survival only", "Nur Überlebensmodus"),
    TRANSITIONS("action.regionsv2.gui.config.transitions", "Action-bar transitions", "Aktionsleisten-Übergänge"),
    DISABLED_WORLDS("action.regionsv2.gui.config.disabledWorlds", "Disabled worlds", "Deaktivierte Welten"),
    WORLDS_LORE(
        "action.regionsv2.gui.config.worlds.lore",
        "<worlds><newline>Comma-separated world names",
        "<worlds><newline>Weltnamen durch Kommas trennen"
    ),
    POLICIES("action.regionsv2.gui.policies", "WorldGuard flag policies", "WorldGuard-Flag-Richtlinien"),
    POLICY("action.regionsv2.gui.policy", "Policy: <flag>", "Richtlinie: <flag>"),
    POLICY_LORE(
        "action.regionsv2.gui.policy.lore",
        "Type: <type><newline>Owner editing: <enabled><newline>Scope: <scope>",
        "Typ: <type><newline>Besitzerbearbeitung: <enabled><newline>Geltungsbereich: <scope>"
    ),
    RESERVED(
        "action.regionsv2.gui.reserved",
        "Reserved for internal claim protection",
        "Für internen Gebietsschutz reserviert"
    ),
    EDIT_ENABLED("action.regionsv2.gui.policy.enabled", "Owner editing: <enabled>", "Besitzerbearbeitung: <enabled>"),
    EDIT_SCOPE("action.regionsv2.gui.policy.scope", "Scope: <scope>", "Geltungsbereich: <scope>"),
    RESET_LORE(
        "action.regionsv2.gui.policy.reset.lore",
        "Changing this resets all claims to server defaults.",
        "Änderungen setzen alle Gebiete auf Serverstandard zurück."
    ),
    DEFAULT("action.regionsv2.gui.policy.default", "<role> default", "Standard für <role>"),
    CLEAR_DEFAULT("action.regionsv2.gui.policy.clearDefault", "Unset <role> default", "Standard für <role> entfernen"),
    DEFAULT_INPUT("action.regionsv2.gui.policy.input", "Default: <flag>", "Standard: <flag>"),
    HELP(
        "command.regionsv2.help",
        "<prefix> <gray>/regionsv2 info|list|delete|role|flag|resize|merge|rename|message|createat",
        "<prefix> <gray>/regionsv2 info|list|delete|role|flag|resize|merge|rename|message|createat"
    ),
    COMPLETED(
        "command.regionsv2.completed",
        "<prefix> <green>Operation completed.",
        "<prefix> <green>Aktion abgeschlossen."
    ),
    POSITION_SET(
        "command.regionsv2.selection.set",
        "<prefix> <green>Position <position> selected.",
        "<prefix> <green>Position <position> ausgewählt."
    ),
    PREVIEW_STARTED(
        "command.regionsv2.preview",
        "<prefix> <green>Selection preview active for 15 seconds.",
        "<prefix> <green>Auswahlvorschau für 15 Sekunden aktiviert."
    ),
    SELECTION_CLEARED(
        "command.regionsv2.selection.clear",
        "<prefix> <green>Selection cleared.",
        "<prefix> <green>Auswahl gelöscht."
    ),
    ROLE_UPDATED(
        "command.regionsv2.role.updated",
        "<prefix> <green>Role updated.",
        "<prefix> <green>Rolle aktualisiert."
    ),
    RESOLVING(
        "command.regionsv2.profile.resolving",
        "<prefix> <gray>Resolving player profile…",
        "<prefix> <gray>Spielerprofil wird gesucht…"
    ),
    POLICY_SAVED(
        "command.regionsv2.policy.saved",
        "<prefix> <green>Policy saved. Loaded claims updated; other worlds update when loaded.",
        "<prefix> <green>Richtlinie gespeichert. Geladene Gebiete aktualisiert; andere Welten folgen beim Laden."
    ),
    LIST_LINE(
        "command.regionsv2.list.line",
        "<prefix> <white><region>: <uuid> (<world>)",
        "<prefix> <white><region>: <uuid> (<world>)"
    ),
    REGIONS(
        "command.regionsv2.worldguard.found",
        "<prefix> <white>Regions at your location: <regions>",
        "<prefix> <white>Regionen an deiner Position: <regions>"
    ),
    WELCOME_BAR("regionsv2.actionbar.welcome", "<green>Welcome to <region>", "<green>Willkommen in <region>"),
    GOODBYE_BAR("regionsv2.actionbar.goodbye", "<gray>Goodbye from <region>", "<gray>Auf Wiedersehen aus <region>"),
    INFO_NAME("regionsv2.info.name", "Name: <region>", "Name: <region>"),
    INFO_UUID("regionsv2.info.uuid", "ID: <uuid>", "ID: <uuid>"),
    INFO_WORLD("regionsv2.info.world", "World: <world>", "Welt: <world>"),
    INFO_CORNERS("regionsv2.info.corners", "Corners: <corners>", "Ecken: <corners>"),
    INFO_AREA("regionsv2.info.area", "Area: <area> blocks", "Fläche: <area> Blöcke"),
    INFO_CREATED("regionsv2.info.created", "Created: <created>", "Erstellt: <created>"),
    INFO_OWNERS("regionsv2.info.owners", "Owners: <owners>", "Besitzer: <owners>"),
    INFO_MEMBERS("regionsv2.info.members", "Members: <members>", "Mitglieder: <members>"),
    INFO_PLAYERS("regionsv2.info.players", "Players inside: <count>", "Spieler im Gebiet: <count>"),
    INFO_ROLE("regionsv2.info.role", "Role: <role>", "Rolle: <role>"),
    INFO_FLAG("regionsv2.info.flag", "<scope> / <flag>: <value>", "<scope> / <flag>: <value>"),
    INVALID_INPUT("regionsv2.error.input", "Invalid input.", "Ungültige Eingabe."),
    FAILED("regionsv2.error.failed", "Could not complete the action.", "Aktion konnte nicht abgeschlossen werden."),
    ADMIN_REQUIRED(
        "regionsv2.error.admin",
        "Administrator permission required.",
        "Administratorberechtigung erforderlich."
    ),
    MANAGE_DENIED("regionsv2.error.manage", "You cannot manage claims.", "Du darfst keine Gebiete verwalten."),
    CREATE_DENIED("regionsv2.error.create", "You cannot create claims.", "Du darfst keine Gebiete erstellen."),
    DISABLED(
        "regionsv2.error.disabled",
        "Regions are disabled by the server.",
        "Gebiete sind auf diesem Server deaktiviert."
    ),
    OWNER_REQUIRED(
        "regionsv2.error.owner",
        "Only owners can manage this claim.",
        "Nur Besitzer können dieses Gebiet verwalten."
    ),
    STALE_CLAIM(
        "regionsv2.error.stale",
        "This claim no longer exists. Reopen the menu.",
        "Dieses Gebiet existiert nicht mehr. Öffne das Menü erneut."
    ),
    METADATA_MISSING("regionsv2.error.metadata", "Claim metadata is missing.", "Gebietsmetadaten fehlen."),
    POLICY_MISSING("regionsv2.error.policyMissing", "Role policy is missing.", "Rollenrichtlinie fehlt."),
    CLAIM_NOT_FOUND("regionsv2.error.notFound", "Claim not found.", "Gebiet nicht gefunden."),
    AMBIGUOUS(
        "regionsv2.error.ambiguous",
        "Multiple claims match; use the UUID.",
        "Mehrere Gebiete gefunden; verwende die UUID."
    ),
    NO_MANAGER(
        "regionsv2.error.manager",
        "WorldGuard regions are unavailable in this world.",
        "WorldGuard-Gebiete sind in dieser Welt nicht verfügbar."
    ),
    SURVIVAL_REQUIRED(
        "regionsv2.error.survival",
        "Create claims in survival mode.",
        "Erstelle Gebiete im Überlebensmodus."
    ),
    INVALID_NAME(
        "regionsv2.error.name",
        "Use a name of 1–64 printable characters.",
        "Verwende einen Namen mit 1–64 sichtbaren Zeichen."
    ),
    OWNERSHIP_LIMIT(
        "regionsv2.error.ownershipLimit",
        "Claim ownership limit reached.",
        "Maximale Anzahl eigener Gebiete erreicht."
    ),
    WORLD_DISABLED(
        "regionsv2.error.worldDisabled",
        "Claiming is disabled in this world.",
        "Gebiete können in dieser Welt nicht erstellt werden."
    ),
    TOO_SMALL(
        "regionsv2.error.minArea",
        "Selection is smaller than the minimum claim area.",
        "Die Auswahl ist kleiner als die Mindestfläche."
    ),
    TOO_LARGE(
        "regionsv2.error.maxSize",
        "Selection exceeds the server's claim size limits.",
        "Die Auswahl überschreitet die Größenbegrenzung."
    ),
    WORLD_BORDER(
        "regionsv2.error.border",
        "The claim must fit inside the world border.",
        "Das Gebiet muss innerhalb der Weltgrenze liegen."
    ),
    OVERLAP(
        "regionsv2.error.overlap",
        "The selection overlaps an existing protected region.",
        "Die Auswahl überschneidet ein geschütztes Gebiet."
    ),
    CREATE_SAVE_FAILED(
        "regionsv2.error.createSave",
        "Could not save the claim; creation was rolled back.",
        "Gebiet konnte nicht gespeichert werden; Erstellung rückgängig gemacht."
    ),
    LAST_OWNER(
        "regionsv2.error.lastOwner",
        "A claim must retain at least one owner.",
        "Ein Gebiet muss mindestens einen Besitzer behalten."
    ),
    MEMBER_LIMIT(
        "regionsv2.error.memberLimit",
        "Maximum members per claim reached.",
        "Maximale Mitgliederzahl des Gebiets erreicht."
    ),
    MEMBERSHIP_LIMIT(
        "regionsv2.error.membershipLimit",
        "Maximum memberships per player reached.",
        "Maximale Mitgliedschaften des Spielers erreicht."
    ),
    ROLE_SAVE_FAILED(
        "regionsv2.error.roleSave",
        "Could not save the role change; it was rolled back.",
        "Rollenänderung konnte nicht gespeichert werden; rückgängig gemacht."
    ),
    DELETE_SAVE_FAILED(
        "regionsv2.error.deleteSave",
        "Could not save the deletion; it was rolled back.",
        "Löschung konnte nicht gespeichert werden; rückgängig gemacht."
    ),
    SAME_WORLD(
        "regionsv2.error.sameWorld",
        "Claims must be in the same world.",
        "Gebiete müssen in derselben Welt liegen."
    ),
    RESIZE_FAILED("regionsv2.error.resize", "Could not resize the claim.", "Gebiet konnte nicht angepasst werden."),
    MERGE_DISTINCT(
        "regionsv2.error.mergeDistinct",
        "Choose two different claims in the same world.",
        "Wähle zwei verschiedene Gebiete in derselben Welt."
    ),
    MERGE_DOMAINS(
        "regionsv2.error.mergeDomains",
        "Claims must have identical owners and members to merge.",
        "Zusammenzuführende Gebiete müssen gleiche Besitzer und Mitglieder haben."
    ),
    MERGE_FLAGS(
        "regionsv2.error.mergeFlags",
        "Claims must have identical flags and transition messages to merge.",
        "Zusammenzuführende Gebiete müssen gleiche Flags und Übergangsnachrichten haben."
    ),
    MERGE_GEOMETRY(
        "regionsv2.error.mergeGeometry",
        "Claims must form one contiguous rectangle to merge.",
        "Gebiete müssen gemeinsam ein zusammenhängendes Rechteck bilden."
    ),
    MERGE_FAILED("regionsv2.error.merge", "Could not merge claims.", "Gebiete konnten nicht zusammengeführt werden."),
    INVALID_MESSAGE(
        "regionsv2.error.message",
        "Message must be at most 160 printable characters.",
        "Nachrichten dürfen höchstens 160 sichtbare Zeichen enthalten."
    ),
    FLAG_CHANGED(
        "regionsv2.error.flagChanged",
        "Editing or scope of this flag has changed. Reopen the menu.",
        "Bearbeitung oder Geltungsbereich hat sich geändert. Öffne das Menü erneut."
    ),
    FLAG_SAVE_FAILED(
        "regionsv2.error.flagSave",
        "Could not save flag change.",
        "Flag-Änderung konnte nicht gespeichert werden."
    ),
    INVALID_FLAG("regionsv2.error.flagValue", "Invalid flag value.", "Ungültiger Flag-Wert."),
    FLAG_INPUT("regionsv2.error.flagInput", "This flag needs value input.", "Dieses Flag benötigt eine Werteingabe."),
    FLAG_RESERVED(
        "regionsv2.error.flagReserved",
        "This flag is reserved for claim protection.",
        "Dieses Flag ist für den Gebietsschutz reserviert."
    ),
    FLAG_SCOPE(
        "regionsv2.error.flagScope",
        "WorldGuard does not support role groups for this flag.",
        "WorldGuard unterstützt keine Rollengruppen für dieses Flag."
    ),
    SELECTION_DENIED(
        "regionsv2.error.selection",
        "Claim selection is unavailable.",
        "Gebietsauswahl ist nicht verfügbar."
    ),
    BUILD_HEIGHT(
        "regionsv2.error.height",
        "Select a corner inside the world's build height.",
        "Wähle eine Ecke innerhalb der Bauhöhe."
    ),
    SELECTION_WORLD(
        "regionsv2.error.selectionWorld",
        "Clear your selection before selecting another world.",
        "Lösche die Auswahl, bevor du eine andere Welt wählst."
    ),
    SELECT_FIRST(
        "regionsv2.error.pos1",
        "Select the first corner with /regionsv2 pos1.",
        "Wähle die erste Ecke mit /regionsv2 pos1."
    ),
    SELECT_SECOND(
        "regionsv2.error.pos2",
        "Select the second corner with /regionsv2 pos2.",
        "Wähle die zweite Ecke mit /regionsv2 pos2."
    ),
    SELECT_CORNERS(
        "regionsv2.error.corners",
        "Use /regionsv2 pos1 and pos2 first.",
        "Verwende zuerst /regionsv2 pos1 und pos2."
    ),
    PREVIEW_WORLD(
        "regionsv2.error.previewWorld",
        "Visit the claim's world to preview its boundary.",
        "Besuche die Welt des Gebiets, um die Grenze anzuzeigen."
    ),
    NO_CLAIM_HERE("regionsv2.error.location", "No claim at your location.", "Kein Gebiet an deiner Position."),
    PROFILE_FAILED(
        "regionsv2.error.profile",
        "Player profile could not be resolved.",
        "Spielerprofil konnte nicht gefunden werden."
    ),
    CONSOLE_CLAIM(
        "regionsv2.error.consoleClaim",
        "Console must specify a claim.",
        "Die Konsole muss ein Gebiet angeben."
    ),
    WORLD_NOT_FOUND("regionsv2.error.world", "World not found.", "Welt nicht gefunden."),
    UNKNOWN_FLAG("regionsv2.error.flag", "Unknown flag.", "Unbekanntes Flag."),
    INVALID_ROLE("regionsv2.error.role", "Invalid role.", "Ungültige Rolle."),
    CONFIG_SAVE_FAILED(
        "regionsv2.error.configSave",
        "Could not save region configuration.",
        "Gebietskonfiguration konnte nicht gespeichert werden."
    ),
    POLICY_SAVE_FAILED(
        "regionsv2.error.policySave",
        "Could not apply server defaults to all claims.",
        "Serverstandard konnte nicht auf alle Gebiete angewendet werden."
    ),
    INVALID_LIMITS(
        "regionsv2.error.limits",
        "Limits must be non-negative or -1 (unlimited); minimum area must be positive.",
        "Grenzen müssen nicht negativ oder -1 (unbegrenzt) sein; die Mindestfläche muss positiv sein."
    );

    override val translations: Map<FileTypes, String>
        get() {
            val style = when {
                key.startsWith("regionsv2.error.") -> "<prefix> <red>"
                key.startsWith("action.regionsv2.") && !key.endsWith(".lore") -> "<aqua>"
                key.endsWith(".lore") -> "<gray>"
                else -> ""
            }
            return mapOf(FileTypes.EN to style + english, FileTypes.DE to style + german)
        }
}
