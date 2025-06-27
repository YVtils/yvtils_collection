package yv.tils.discord.actions.select.handler

import logger.Logger
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import yv.tils.discord.logic.whitelist.*

class JDAAccountRemove {
    fun handleForceRemove(e: StringSelectInteractionEvent) {
        val guild = e.guild
        val user = e.user
        val values = e.values
        if (values.isEmpty()) {
            return
        }

        e.deferEdit().queue()
        val hook = e.hook

        val removedEntries = mutableListOf<WhitelistEntry>()

        for (value in values) {
            try {
                val entry = WhitelistManage().unlinkAccount(value, guild?.id, user)
                removedEntries.add(entry)
            } catch (ex: Exception) {
                Logger.error("Failed to remove account: $value from whitelist - ${ex.message}")
                continue
            }
        }

        val entries = WhitelistLogic.getEntriesBySite(1)

        hook.editOriginalEmbeds(
            WhitelistEmbeds().forceRemoveEmbed(1, removedEntries).build()
        ).setComponents(
            ActionRow.of(WhitelistEmbeds().forceRemoveActionRowDropdown(entries).build()),
            ActionRow.of(WhitelistEmbeds().forceRemoveActionRowButtons(1))
        ).queue()
    }
}
