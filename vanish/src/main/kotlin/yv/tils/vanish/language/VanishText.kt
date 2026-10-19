package yv.tils.vanish.language

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
    ENABLED(
        "enabled",
        "vanish.command.enabled",
        "Vanish enabled (layer <layer>).",
        "Vanish aktiviert (Ebene <layer>)."
    ),
    DISABLED("disabled", "vanish.command.disabled", "Vanish disabled.", "Vanish deaktiviert."),
    CONTAINER(
        "container",
        "vanish.gui.container.title",
        "Silent container (read-only)",
        "Lautloser Container (schreibgeschützt)"
    ),
    CONFIG("config", "vanish.config.title", "Vanish settings", "Vanish-Einstellungen"),
    DENIED(
        "denied",
        "vanish.error.denied",
        "You cannot use this capability or layer.",
        "Du darfst diese Funktion oder Ebene nicht nutzen."
    ),
    MENU_TITLE("menuTitle", "vanish.gui.title", "Vanish", "Vanish"),
    SETTINGS("settings", "vanish.gui.settings.title", "Personal Vanish settings", "Persönliche Vanish-Einstellungen"),
    ANNOUNCE(
        "announce",
        "vanish.gui.settings.announce",
        "Fake join/leave announcements",
        "Falsche Beitritts-/Verlassensmeldungen"
    ),
    LAYER("layer", "vanish.gui.settings.layer", "Vanish layer", "Vanish-Ebene"),
    TOGGLE("toggle", "vanish.gui.settings.toggle", "Toggle Vanish", "Vanish umschalten"),
    VALUE("value", "vanish.gui.settings.value", "Current: <value>", "Aktuell: <value>"),
    SILENT("silentContainers", "vanish.gui.settings.silentContainers", "Silent containers", "Lautlose Container"),
    PICKUP("pickup", "vanish.gui.settings.pickup", "Prevent item pickup", "Gegenstände nicht aufsammeln"),
    DROP("drop", "vanish.gui.settings.drop", "Prevent item dropping", "Gegenstände nicht fallen lassen"),
    MOBS("mobs", "vanish.gui.settings.mobs", "Prevent mob targeting", "Mob-Ziele verhindern"),
    DAMAGE("damage", "vanish.gui.settings.damage", "Prevent combat", "Kampf verhindern"),
    PHYSICAL(
        "physical",
        "vanish.gui.settings.physical",
        "Prevent physical interactions",
        "Physische Interaktionen verhindern"
    ),
    COLLISION("collision", "vanish.gui.settings.collision", "Disable collision", "Kollision deaktivieren"),
    FAILURE(
        "failure",
        "vanish.error.save",
        "Your preferences could not be saved.",
        "Deine Einstellungen konnten nicht gespeichert werden."
    ),
    BACK("back", "vanish.gui.back", "Close", "Schließen");

    override val translations = mapOf(FileTypes.EN to en, FileTypes.DE to de)
}

object VanishText {
    fun register() = LanguageProvider.registerEnumStrings<LangStrings>()
    fun text(player: Player, id: String, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage(LangStrings.entries.first { it.id == id }.key, player, params)

    fun send(player: Player, id: String, params: Map<String, Any> = emptyMap()) {
        val prefix = MessageUtils.convert(Core.prefix + " ")
        val color = if (id in setOf("denied", "failure")) NamedTextColor.RED
        else TextColor.fromHexString(Colors.SECONDARY.color)!!
        player.sendMessage(
            prefix.append(
                Component.empty().color(color).append(text(player, id, params))
            )
        )
    }
}
