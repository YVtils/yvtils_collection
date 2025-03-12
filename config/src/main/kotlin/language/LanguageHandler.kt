package language

import com.mojang.brigadier.Message
import message.MessageUtils
import logger.Logger
import net.kyori.adventure.text.Component
import java.util.Locale
import java.util.UUID

class LanguageHandler {
    companion object {
        private val playerLang = mutableMapOf<UUID, Locale>()
        private var serverDefaultLang: Locale = Locale.ENGLISH

        fun getMessage(key: String, uuid: UUID? = null): Component =
            MessageUtils.convert(getString(key, getLocale(uuid)))

        fun getRawMessage(key: String, uuid: UUID? = null): String =
            getString(key, getLocale(uuid))

        fun getCleanMessage(key: String, uuid: UUID? = null): String =
            MessageUtils.strip(getString(key, getLocale(uuid)))

        fun getMessage(key: String, uuid: UUID? = null, params: Map<String, Any>): Component {
            val message = getString(key, getLocale(uuid))
            return MessageUtils.replacer(message, params)
        }

        private fun getLocale(uuid: UUID?): Locale =
            uuid?.let { playerLang.getOrDefault(it, serverDefaultLang) } ?: serverDefaultLang

        private fun getString(key: String, locale: Locale): String {
            Logger.debug("Getting string for key: $key, locale: $locale")
            val langString = Language.langStrings[key] ?: return key

            val lang = Language().localeToLanguage(locale)
            val shortLang = lang.split("_")

            return langString.value[lang] ?: langString.value[shortLang[1]] ?: langString.value[Language().localeToShortLanguage(serverDefaultLang)] ?: key
        }
    }

    fun setPlayerLanguage(uuid: UUID, locale: Locale) {
        Logger.debug("Setting player language for UUID: $uuid to locale: $locale")
        playerLang[uuid] = locale
    }

    fun removePlayerLanguage(uuid: UUID) {
        Logger.debug("Removing player language for UUID: $uuid")
        playerLang.remove(uuid)
    }

    fun setServerDefaultLanguage(locale: Locale) {
        Logger.debug("Setting server default language to locale: $locale")
        serverDefaultLang = locale
    }
}
