package yv.tils.vanish.language

import org.bukkit.entity.Player
import yv.tils.config.language.FileTypes
import yv.tils.config.language.LanguageHandler
import yv.tils.config.language.LanguageProvider

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
    BACK("back", "vanish.gui.back", "Close", "Schließen");

    override val translations = mapOf(FileTypes.EN to en, FileTypes.DE to de)
}

object VanishText {
    fun register() = LanguageProvider.registerEnumStrings<LangStrings>()
    fun text(player: Player, id: String, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage(LangStrings.entries.first { it.id == id }.key, player, params)

    fun send(player: Player, id: String, params: Map<String, Any> = emptyMap()) =
        player.sendMessage(text(player, id, params))
}
