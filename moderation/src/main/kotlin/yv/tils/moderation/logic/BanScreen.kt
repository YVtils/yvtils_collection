package yv.tils.moderation.logic

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.BanEntry
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.gui.ModerationText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

object BanScreen {
    fun message(entry: BanEntry<*>, uuid: UUID): Component {
        val expiry = entry.expiration
        val reason = entry.reason ?: LanguageHandler.getRawMessage("moderation.placeholder.reason.none", uuid)
        val time = expiry?.let {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(it.time))
        }.orEmpty()
        return render(
            LanguageHandler.getRawMessage(
                if (expiry == null) "moderation.ban.screen.permanent"
                else "moderation.ban.screen.temporary", uuid
            ), reason, entry.source ?: "Server", time
        )
    }

    fun render(template: String, reason: String, issuer: String, expires: String): Component =
        MiniMessage.miniMessage().deserialize(
            template,
            Placeholder.component("reason", ModerationText.reason(reason)),
            Placeholder.unparsed("issuer", issuer), Placeholder.unparsed("expires", expires)
        )
}
