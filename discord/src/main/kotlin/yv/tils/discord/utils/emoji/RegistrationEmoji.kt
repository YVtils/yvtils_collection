package yv.tils.discord.utils.emoji

import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.Emoji
import yv.tils.discord.logic.AppLogic
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger

/** Bundled application artwork uses a separate namespace from expiring yv_ player heads. */
object RegistrationEmoji {
    private const val NAME = "yvtils_registration_furnace"
    private const val RESOURCE = "/discord/emojis/registration-furnace.gif"

    @Volatile
    private var emoji: Emoji = Emoji.fromUnicode("⛏️")

    fun get(): Emoji = emoji

    fun initialize() {
        emoji = Emoji.fromUnicode("⛏️")
        CoroutineHandler.launchTask(task = {
            try {
                val existing = AppLogic.getJDA().retrieveApplicationEmojis().complete()
                    .firstOrNull { it.name == NAME }
                val applicationEmoji = existing ?: RegistrationEmoji::class.java.getResourceAsStream(RESOURCE).use { stream ->
                    checkNotNull(stream) { "Missing registration emoji resource: $RESOURCE" }
                    AppLogic.getJDA().createApplicationEmoji(NAME, Icon.from(stream.readBytes(), Icon.IconType.GIF)).complete()
                }
                emoji = Emoji.fromCustom(NAME, applicationEmoji.idLong, applicationEmoji.isAnimated)
                Logger.debug("Registration application emoji ready: ${applicationEmoji.idLong}; animated=${applicationEmoji.isAnimated}")
            } catch (error: Exception) {
                Logger.warn("Registration emoji unavailable; using Unicode fallback: ${error.message}")
            }
        }, isOnce = true)
    }
}
