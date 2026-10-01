/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.regions.language

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.logger.Logger
import net.kyori.adventure.text.minimessage.MiniMessage

/** Typed message references. No English lookup, locale fallback or custom substitution. */
data class RegionMessage(val string: LangStrings, val parameters: Map<String, Any> = emptyMap()) {
    fun component(sender: CommandSender): Component = LanguageHandler.getMessage(
        string, sender,
        parameters.mapValues { (_, value) ->
            if (value is String) MiniMessage.miniMessage().escapeTags(value) else value
        })
}

fun LangStrings.message(vararg parameters: Pair<String, Any>) = RegionMessage(this, mapOf(*parameters))

class RegionFailure(val string: LangStrings, val parameters: Map<String, Any> = emptyMap(), cause: Throwable? = null) :
    IllegalStateException(string.key, cause)

object RegionText {
    fun send(sender: CommandSender, message: RegionMessage) {
        sender.sendMessage(message.component(sender))
    }

    fun action(sender: CommandSender, block: () -> Unit) {
        try {
            block()
        } catch (e: RegionFailure) {
            send(sender, RegionMessage(e.string, e.parameters))
        } catch (_: IllegalArgumentException) {
            send(sender, LangStrings.INVALID_INPUT.message())
        } catch (e: IllegalStateException) {
            val string = LangStrings.entries.firstOrNull { it.key == e.message }
            if (string != null) send(sender, string.message()) else {
                Logger.error("regions operation failed", e)
                send(sender, LangStrings.FAILED.message())
            }
        } catch (e: Exception) {
            Logger.error("regions operation failed", e)
            send(sender, LangStrings.FAILED.message())
        }
    }
}
