/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.language

import org.bukkit.entity.Player
import yv.tils.configv2.language.BuildLanguage
import yv.tils.configv2.language.FileTypes
import yv.tils.configv2.language.LanguageHandler

object FusionText {
    private val strings = mapOf(
        "flaskNourished" to ("<prefix> <green>Restored <food> hunger and <saturation> saturation for <cost> XP points." to
            "<prefix> <green><food> Hunger und <saturation> Sättigung für <cost> XP-Punkte aufgefüllt."),
        "pickItem" to ("Select an inventory item" to "Inventargegenstand wählen"),
        "pickHint" to ("Click to copy this item into the recipe. Your item is not consumed." to "Klicken, um diesen Gegenstand ins Rezept zu kopieren. Er wird nicht verbraucht."),
        "inventoryEmpty" to ("Put the desired item in your inventory first." to "Lege den gewünschten Gegenstand zuerst in dein Inventar."),
        "matchMaterial" to ("Match material only" to "Nur Material vergleichen"),
        "matchExact" to ("Match exact item metadata" to "Exakte Gegenstandsmetadaten vergleichen"),
        "addSetting" to ("Add one behavior setting" to "Eine Verhaltenseinstellung hinzufügen"),
        "removeSetting" to ("Select a setting to remove" to "Zu entfernende Einstellung wählen"),
        "settingKey" to ("Enter setting key" to "Einstellungsschlüssel eingeben"),
        "settingInput" to ("Enter setting value" to "Einstellungswert eingeben"),
        "numberInputHint" to ("Middle-click: enter an exact value" to "Mittelklick: genauen Wert eingeben"),
        "settingValue" to ("Setting: <setting>" to "Einstellung: <setting>"),
        "settingsText" to ("Add/remove settings as text" to "Einstellungen als Text hinzufügen/entfernen"),
        "craftReady" to ("✓ Ready to craft" to "✓ Bereit zur Herstellung"),
        "craftBlocked" to ("✗ Requirements not met" to "✗ Voraussetzungen nicht erfüllt"),
        "craftAmount" to ("Items to receive: <amount>" to "Erhaltene Gegenstände: <amount>"),
        "ingredientSummary" to ("<mark> <ingredient>: <have> / <need>" to "<mark> <ingredient>: <have> / <need>"),
        "haveNeed" to ("Available for this recipe: <have> / <need>" to "Für dieses Rezept vorhanden: <have> / <need>"),
        "missingCount" to ("Missing: <amount>" to "Fehlend: <amount>"),
        "owned" to ("✓ Ingredient complete" to "✓ Zutat vollständig"),
        "missingIngredients" to ("✗ Ingredients missing" to "✗ Zutaten fehlen"),
        "allocationHint" to ("Counts are allocated once across all ingredients." to "Gegenstände werden nur einmal auf alle Zutaten verteilt."),
        "xpStatus" to ("XP levels: <have> / <need> required" to "XP-Level: <have> / <need> benötigt"),
        "requirements" to ("Crafting requirements" to "Herstellungsvoraussetzungen"),
        "selectionReady" to ("✓ Output options selected" to "✓ Ausgabeoptionen gewählt"),
        "selectionMissing" to ("✗ Select output options first" to "✗ Zuerst Ausgabeoptionen wählen"),
        "roomReady" to ("✓ Inventory space available" to "✓ Inventarplatz vorhanden"),
        "roomMissing" to ("✗ Check inventory space / output selection" to "✗ Inventarplatz / Ausgabeauswahl prüfen"),
        "outputQuantity" to ("Output per craft: <amount>" to "Ausgabe pro Vorgang: <amount>"),
        "recipeHint" to ("Click to view ingredients and craft." to "Klicken für Zutaten und Herstellung."),
        "editHint" to ("Click to open the recipe editor." to "Klicken, um das Rezept zu bearbeiten."),
        "currentValue" to ("Current: <value>" to "Aktuell: <value>"),
        "fieldHint" to ("Click to edit this setting." to "Klicken, um diese Einstellung zu bearbeiten."),
        "draftHint" to ("Draft only — use Save to apply changes." to "Nur Entwurf — mit Speichern übernehmen."),
        "toggleHint" to ("Click to toggle in this draft." to "Klicken, um den Entwurfswert umzuschalten."),
        "enabled" to ("Enabled" to "Aktiviert"),
        "editName" to ("Recipe name" to "Rezeptname"),
        "editDescription" to ("Description" to "Beschreibung"),
        "editCategory" to ("Category" to "Kategorie"),
        "editThumbnail" to ("Browser icon" to "Symbol in der Übersicht"),
        "editEnabled" to ("Recipe availability" to "Rezeptverfügbarkeit"),
        "editPermission" to ("Required permission" to "Benötigte Berechtigung"),
        "editXp" to ("Crafting XP price" to "XP-Preis der Herstellung"),
        "editAmount" to ("Output quantity" to "Ausgabemenge"),
        "editOutput" to ("Output material" to "Ausgabematerial"),
        "editKind" to ("Output behavior" to "Ausgabeverhalten"),
        "editSettings" to ("Behavior settings (key=value)" to "Verhaltenseinstellungen (key=value)"),
        "editIngredients" to ("Recipe ingredients" to "Rezeptzutaten"),
        "flaskFull" to ("<prefix> <gray>You are already full." to "<prefix> <gray>Du bist bereits satt."),
        "flaskXp" to ("<prefix> <red>You need at least <cost> XP points to use the flask." to "<prefix> <red>Du brauchst mindestens <cost> XP-Punkte für die Flasche."),
        "flaskFed" to ("<prefix> <green>Restored <food> hunger points for <cost> XP points." to "<prefix> <green><food> Hungerpunkte für <cost> XP-Punkte aufgefüllt."),
        "browser" to ("Fusion crafting" to "Fusion-Handwerk"),
        "search" to ("Search recipes" to "Rezepte suchen"),
        "category" to ("Category: <value>" to "Kategorie: <value>"),
        "all" to ("All" to "Alle"),
        "empty" to ("No recipes found" to "Keine Rezepte gefunden"),
        "disabled" to ("Recipe disabled" to "Rezept deaktiviert"),
        "back" to ("Back" to "Zurück"),
        "next" to ("Next page" to "Nächste Seite"),
        "previous" to ("Previous page" to "Vorherige Seite"),
        "refresh" to ("Refresh" to "Aktualisieren"),
        "one" to ("Craft one" to "Einmal herstellen"),
        "stack" to ("Craft a stack" to "Stapel herstellen"),
        "maximum" to ("Craft maximum (up to 64 crafts)" to "Maximum herstellen (bis zu 64 Vorgänge)"),
        "available" to ("Ingredients available" to "Zutaten vorhanden"),
        "need" to ("Missing <amount> × <ingredient>" to "Es fehlen <amount> × <ingredient>"),
        "ingredient" to ("<amount> × <ingredient>" to "<amount> × <ingredient>"),
        "cost" to ("XP level cost per craft: <amount>" to "XP-Level pro Vorgang: <amount>"),
        "locked" to ("Requires permission: <permission>" to "Benötigt Berechtigung: <permission>"),
        "light" to ("Light level: <value> (click to cycle)" to "Lichtstärke: <value> (zum Wechseln klicken)"),
        "head" to ("Select player / preview skin" to "Spieler wählen / Skin-Vorschau"),
        "resolving" to ("<prefix> <gray>Loading player skin…" to "<prefix> <gray>Spieler-Skin wird geladen…"),
        "badHead" to ("<prefix> <red>Could not resolve that player's skin. Try a Java username or UUID." to
                "<prefix> <red>Skin nicht gefunden. Nutze einen Java-Namen oder eine UUID."),
        "done" to ("<prefix> <green>Crafted <amount> × <recipe>." to "<prefix> <green><amount> × <recipe> hergestellt."),
        "denied" to ("<prefix> <red>No permission." to "<prefix> <red>Keine Berechtigung."),
        "missing" to ("<prefix> <red>Not enough ingredients." to "<prefix> <red>Nicht genug Zutaten."),
        "full" to ("<prefix> <red>Not enough inventory space." to "<prefix> <red>Nicht genug Inventarplatz."),
        "xp" to ("<prefix> <red>Not enough XP levels." to "<prefix> <red>Nicht genug XP-Level."),
        "stale" to ("<prefix> <red>Recipe changed; reopen it." to "<prefix> <red>Rezept geändert; erneut öffnen."),
        "error" to ("<prefix> <red>Operation failed: <error>" to "<prefix> <red>Vorgang fehlgeschlagen: <error>"),
        "manage" to ("Manage recipes" to "Rezepte verwalten"),
        "new" to ("Create recipe" to "Rezept erstellen"),
        "editor" to ("Recipe draft" to "Rezeptentwurf"),
        "save" to ("Validate and save draft" to "Entwurf prüfen und speichern"),
        "cancel" to ("Discard / back" to "Verwerfen / zurück"),
        "confirm" to ("Confirm" to "Bestätigen"),
        "saved" to ("<prefix> <green>Recipe saved." to "<prefix> <green>Rezept gespeichert."),
        "reloaded" to ("<prefix> <green>Fusion recipes reloaded." to "<prefix> <green>Fusion-Rezepte neu geladen."),
        "field" to ("Edit <field>: <value>" to "<field> bearbeiten: <value>"),
        "capture" to ("Capture held item as output (metadata included)" to "Gehaltenen Gegenstand als Ausgabe übernehmen (mit Metadaten)"),
        "add" to ("Add held item as exact ingredient" to "Gehaltenen Gegenstand als exakte Zutat hinzufügen"),
        "remove" to ("Remove last ingredient" to "Letzte Zutat entfernen"),
        "ingredients" to ("Ingredients: MATERIAL*count; #minecraft:logs*count; @fusion-id*count" to
                "Zutaten: MATERIAL*Anzahl; #minecraft:logs*Anzahl; @fusion-id*Anzahl"),
        "preview" to ("Output preview" to "Ausgabevorschau"),
        "help" to ("<prefix> /fusion [manage|new <id>|reload]" to "<prefix> /fusion [manage|new <id>|reload]")
    )

    fun register() = strings.forEach { (key, pair) ->
        BuildLanguage.registerString(BuildLanguage.RegisteredString(FileTypes.EN, "fusion.$key", pair.first))
        BuildLanguage.registerString(BuildLanguage.RegisteredString(FileTypes.DE, "fusion.$key", pair.second))
    }

    fun text(player: Player, key: String, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage("fusion.$key", player, params)

    fun send(player: Player, key: String, params: Map<String, Any> = emptyMap()) =
        player.sendMessage(text(player, key, params))
}
