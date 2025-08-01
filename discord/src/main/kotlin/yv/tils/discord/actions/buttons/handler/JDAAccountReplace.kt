package yv.tils.discord.actions.buttons.handler

import language.LanguageHandler
import logger.Logger
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import yv.tils.discord.data.Components
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.whitelist.*

class JDAAccountReplace {
    /**
     * Executes the confirm action for account replacement.
     *
     * @param e The ButtonInteractionEvent containing the button interaction event.
     */
    // TODO FIX: There probably is a bug when replacing the account over forceAdd for another user
    fun executeConfirm(e: ButtonInteractionEvent) {
        val message = e.message
        val user = e.user
        val userID = user.id
        val guildID = e.guild?.id

        message.delete().queue()

        e.deferReply().queue()
        val hook = e.hook

        val newAccount = WhitelistManage.accountReplaceCache[userID]
        WhitelistManage.accountReplaceCache.remove(userID)
        if (newAccount == null) {
            hook.sendMessageEmbeds(
                WhitelistEmbeds().accountErrorEmbed(
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NO_CACHE.key,
                        params = mapOf<String, Any>(
                            "user" to user.effectiveName,
                        )
                    )
                ).build()
            ).queue()

            Logger.warn(
                LanguageHandler.getMessage(
                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_NO_CACHE.key,
                    params = mapOf<String, Any>(
                        "user" to user.effectiveName,
                    )
                )
            )

            return
        }

        val oldEntry: WhitelistEntry

        try {
            oldEntry = WhitelistManage().unlinkAccount(userID, guildID, user)
            WhitelistManage().linkAccount(newAccount, userID, guildID, user)
        } catch (ex: Exception) {
            hook.sendMessageEmbeds(
                WhitelistEmbeds().accountErrorEmbed(
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_EXCEPTION.key,
                        params = mapOf<String, Any>(
                            "user" to user.effectiveName,
                            "error" to (ex.message ?: "Unknown error")
                        )
                    )
                ).build()
            ).queue()
            Logger.warn(
                LanguageHandler.getMessage(
                    RegisterStrings.LangStrings.ERROR_WHITELIST_ACCOUNT_REPLACE_EXCEPTION.key,
                    params = mapOf<String, Any>(
                        "user" to user.effectiveName,
                        "error" to (ex.message ?: "Unknown error")
                    )
                )
            )
            return
        }

        hook.sendMessageEmbeds(
            WhitelistEmbeds().accountChangeEmbed(
                oldEntry.minecraftName,
                newAccount
            ).build()
        ).setEphemeral(true).queue()

        Logger.info(
            LanguageHandler.getMessage(
                RegisterStrings.LangStrings.CONSOLE_WHITELIST_ACCOUNT_REPLACED.key,
                params = mapOf<String, Any>(
                    "oldAccount" to oldEntry.minecraftName,
                    "newAccount" to newAccount,
                    "user" to user.effectiveName,
                    "discordAccount" to user.effectiveName,
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

        WhitelistManage.accountReplaceCache.remove(userID)

        hook.sendMessageComponents(
            Components().actionCancelledComponent(
                LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_ACTION_CANCELLED_ACTION_ACCOUNT_REPLACE.key)
            )
        ).useComponentsV2().setEphemeral(true).queue()
    }
}
