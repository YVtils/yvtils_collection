/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.configv2.language

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LanguageHandler {
    companion object {
        val playerLang = ConcurrentHashMap<UUID, Locale>()

        @Volatile
        private var serverDefaultLang: Locale = Locale.ENGLISH

        fun getMessage(key: LanguageProvider.LangStrings, uuid: UUID? = null): Component =
            getMessage(key.key, uuid)

        fun getMessage(key: LanguageProvider.LangStrings, sender: CommandSender): Component =
            getMessage(key.key, sender)

        fun getMessage(key: LanguageProvider.LangStrings, uuid: UUID? = null, params: Map<String, Any>): Component =
            getMessage(key.key, uuid, params)

        fun getMessage(key: LanguageProvider.LangStrings, sender: CommandSender, params: Map<String, Any>): Component =
            getMessage(key.key, sender, params)

        fun getMessage(key: String, uuid: UUID? = null): Component {
            val message = getString(key, getLocale(uuid))
            return MessageUtils.replacer(message, mapOf())
        }

        fun getMessage(key: String, sender: CommandSender): Component =
            if (sender is Player) {
                getMessage(key, sender.uniqueId)
            } else {
                getMessage(key)
            }

        fun getMessage(key: String, uuid: UUID? = null, params: Map<String, Any>): Component {
            val message = getString(key, getLocale(uuid))
            return MessageUtils.replacer(message, params)
        }

        fun getMessage(key: String, params: Map<String, Any>): Component {
            val message = getString(key, serverDefaultLang)
            return MessageUtils.replacer(message, params)
        }

        fun getMessage(key: String, sender: CommandSender, params: Map<String, Any>): Component {
            return if (sender is Player) {
                getMessage(key, sender.uniqueId, params)
            } else {
                getMessage(key, params = params)
            }
        }

        fun getRawMessage(key: LanguageProvider.LangStrings, uuid: UUID? = null): String =
            getRawMessage(key.key, uuid)

        fun getRawMessage(key: LanguageProvider.LangStrings, sender: CommandSender): String =
            getRawMessage(key.key, sender)

        fun getRawMessage(key: LanguageProvider.LangStrings, uuid: UUID? = null, params: Map<String, Any>): String =
            getRawMessage(key.key, uuid, params)

        fun getRawMessage(key: LanguageProvider.LangStrings, sender: CommandSender, params: Map<String, Any>): String =
            getRawMessage(key.key, sender, params)

        fun getRawMessage(key: String, uuid: UUID? = null): String =
            getString(key, getLocale(uuid))

        fun getRawMessage(key: String, sender: CommandSender): String =
            if (sender is Player) {
                getRawMessage(key, sender.uniqueId)
            } else {
                getRawMessage(key)
            }

        fun getRawMessage(key: String, uuid: UUID? = null, params: Map<String, Any>): String {
            val message = getString(key, getLocale(uuid))
            return MessageUtils.convert(MessageUtils.replacer(message, params))
        }

        fun getRawMessage(key: String, sender: CommandSender, params: Map<String, Any>): String {
            return if (sender is Player) {
                getRawMessage(key, sender.uniqueId, params)
            } else {
                getRawMessage(key, params = params)
            }
        }

        fun getCleanMessage(key: String, uuid: UUID? = null): String =
            MessageUtils.strip(getString(key, getLocale(uuid)))

        fun getCleanMessage(key: String, sender: CommandSender): String =
            if (sender is Player) {
                getCleanMessage(key, sender.uniqueId)
            } else {
                getCleanMessage(key)
            }

        fun getCleanMessage(key: String, uuid: UUID? = null, params: Map<String, Any>): String {
            val message = getRawMessage(key, uuid, params)
            return MessageUtils.strip(message)
        }

        fun getCleanMessage(key: String, sender: CommandSender, params: Map<String, Any>): String {
            return if (sender is Player) {
                getCleanMessage(key, sender.uniqueId, params)
            } else {
                getCleanMessage(key, params = params)
            }
        }

        private fun getLocale(uuid: UUID?): Locale =
            uuid?.let { playerLang.getOrDefault(it, serverDefaultLang) } ?: serverDefaultLang

        private fun getString(key: String, locale: Locale): String {
            Logger.debug("Getting string for key: $key, locale: $locale")
            val langString = Language.langStrings[key] ?: return key

            val lang = Language.localeToLanguage(locale)
            // `lang` is always "<country>_<language>" (see Language.localeToLanguage),
            // but derive the short form defensively instead of `lang.split("_")[1]` -
            // the original module's version threw IndexOutOfBoundsException on any
            // value that didn't contain an underscore.
            val shortLang = lang.substringAfter('_', missingDelimiterValue = lang)

            return langString.value[lang]
                ?: langString.value[shortLang]
                ?: langString.value[Language.localeToShortLanguage(serverDefaultLang)]
                ?: key
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

    fun setServerDefaultLanguage(locale: String) {
        setServerDefaultLanguage(Language.stringToLocale(locale))
    }

    fun setServerDefaultLanguage(locale: Locale) {
        Logger.debug("Setting server default language to locale: $locale")
        serverDefaultLang = locale
    }
}
