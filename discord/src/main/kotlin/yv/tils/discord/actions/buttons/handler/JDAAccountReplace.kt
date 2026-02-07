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

package yv.tils.discord.actions.buttons.handler

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.data.Components
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.whitelist.*
import yv.tils.utils.logger.Logger
import java.util.concurrent.TimeUnit

class JDAAccountReplace {
    /**
     * Executes the confirm action for account replacement.
     *
     * @param e The ButtonInteractionEvent containing the button interaction event.
     */
    fun executeConfirm(e: ButtonInteractionEvent) {
        val message = e.message
        val user = e.user
        val userID = user.id
        val guildID = e.guild?.id

        message.delete().queue()

        e.deferReply(true).queue()
        val hook = e.hook

        var newAccount: String?

        var targetUserID: String?

        try {
            val cacheEntry = WhitelistManage.getCacheEntryAsMap(userID, true)

            if (cacheEntry.size == 1) {
                val entryKey = cacheEntry.keys.first()
                newAccount = cacheEntry[entryKey]

                targetUserID = entryKey.split("_")[1]
            } else {
                throw Exception("Multiple accounts found in cache for user: $userID")
            }

            WhitelistManage.removeFromCache(userID, true)
        } catch (_: Exception) {
            e.channel.sendMessageComponents(
                WhitelistComponents().accountErrorContainer(
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED.key,
                        params = mapOf<String, Any>(
                            "user" to user.effectiveName,
                        )
                    )
                )
            ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.MINUTES)

            Logger.warn(
                LanguageHandler.getMessage(
                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED.key,
                    params = mapOf<String, Any>(
                        "user" to user.effectiveName,
                    )
                )
            )

            return
        }

        if (newAccount == null) {
            e.channel.sendMessageComponents(
                WhitelistComponents().accountErrorContainer(
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED.key,
                        params = mapOf<String, Any>(
                            "user" to user.effectiveName,
                        )
                    )
                )
            ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.MINUTES)

            Logger.warn(
                LanguageHandler.getMessage(
                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED.key,
                    params = mapOf<String, Any>(
                        "user" to user.effectiveName,
                    )
                )
            )

            return
        }

        val oldEntry: WhitelistEntry

        try {
            oldEntry = WhitelistManage().unlinkAccount(targetUserID, guildID, user)
            WhitelistManage().linkAccount(newAccount, targetUserID, guildID, user)
        } catch (ex: Exception) {
            e.channel.sendMessageComponents(
                WhitelistComponents().accountErrorContainer(
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_EXCEPTION.key,
                        params = mapOf<String, Any>(
                            "user" to "<@$targetUserID>",
                            "error" to (ex.message ?: "Unknown error")
                        )
                    )
                )
            ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.MINUTES)
            Logger.warn(
                LanguageHandler.getMessage(
                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_EXCEPTION.key,
                    params = mapOf<String, Any>(
                        "user" to targetUserID,
                        "error" to (ex.message ?: "Unknown error")
                    )
                )
            )
            return
        }

        hook.sendMessageComponents(
            WhitelistComponents().accountChangeContainer(
                oldEntry.minecraftName,
                newAccount
            )
        ).useComponentsV2().setEphemeral(true).queue()

        Logger.info(
            LanguageHandler.getMessage(
                RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_REPLACED.key,
                params = mapOf<String, Any>(
                    "oldAccount" to oldEntry.minecraftName,
                    "newAccount" to newAccount,
                    "user" to user.effectiveName,
                    "discordAccount" to "<@$targetUserID>",
                )
            )
        )
    }

    /**
     * Executes the cancel action for account replacement.
     *
     * @param e The ButtonInteractionEvent containing the button interaction event.
     */
    fun executeCancel(e: ButtonInteractionEvent) {
        val message = e.message
        val userID = e.user.id

        message.delete().queue()
        e.deferReply(true).queue()

        val hook = e.hook

        try {
            WhitelistManage.removeFromCache(userID, true)
        } catch (ex: Exception) {
            e.channel.sendMessageComponents(
                WhitelistComponents().accountErrorContainer(
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED.key,
                        params = mapOf<String, Any>(
                            "user" to e.user.effectiveName
                        )
                    )
                )
            ).useComponentsV2().complete().delete().queueAfter(5, TimeUnit.MINUTES)

            Logger.warn(
                LanguageHandler.getMessage(
                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NOT_CACHED.key,
                    params = mapOf<String, Any>(
                        "user" to e.user.effectiveName
                    )
                )
            )
            return
        }

        hook.sendMessageComponents(
            Components().actionCancelledComponent(
                LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_ACTION_CANCELLED_ACTION_ACCOUNT_REPLACE.key)
            )
        ).useComponentsV2().setEphemeral(true).queue()
    }
}
