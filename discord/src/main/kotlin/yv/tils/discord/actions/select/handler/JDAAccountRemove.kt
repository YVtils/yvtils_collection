package yv.tils.discord.actions.select.handler

import logger.Logger
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import yv.tils.discord.logic.whitelist.WhitelistManage

class JDAAccountRemove {
    fun handleForceRemove(e: StringSelectInteractionEvent) {
        val guild = e.guild
        val values = e.values
        if (values.isEmpty()) {
            return
        }

        for (value in values) {
            Logger.dev("Removing account: $value from whitelist")
            try {
                WhitelistManage().unlinkAccount(value, guild?.id)
            } catch (ex: Exception) {
                Logger.error("Failed to remove account: $value from whitelist - ${ex.message}")
                e.reply("Failed to remove account: $value").setEphemeral(true).queue()
                return
            }
        }

        e.reply("You selected: $values").setEphemeral(true).queue()
    }
}