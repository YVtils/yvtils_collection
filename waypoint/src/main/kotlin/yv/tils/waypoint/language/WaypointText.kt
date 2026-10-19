package yv.tils.waypoint.language

import org.bukkit.entity.Player
import yv.tils.config.language.FileTypes
import yv.tils.config.language.LanguageHandler
import yv.tils.config.language.LanguageProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core

enum class LangStrings(val id: String, override val key: String, en: String, de: String) :
    LanguageProvider.LangStrings {
    SAVED("saved", "waypoint.command.saved", "Waypoint saved.", "Wegpunkt gespeichert."),
    DELETED("deleted", "waypoint.command.deleted", "Waypoint deleted.", "Wegpunkt gelöscht."),
    MISSING(
        "missing",
        "waypoint.error.missing",
        "Waypoint unavailable in this world.",
        "Wegpunkt in dieser Welt nicht verfügbar."
    ),
    DENIED(
        "denied",
        "waypoint.error.denied",
        "You cannot perform this action.",
        "Du darfst diese Aktion nicht ausführen."
    ),
    INVALID(
        "invalid",
        "waypoint.error.invalid",
        "Use a unique name with 1–64 letters, digits, '_' or '-'.",
        "Nutze einen eindeutigen Namen mit 1–64 Buchstaben, Zahlen, '_' oder '-'."
    ),
    STOPPED("stopped", "waypoint.navigation.stopped", "Navigation stopped.", "Navigation beendet."),
    ARRIVED("arrived", "waypoint.navigation.arrived", "Waypoint reached.", "Wegpunkt erreicht."),
    STARTED(
        "started",
        "waypoint.navigation.started",
        "Following <waypoint>. Repeat to stop.",
        "Navigation zu <waypoint>. Wiederholen zum Beenden."
    ),
    TITLE("title", "waypoint.gui.title", "Waypoints", "Wegpunkte"),
    ADMIN("admin", "waypoint.gui.admin", "All waypoints (admin)", "Alle Wegpunkte (Admin)"),
    CREATE("create", "waypoint.gui.create", "Create at your position", "An deiner Position erstellen"),
    NAME("name", "waypoint.gui.input.name", "Waypoint name", "Wegpunktname"),
    RENAME("rename", "waypoint.gui.rename", "Rename waypoint", "Wegpunkt umbenennen"),
    LOOKUP(
        "lookup",
        "waypoint.gui.lookup",
        "Navigate by name (including unlisted)",
        "Nach Name navigieren (auch ungelistete)"
    ),
    NAVIGATE("navigate", "waypoint.gui.navigate", "Navigate", "Navigieren"),
    DELETE("delete", "waypoint.gui.delete", "Delete waypoint", "Wegpunkt löschen"),
    CONFIRM("confirm", "waypoint.gui.confirm", "Confirm", "Bestätigen"),
    BACK("back", "waypoint.gui.back", "Back", "Zurück"),
    NEXT("next", "waypoint.gui.next", "Next page", "Nächste Seite"),
    PREVIOUS("previous", "waypoint.gui.previous", "Previous page", "Vorherige Seite"),
    VISIBILITY("visibility", "waypoint.gui.visibility", "Visibility: <value>", "Sichtbarkeit: <value>"),
    MOVE("move", "waypoint.gui.move", "Move to your position", "An deine Position verschieben"),
    CONFIG("config", "waypoint.config.title", "Waypoint settings", "Wegpunkt-Einstellungen"),
    PERSONAL("personal", "waypoint.gui.personal", "Player waypoints", "Spieler-Wegpunkte"),
    SHARING("sharing", "waypoint.gui.sharing.title", "Shared players", "Freigegebene Spieler"),
    SHARE("share", "waypoint.gui.sharing.add", "Share with name or UUID", "Mit Name oder UUID teilen"),
    UNSHARE("unshare", "waypoint.gui.sharing.remove", "Remove access: <player>", "Zugriff entfernen: <player>"),
    UNKNOWN(
        "unknownPlayer",
        "waypoint.error.player",
        "Player not found. Use a known name or UUID.",
        "Spieler nicht gefunden. Nutze Name oder UUID."
    ),
    LIST(
        "listTitle",
        "waypoint.command.list.title",
        "Available waypoints — click to manage:",
        "Verfügbare Wegpunkte — zum Verwalten anklicken:"
    ),
    LIST_EMPTY("listEmpty", "waypoint.command.list.empty", "No waypoints are available to you yet.", "Für dich sind noch keine Wegpunkte verfügbar."),
    LIST_ENTRY("listEntry", "waypoint.command.list.entry", "<gray> • <yellow><waypoint> <gray>— Owner: <white><owner>", "<gray> • <yellow><waypoint> <gray>— Besitzer: <white><owner>"),
    OPEN("openDetails", "waypoint.gui.open", "Open waypoint details", "Wegpunktdetails öffnen"),
    FAILURE(
        "failure",
        "waypoint.error.save",
        "Action failed; no change was saved.",
        "Aktion fehlgeschlagen; keine Änderung gespeichert."
    );

    override val translations = mapOf(FileTypes.EN to en, FileTypes.DE to de)
}

object WaypointText {
    fun register() = LanguageProvider.registerEnumStrings<LangStrings>()
    fun text(player: Player, id: String, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage(LangStrings.entries.first { it.id == id }.key, player, params)

    fun send(player: Player, id: String, params: Map<String, Any> = emptyMap()) {
        val prefix = MessageUtils.convert(Core.prefix + " ")
        val color = if (id in setOf(
                "denied",
                "failure",
                "invalid",
                "missing",
                "unknownPlayer"
            )
        ) NamedTextColor.RED
        else TextColor.fromHexString(Colors.SECONDARY.color)!!
        player.sendMessage(
            prefix.append(
                Component.empty().color(color).append(text(player, id, params))
            )
        )
    }
}
