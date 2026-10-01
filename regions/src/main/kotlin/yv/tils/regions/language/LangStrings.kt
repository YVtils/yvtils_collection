/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.regions.language

import yv.tils.configv2.language.FileTypes
import yv.tils.configv2.language.LanguageProvider
import yv.tils.utils.colors.Colors

/** Stable, descriptive keys and named placeholders, following the other modules. */
enum class LangStrings(override val key: String, val english: String, val german: String) :
    LanguageProvider.LangStrings {
    PREVIOUS("action.gui.nav.previousPage", "Previous page", "Vorherige Seite"),
    NEXT("action.gui.nav.nextPage", "Next page", "Nächste Seite"),
    BACK("action.gui.nav.back", "Back", "Zurück"),
    CLOSE("action.regions.gui.close", "Close", "Schließen"),
    CANCEL("action.gui.nav.cancel", "Cancel", "Abbrechen"),
    CONFIRM("action.gui.nav.confirm", "Confirm", "Bestätigen"),
    INPUT("action.regions.gui.input", "Enter a value", "Wert eingeben"),
    CLAIMS("action.regions.gui.claims", "Your claims", "Deine Gebiete"),
    OTHER_CLAIMS("action.regions.gui.otherClaims.title", "Other players' regions", "Gebiete anderer Spieler"),
    OTHER_CLAIMS_LORE(
        "action.regions.gui.otherClaims.lore",
        "Admin view: claims you do not own or belong to.",
        "Adminansicht: Gebiete ohne deine Besitzer- oder Mitgliedschaft."
    ),
    PERSONAL_CLAIMS_LORE(
        "action.regions.gui.personalClaims.lore",
        "Show only claims you own or belong to.",
        "Nur Gebiete mit deiner Besitzer- oder Mitgliedschaft anzeigen."
    ),
    NO_OTHER_CLAIMS("action.regions.gui.noOtherClaims", "No other players' regions", "Keine Gebiete anderer Spieler"),
    MAIN_MENU("action.regions.gui.main", "Regions", "Gebiete"),
    SUBZONES("action.regions.gui.subzones", "3D subzones", "3D-Untergebiete"),
    SUBZONE_INHERIT("regions.value.inherit", "Inherited from parent claim", "Vom Hauptgebiet geerbt"),
    SUBZONE_CREATE("action.regions.gui.subzone.create", "Create 3D subzone", "3D-Untergebiet erstellen"),
    SUBZONE_TITLE("action.regions.gui.subzone.title", "Subzone: <zone>", "Untergebiet: <zone>"),
    SUBZONE_BOUNDS("action.regions.gui.subzone.bounds.lore", "3D corners: <corners>", "3D-Ecken: <corners>"),
    SUBZONE_CORNER_LORE(
        "action.regions.gui.subzone.corner.lore",
        "<position><newline>Click to use your current X/Y/Z position inside this claim.",
        "<position><newline>Klicken, um deine aktuelle X/Y/Z-Position in diesem Gebiet zu verwenden."
    ),
    SUBZONE_FLAGS_LORE(
        "action.regions.gui.subzone.flags.lore",
        "Override this group locally.<newline>Right-click unset restores the parent claim's value.",
        "Diese Gruppe lokal überschreiben.<newline>Rechtsklick stellt den Wert des Hauptgebiets wieder her."
    ),
    SUBZONE_OPEN("action.regions.gui.subzone.open.title", "Open protection", "Schutz öffnen"),
    SUBZONE_OPEN_LORE(
        "action.regions.gui.subzone.open.lore",
        "Allows region state flags and building for EVERYONE here.<newline>Plugin-wide settings and boundary checks may still apply.<newline>Disabling restores inherited flags and your overrides.",
        "Erlaubt Zustands-Flags und Bauen für ALLE in diesem Bereich.<newline>Pluginweite Einstellungen und Grenzprüfungen gelten ggf. weiterhin.<newline>Deaktivieren stellt geerbte Flags und deine Ausnahmen wieder her."
    ),
    SUBZONE_MISSING(
        "regions.error.subzoneMissing",
        "Subzone no longer exists. Reopen the menu.",
        "Untergebiet existiert nicht mehr. Öffne das Menü erneut."
    ),
    SUBZONE_OUTSIDE(
        "regions.error.subzoneOutside",
        "Every subzone must fit fully inside its parent claim, including height.",
        "Jedes Untergebiet muss vollständig im Hauptgebiet liegen, einschließlich Höhe."
    ),
    SUBZONE_OVERLAP(
        "regions.error.subzoneOverlap",
        "Subzones cannot overlap one another.",
        "Untergebiete dürfen sich nicht überschneiden."
    ),
    SUBZONE_LIMIT(
        "regions.error.subzoneLimit",
        "Maximum subzones per claim reached.",
        "Maximale Anzahl an Untergebieten erreicht."
    ),
    SUBZONE_SELECT(
        "regions.error.subzoneSelect",
        "Select both 3D corners in the subzone creation menu first.",
        "Wähle zuerst beide 3D-Ecken im Untergebietsmenü."
    ),
    SUBZONE_OPEN_DISABLED(
        "regions.error.subzoneOpenDisabled",
        "Open-protection subzones are disabled by the server.",
        "Untergebiete mit offenem Schutz sind serverseitig deaktiviert."
    ),
    SUBZONE_OPEN_ACTIVE(
        "regions.error.subzoneOpenActive",
        "Disable open protection before editing local flag overrides.",
        "Deaktiviere den offenen Schutz, bevor du lokale Flag-Ausnahmen bearbeitest."
    ),
    SUBZONE_SAVE_FAILED(
        "regions.error.subzoneSave",
        "Could not save subzones; the change was rolled back.",
        "Untergebiete konnten nicht gespeichert werden; Änderung rückgängig gemacht."
    ),
    FLAGS_TAB("action.regions.gui.tabs.flags", "Flags", "Flags"),
    SETTINGS_TAB("action.regions.gui.tabs.settings", "Settings", "Einstellungen"),
    ACTIVE_TAB("action.regions.gui.tabs.active.lore", "Current page", "Aktuelle Seite"),
    CREATE("action.regions.gui.create.title", "Create a claim", "Gebiet erstellen"),
    CREATE_LORE(
        "action.regions.gui.create.lore",
        "Select two corners, preview and name your claim.<newline>Protects the entire world height.",
        "Zwei Ecken wählen, Vorschau anzeigen und benennen.<newline>Schützt die gesamte Welthöhe."
    ),
    SERVER("action.regions.gui.server", "Server controls", "Serververwaltung"),
    INSPECT("action.regions.gui.inspect", "Current region", "Aktuelles Gebiet"),
    PREVIEW("action.regions.gui.preview", "Preview boundary", "Grenze anzeigen"),
    CLEAR("action.regions.gui.clear", "Clear selection", "Auswahl löschen"),
    NAME_INPUT("action.regions.gui.name", "Claim name", "Gebietsname"),
    CORNER("action.regions.gui.corner.title", "Set position <position>", "Position <position> setzen"),
    CORNER_LORE(
        "action.regions.gui.corner.lore",
        "<position><newline>Click to use your current X/Z position.",
        "<position><newline>Klicken, um deine aktuelle X/Z-Position zu verwenden."
    ),
    UNSELECTED("action.regions.gui.unselected", "Not selected", "Nicht ausgewählt"),
    CREATE_CONFIRM("action.regions.gui.create.confirm", "Confirm claim: <region>", "Gebiet bestätigen: <region>"),
    BOUNDS(
        "action.regions.gui.bounds",
        "World: <world><newline>Size: <x> × <z><newline>Full height: <height> blocks",
        "Welt: <world><newline>Größe: <x> × <z><newline>Gesamthöhe: <height> Blöcke"
    ),
    CLAIM("action.regions.gui.claim.title", "Claim: <region>", "Gebiet: <region>"),
    CLAIM_LORE(
        "action.regions.gui.claim.lore",
        "World: <world><newline>Role: <role><newline>Size: <x> × <z> blocks",
        "Welt: <world><newline>Rolle: <role><newline>Größe: <x> × <z> Blöcke"
    ),
    INFORMATION("action.regions.gui.information", "Information", "Informationen"),
    BASIC_INFO("action.regions.gui.basicInfo", "Basic information", "Grundinformationen"),
    LOCATION_INFO("action.regions.gui.locationInfo", "Location and boundary", "Lage und Grenze"),
    HIGHLIGHT_LORE(
        "action.regions.gui.highlight.lore",
        "Click to highlight the boundary.",
        "Klicken, um die Grenze anzuzeigen."
    ),
    NO_FLAGS("action.regions.gui.noFlags", "No explicit flags set", "Keine Flags ausdrücklich gesetzt"),
    FLAG_SUMMARY("action.regions.gui.flagSummary.lore", "<flag>: <value>", "<flag>: <value>"),
    EDIT_FLAGS_LORE(
        "action.regions.gui.editFlags.lore",
        "Click to edit this group's flags.",
        "Klicken, um die Flags dieser Gruppe zu bearbeiten."
    ),
    OVERVIEW("action.regions.gui.overview.title", "Overview", "Übersicht"),
    OVERVIEW_LORE(
        "action.regions.gui.overview.lore",
        "Claims shown: <count><newline>World: <world>",
        "Angezeigte Gebiete: <count><newline>Welt: <world>"
    ),
    EMPTY_CLAIMS("action.regions.gui.empty", "No claims yet", "Noch keine Gebiete"),
    PAGE("action.regions.gui.page", "Page <page> / <pages>", "Seite <page> / <pages>"),
    RESTORE_DEFAULTS(
        "action.regions.gui.defaults.title",
        "Restore server flag defaults",
        "Server-Flag-Standard wiederherstellen"
    ),
    RESTORE_LORE(
        "action.regions.gui.defaults.lore",
        "Replaces all custom flags in this claim.<newline>Owners and members are preserved.",
        "Ersetzt alle eigenen Flags dieses Gebiets.<newline>Besitzer und Mitglieder bleiben erhalten."
    ),
    GEOMETRY("action.regions.gui.geometry", "Name and boundaries", "Name und Grenzen"),
    MESSAGES("action.regions.gui.messages", "Transition messages", "Übergangsnachrichten"),
    CURRENCY("action.regions.gui.config.currency", "Diamond pricing", "Diamantpreise"),
    FREE_CHUNKS(
        "action.regions.gui.config.freeChunks",
        "Free size (chunks per side)",
        "Kostenlose Größe (Chunks pro Seite)"
    ),
    DIAMONDS_PER_CHUNK("action.regions.gui.config.diamonds", "Diamonds per size tier", "Diamanten pro Größenstufe"),
    COST("regions.currency.cost", "Cost: <cost> diamonds", "Kosten: <cost> Diamanten"),
    COST_LORE(
        "action.regions.gui.cost.lore",
        "Cost: <cost> diamonds<newline>Payment uses diamonds in your inventory.<newline>No refunds for shrinking or deleting.",
        "Kosten: <cost> Diamanten<newline>Bezahlung mit Diamanten aus deinem Inventar.<newline>Keine Erstattung bei Verkleinerung oder Löschung."
    ),
    INSUFFICIENT_CURRENCY(
        "regions.error.currency",
        "You need <cost> diamonds; your inventory contains <available>.",
        "Du benötigst <cost> Diamanten; dein Inventar enthält <available>."
    ),
    PEOPLE("action.regions.gui.people", "Owners and members", "Besitzer und Mitglieder"),
    ADD_PLAYER("action.regions.gui.player.add", "Add player", "Spieler hinzufügen"),
    PLAYER_INPUT("action.regions.gui.player.input", "Player name / UUID", "Spielername / UUID"),
    CHOOSE_ROLE("action.regions.gui.role.choose", "Choose role", "Rolle wählen"),
    ROLE_OWNER("regions.role.owner", "Owner", "Besitzer"),
    ROLE_MEMBER("regions.role.member", "Member", "Mitglied"),
    ROLE_VISITOR("regions.role.visitor", "Visitor", "Besucher"),
    ROLE_LORE(
        "action.regions.gui.role.lore",
        "Role: <role><newline>Click to change role",
        "Rolle: <role><newline>Klicken, um die Rolle zu ändern"
    ),
    OWNER_LORE(
        "action.regions.gui.role.owner.lore",
        "Owners manage flags, roles and can delete the claim.",
        "Besitzer verwalten Flags und Rollen und können das Gebiet löschen."
    ),
    VISITOR_LORE("action.regions.gui.role.visitor.lore", "Remove explicit membership", "Mitgliedschaft entfernen"),
    FLAGS("action.regions.gui.flags", "<role> flags", "Flags für <role>"),
    GLOBAL("regions.scope.global", "Global", "Global"),
    ROLE_SCOPE("regions.scope.role", "Per role", "Pro Rolle"),
    VALUE(
        "action.regions.gui.value",
        "Value: <value><newline>Click to edit",
        "Wert: <value><newline>Klicken zum Bearbeiten"
    ),
    CURRENT_VALUE("action.regions.gui.currentValue.lore", "Value: <value>", "Wert: <value>"),
    DEFAULT_VALUE("action.regions.gui.defaultValue.lore", "Default: <value>", "Standard: <value>"),
    EDIT_TEXT_CONTROL("action.regions.gui.textControl.lore", "Left-click: edit text", "Linksklick: Text bearbeiten"),
    RESET_CONTROL("action.regions.gui.resetControl.lore", "Right-click: unset", "Rechtsklick: entfernen"),
    UNSET("regions.value.unset", "Unset (WorldGuard default)", "Nicht gesetzt (WorldGuard-Standard)"),
    YES("regions.value.yes", "Yes", "Ja"),
    NO("regions.value.no", "No", "Nein"),
    ALLOW("regions.value.allow", "Allow", "Erlauben"),
    DENY("regions.value.deny", "Deny", "Verweigern"),
    DELETE("action.regions.gui.delete.title", "Delete claim", "Gebiet löschen"),
    DELETE_CONFIRM("action.regions.gui.delete.confirm", "Delete <region>?", "<region> löschen?"),
    DELETE_LORE(
        "action.regions.gui.delete.lore",
        "Removes protection permanently.",
        "Entfernt den Schutz dauerhaft."
    ),
    RENAME("action.regions.gui.rename", "Rename claim", "Gebiet umbenennen"),
    RESIZE("action.regions.gui.resize.title", "Resize claim", "Gebiet anpassen"),
    RESIZE_LORE(
        "action.regions.gui.resize.lore",
        "Uses your pos1/pos2 selection.",
        "Verwendet deine pos1/pos2-Auswahl."
    ),
    MERGE("action.regions.gui.merge.title", "Merge claims", "Gebiete zusammenführen"),
    MERGE_INPUT("action.regions.gui.merge.input", "Other claim UUID / name", "UUID / Name des anderen Gebiets"),
    MERGE_LORE(
        "action.regions.gui.merge.lore",
        "Keeps this claim's UUID and name.<newline>Other claim: <region>",
        "Behält UUID und Namen dieses Gebiets.<newline>Anderes Gebiet: <region>"
    ),
    WELCOME("action.regions.gui.welcome", "Welcome action bar", "Willkommensnachricht"),
    GOODBYE("action.regions.gui.goodbye", "Goodbye action bar", "Abschiedsnachricht"),
    MESSAGE_LORE(
        "action.regions.gui.message.lore",
        "Use {region} and {player}.<newline>unset restores the translated default.",
        "Verwende {region} und {player}.<newline>unset stellt den übersetzten Standard wieder her."
    ),
    CONFIG("action.regions.gui.config.title", "Region configuration", "Gebietskonfiguration"),
    MIN_AREA("action.regions.gui.config.minArea", "Minimum area", "Mindestfläche"),
    MAX_TOTAL("action.regions.gui.config.maxTotal", "Owned claims (all worlds)", "Eigene Gebiete (alle Welten)"),
    MAX_WORLD("action.regions.gui.config.maxWorld", "Owned claims (per world)", "Eigene Gebiete (pro Welt)"),
    MAX_MEMBERS("action.regions.gui.config.maxMembers", "Members per claim", "Mitglieder pro Gebiet"),
    MAX_MEMBERSHIPS(
        "action.regions.gui.config.maxMemberships",
        "Memberships per player",
        "Mitgliedschaften pro Spieler"
    ),
    MAX_VOLUME("action.regions.gui.config.maxVolume", "Maximum volume", "Maximales Volumen"),
    MAX_SIDE("action.regions.gui.config.maxSide", "Maximum side", "Maximale Seitenlänge"),
    LIMIT_LORE(
        "action.regions.gui.config.limit.lore",
        "Value: <value><newline>-1 means unlimited (maximum limits only).",
        "Wert: <value><newline>-1 bedeutet unbegrenzt (nur Maximalgrenzen)."
    ),
    ENABLED("action.regions.gui.config.enabled", "Enabled", "Aktiviert"),
    SURVIVAL("action.regions.gui.config.survival", "Survival only", "Nur Überlebensmodus"),
    TRANSITIONS("action.regions.gui.config.transitions", "Action-bar transitions", "Aktionsleisten-Übergänge"),
    DISABLED_WORLDS("action.regions.gui.config.disabledWorlds.title", "Disabled worlds", "Deaktivierte Welten"),
    WORLDS_LORE(
        "action.regions.gui.config.worlds.lore",
        "<worlds><newline>Comma-separated world names",
        "<worlds><newline>Weltnamen durch Kommas trennen"
    ),
    POLICIES("action.regions.gui.policies", "WorldGuard flag policies", "WorldGuard-Flag-Richtlinien"),
    POLICY("action.regions.gui.policy.title", "Policy: <flag>", "Richtlinie: <flag>"),
    POLICY_LORE(
        "action.regions.gui.policy.lore",
        "Type: <type><newline>Owner editing: <enabled><newline>Scope: <scope>",
        "Typ: <type><newline>Besitzerbearbeitung: <enabled><newline>Geltungsbereich: <scope>"
    ),
    RESERVED(
        "action.regions.gui.reserved",
        "Reserved for internal claim protection",
        "Für internen Gebietsschutz reserviert"
    ),
    EDIT_ENABLED("action.regions.gui.policy.enabled", "Owner editing: <enabled>", "Besitzerbearbeitung: <enabled>"),
    EDIT_SCOPE("action.regions.gui.policy.scope", "Scope: <scope>", "Geltungsbereich: <scope>"),
    RESET_LORE(
        "action.regions.gui.policy.reset.lore",
        "Changing this resets all claims to server defaults.",
        "Änderungen setzen alle Gebiete auf Serverstandard zurück."
    ),
    DEFAULT("action.regions.gui.policy.default.title", "<role> default", "Standard für <role>"),
    DEFAULT_SUMMARY(
        "action.regions.gui.policy.default.lore",
        "<role> default: <value>",
        "Standard für <role>: <value>"
    ),
    CLEAR_DEFAULT("action.regions.gui.policy.clearDefault", "Unset <role> default", "Standard für <role> entfernen"),
    DEFAULT_INPUT("action.regions.gui.policy.input", "Default: <flag>", "Standard: <flag>"),
    HELP(
        "command.regions.help",
        "<prefix> <gray>/region pos1|pos2|preview|cost|create|info|list|delete|role|flag|resize|merge|rename|message|createat",
        "<prefix> <gray>/region pos1|pos2|preview|cost|create|info|list|delete|role|flag|resize|merge|rename|message|createat"
    ),
    COMPLETED(
        "command.regions.completed",
        "<prefix> <green>Operation completed.",
        "<prefix> <green>Aktion abgeschlossen."
    ),
    POSITION_SET(
        "command.regions.selection.set",
        "<prefix> <green>Position <position> selected.",
        "<prefix> <green>Position <position> ausgewählt."
    ),
    PREVIEW_STARTED(
        "command.regions.preview",
        "<prefix> <green>Selection preview active for 15 seconds.",
        "<prefix> <green>Auswahlvorschau für 15 Sekunden aktiviert."
    ),
    SELECTION_CLEARED(
        "command.regions.selection.clear",
        "<prefix> <green>Selection cleared.",
        "<prefix> <green>Auswahl gelöscht."
    ),
    ROLE_UPDATED(
        "command.regions.role.updated",
        "<prefix> <green>Role updated.",
        "<prefix> <green>Rolle aktualisiert."
    ),
    RESOLVING(
        "command.regions.profile.resolving",
        "<prefix> <gray>Resolving player profile…",
        "<prefix> <gray>Spielerprofil wird gesucht…"
    ),
    POLICY_SAVED(
        "command.regions.policy.saved",
        "<prefix> <green>Policy saved. Loaded claims updated; other worlds update when loaded.",
        "<prefix> <green>Richtlinie gespeichert. Geladene Gebiete aktualisiert; andere Welten folgen beim Laden."
    ),
    LIST_LINE(
        "command.regions.list.line",
        "<prefix> <white><region>: <uuid> (<world>)",
        "<prefix> <white><region>: <uuid> (<world>)"
    ),
    REGIONS(
        "command.regions.worldguard.found",
        "<prefix> <white>Regions at your location: <regions>",
        "<prefix> <white>Regionen an deiner Position: <regions>"
    ),
    WELCOME_BAR("regions.actionbar.welcome", "<green>Welcome to <region>", "<green>Willkommen in <region>"),
    GOODBYE_BAR("regions.actionbar.goodbye", "<gray>Goodbye from <region>", "<gray>Auf Wiedersehen aus <region>"),
    INFO_NAME("regions.info.name", "Name: <region>", "Name: <region>"),
    CURRENT_AREA_SUMMARY(
        "command.regions.currentArea.summary",
        "<prefix> <white>Current region: <${Colors.SECONDARY.color}><region><newline>" +
                "  <white>Owners: <${Colors.TERTIARY.color}><owners><newline>" +
                "  <white>Created: <${Colors.TERTIARY.color}><created><newline>" +
                "  <white>World: <${Colors.TERTIARY.color}><world> <dark_gray>• <white>Size: <${Colors.TERTIARY.color}><x> × <z> blocks<newline>" +
                "  <white>Corners: <${Colors.TERTIARY.color}><corners>",
        "<prefix> <white>Aktuelles Gebiet: <${Colors.SECONDARY.color}><region><newline>" +
                "  <white>Besitzer: <${Colors.TERTIARY.color}><owners><newline>" +
                "  <white>Erstellt: <${Colors.TERTIARY.color}><created><newline>" +
                "  <white>Welt: <${Colors.TERTIARY.color}><world> <dark_gray>• <white>Größe: <${Colors.TERTIARY.color}><x> × <z> Blöcke<newline>" +
                "  <white>Ecken: <${Colors.TERTIARY.color}><corners>"
    ),
    INFO_UUID("regions.info.uuid", "ID: <uuid>", "ID: <uuid>"),
    INFO_WORLD("regions.info.world", "World: <world>", "Welt: <world>"),
    INFO_CORNERS("regions.info.corners", "Corners: <corners>", "Ecken: <corners>"),
    INFO_AREA("regions.info.area", "Area: <area> blocks", "Fläche: <area> Blöcke"),
    INFO_SIZE("regions.info.size", "Size: <x> × <z> blocks", "Größe: <x> × <z> Blöcke"),
    INFO_CREATED("regions.info.created", "Created: <created>", "Erstellt: <created>"),
    INFO_OWNERS("regions.info.owners", "Owners: <owners>", "Besitzer: <owners>"),
    INFO_MEMBERS("regions.info.members", "Members: <members>", "Mitglieder: <members>"),
    INFO_PLAYERS("regions.info.players", "Players inside: <count>", "Spieler im Gebiet: <count>"),
    INFO_ROLE("regions.info.role", "Role: <role>", "Rolle: <role>"),
    INFO_FLAG("regions.info.flag", "<scope> / <flag>: <value>", "<scope> / <flag>: <value>"),
    INVALID_INPUT("regions.error.input", "Invalid input.", "Ungültige Eingabe."),
    FAILED("regions.error.failed", "Could not complete the action.", "Aktion konnte nicht abgeschlossen werden."),
    ADMIN_REQUIRED(
        "regions.error.admin",
        "Administrator permission required.",
        "Administratorberechtigung erforderlich."
    ),
    MANAGE_DENIED("regions.error.manage", "You cannot manage claims.", "Du darfst keine Gebiete verwalten."),
    PERMISSION_DENIED(
        "regions.error.permission",
        "You do not have permission for this action (<permission>).",
        "Du hast keine Berechtigung für diese Aktion (<permission>)."
    ),
    CREATE_DENIED("regions.error.create", "You cannot create claims.", "Du darfst keine Gebiete erstellen."),
    DISABLED(
        "regions.error.disabled",
        "Regions are disabled by the server.",
        "Gebiete sind auf diesem Server deaktiviert."
    ),
    OWNER_REQUIRED(
        "regions.error.owner",
        "Only owners can manage this claim.",
        "Nur Besitzer können dieses Gebiet verwalten."
    ),
    STALE_CLAIM(
        "regions.error.stale",
        "This claim no longer exists. Reopen the menu.",
        "Dieses Gebiet existiert nicht mehr. Öffne das Menü erneut."
    ),
    METADATA_MISSING("regions.error.metadata", "Claim metadata is missing.", "Gebietsmetadaten fehlen."),
    POLICY_MISSING("regions.error.policyMissing", "Role policy is missing.", "Rollenrichtlinie fehlt."),
    CLAIM_NOT_FOUND("regions.error.notFound", "Claim not found.", "Gebiet nicht gefunden."),
    AMBIGUOUS(
        "regions.error.ambiguous",
        "Multiple claims match; use the UUID.",
        "Mehrere Gebiete gefunden; verwende die UUID."
    ),
    NO_MANAGER(
        "regions.error.manager",
        "WorldGuard regions are unavailable in this world.",
        "WorldGuard-Gebiete sind in dieser Welt nicht verfügbar."
    ),
    SURVIVAL_REQUIRED(
        "regions.error.survival",
        "Create claims in survival mode.",
        "Erstelle Gebiete im Überlebensmodus."
    ),
    INVALID_NAME(
        "regions.error.name",
        "Use a name of 1–64 printable characters.",
        "Verwende einen Namen mit 1–64 sichtbaren Zeichen."
    ),
    OWNERSHIP_LIMIT(
        "regions.error.ownershipLimit",
        "Claim ownership limit reached.",
        "Maximale Anzahl eigener Gebiete erreicht."
    ),
    WORLD_DISABLED(
        "regions.error.worldDisabled",
        "Claiming is disabled in this world.",
        "Gebiete können in dieser Welt nicht erstellt werden."
    ),
    TOO_SMALL(
        "regions.error.minArea",
        "Selection is smaller than the minimum claim area.",
        "Die Auswahl ist kleiner als die Mindestfläche."
    ),
    TOO_LARGE(
        "regions.error.maxSize",
        "Selection exceeds the server's claim size limits.",
        "Die Auswahl überschreitet die Größenbegrenzung."
    ),
    WORLD_BORDER(
        "regions.error.border",
        "The claim must fit inside the world border.",
        "Das Gebiet muss innerhalb der Weltgrenze liegen."
    ),
    OVERLAP(
        "regions.error.overlap",
        "The selection overlaps an existing protected region.",
        "Die Auswahl überschneidet ein geschütztes Gebiet."
    ),
    CREATE_SAVE_FAILED(
        "regions.error.createSave",
        "Could not save the claim; creation was rolled back.",
        "Gebiet konnte nicht gespeichert werden; Erstellung rückgängig gemacht."
    ),
    LAST_OWNER(
        "regions.error.lastOwner",
        "A claim must retain at least one owner.",
        "Ein Gebiet muss mindestens einen Besitzer behalten."
    ),
    MEMBER_LIMIT(
        "regions.error.memberLimit",
        "Maximum members per claim reached.",
        "Maximale Mitgliederzahl des Gebiets erreicht."
    ),
    MEMBERSHIP_LIMIT(
        "regions.error.membershipLimit",
        "Maximum memberships per player reached.",
        "Maximale Mitgliedschaften des Spielers erreicht."
    ),
    ROLE_SAVE_FAILED(
        "regions.error.roleSave",
        "Could not save the role change; it was rolled back.",
        "Rollenänderung konnte nicht gespeichert werden; rückgängig gemacht."
    ),
    DELETE_SAVE_FAILED(
        "regions.error.deleteSave",
        "Could not save the deletion; it was rolled back.",
        "Löschung konnte nicht gespeichert werden; rückgängig gemacht."
    ),
    SAME_WORLD(
        "regions.error.sameWorld",
        "Claims must be in the same world.",
        "Gebiete müssen in derselben Welt liegen."
    ),
    RESIZE_FAILED("regions.error.resize", "Could not resize the claim.", "Gebiet konnte nicht angepasst werden."),
    MERGE_DISTINCT(
        "regions.error.mergeDistinct",
        "Choose two different claims in the same world.",
        "Wähle zwei verschiedene Gebiete in derselben Welt."
    ),
    MERGE_DOMAINS(
        "regions.error.mergeDomains",
        "Claims must have identical owners and members to merge.",
        "Zusammenzuführende Gebiete müssen gleiche Besitzer und Mitglieder haben."
    ),
    MERGE_FLAGS(
        "regions.error.mergeFlags",
        "Claims must have identical flags and transition messages to merge.",
        "Zusammenzuführende Gebiete müssen gleiche Flags und Übergangsnachrichten haben."
    ),
    MERGE_GEOMETRY(
        "regions.error.mergeGeometry",
        "Claims must form one contiguous rectangle to merge.",
        "Gebiete müssen gemeinsam ein zusammenhängendes Rechteck bilden."
    ),
    MERGE_FAILED("regions.error.merge", "Could not merge claims.", "Gebiete konnten nicht zusammengeführt werden."),
    INVALID_MESSAGE(
        "regions.error.message",
        "Message must be at most 160 printable characters.",
        "Nachrichten dürfen höchstens 160 sichtbare Zeichen enthalten."
    ),
    FLAG_CHANGED(
        "regions.error.flagChanged",
        "Editing or scope of this flag has changed. Reopen the menu.",
        "Bearbeitung oder Geltungsbereich hat sich geändert. Öffne das Menü erneut."
    ),
    FLAG_SAVE_FAILED(
        "regions.error.flagSave",
        "Could not save flag change.",
        "Flag-Änderung konnte nicht gespeichert werden."
    ),
    INVALID_FLAG("regions.error.flagValue", "Invalid flag value.", "Ungültiger Flag-Wert."),
    FLAG_INPUT("regions.error.flagInput", "This flag needs value input.", "Dieses Flag benötigt eine Werteingabe."),
    FLAG_RESERVED(
        "regions.error.flagReserved",
        "This flag is reserved for claim protection.",
        "Dieses Flag ist für den Gebietsschutz reserviert."
    ),
    FLAG_SCOPE(
        "regions.error.flagScope",
        "WorldGuard does not support role groups for this flag.",
        "WorldGuard unterstützt keine Rollengruppen für dieses Flag."
    ),
    SELECTION_DENIED(
        "regions.error.selection",
        "Claim selection is unavailable.",
        "Gebietsauswahl ist nicht verfügbar."
    ),
    BUILD_HEIGHT(
        "regions.error.height",
        "Select a corner inside the world's build height.",
        "Wähle eine Ecke innerhalb der Bauhöhe."
    ),
    SELECTION_WORLD(
        "regions.error.selectionWorld",
        "Clear your selection before selecting another world.",
        "Lösche die Auswahl, bevor du eine andere Welt wählst."
    ),
    SELECT_FIRST(
        "regions.error.pos1",
        "Select the first corner with /region pos1.",
        "Wähle die erste Ecke mit /region pos1."
    ),
    SELECT_SECOND(
        "regions.error.pos2",
        "Select the second corner with /region pos2.",
        "Wähle die zweite Ecke mit /region pos2."
    ),
    SELECT_CORNERS(
        "regions.error.corners",
        "Use /region pos1 and pos2 first.",
        "Verwende zuerst /region pos1 und pos2."
    ),
    PREVIEW_WORLD(
        "regions.error.previewWorld",
        "Visit the claim's world to preview its boundary.",
        "Besuche die Welt des Gebiets, um die Grenze anzuzeigen."
    ),
    NO_CLAIM_HERE("regions.error.location", "No claim at your location.", "Kein Gebiet an deiner Position."),
    PROFILE_FAILED(
        "regions.error.profile",
        "Player profile could not be resolved.",
        "Spielerprofil konnte nicht gefunden werden."
    ),
    CONSOLE_CLAIM(
        "regions.error.consoleClaim",
        "Console must specify a claim.",
        "Die Konsole muss ein Gebiet angeben."
    ),
    WORLD_NOT_FOUND("regions.error.world", "World not found.", "Welt nicht gefunden."),
    UNKNOWN_FLAG("regions.error.flag", "Unknown flag.", "Unbekanntes Flag."),
    INVALID_ROLE("regions.error.role", "Invalid role.", "Ungültige Rolle."),
    CONFIG_SAVE_FAILED(
        "regions.error.configSave",
        "Could not save region configuration.",
        "Gebietskonfiguration konnte nicht gespeichert werden."
    ),
    POLICY_SAVE_FAILED(
        "regions.error.policySave",
        "Could not apply server defaults to all claims.",
        "Serverstandard konnte nicht auf alle Gebiete angewendet werden."
    ),
    INVALID_LIMITS(
        "regions.error.limits",
        "Maximum limits must be non-negative or -1 (unlimited); minimum area must be positive and pricing settings non-negative.",
        "Maximalgrenzen müssen nicht negativ oder -1 (unbegrenzt) sein; Mindestfläche positiv und Preiseinstellungen nicht negativ."
    );

    override val translations: Map<FileTypes, String>
        get() {
            val style = when {
                key.startsWith("regions.error.") -> "<prefix> <red>"
                key.startsWith("action.regions.") && !key.endsWith(".lore") -> "<aqua>"
                key.endsWith(".lore") -> "<gray>"
                else -> ""
            }
            return mapOf(FileTypes.EN to style + english, FileTypes.DE to style + german)
        }
}
