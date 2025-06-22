package yv.tils.discord.actions.buttons.handler

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import yv.tils.discord.logic.whitelist.WhitelistManage

class JDAAccountReplace {
    /**
     * Executes the confirm action for account replacement.
     *
     * @param e The ButtonInteractionEvent containing the button interaction event.
     */
    fun executeConfirm(e: ButtonInteractionEvent) {
        val message = e.message
        val userID = e.user.id
        val guildID = e.guild?.id

        message.delete().queue()

        val newAccount = WhitelistManage.accountReplaceCache[userID]
        WhitelistManage.accountReplaceCache.remove(userID)
        if (newAccount == null) {
            e.reply("No account replacement found for your user.").setEphemeral(true).queue() // TODO: error handling...
            return
        }

        try {
            WhitelistManage().unlinkAccount(userID, guildID)
            WhitelistManage().linkAccount(newAccount, userID, guildID)
        } catch (ex: Exception) {
            e.reply("An error occurred while replacing the account: ${ex.message}").setEphemeral(true).queue() // TODO: error handling...
            return
        }

        e.reply("Account replacement confirmed.").setEphemeral(true).queue() // TODO: success message
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

        WhitelistManage.accountReplaceCache.remove(userID)

        e.reply("Account replacement cancelled.").setEphemeral(true).queue() // TODO: success message
    }
}