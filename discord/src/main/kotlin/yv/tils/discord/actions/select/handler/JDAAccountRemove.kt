package yv.tils.discord.actions.select.handler

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import yv.tils.discord.logic.whitelist.*
import yv.tils.utils.logger.Logger

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

        hook.editOriginalComponents(
            WhitelistComponents().forceRemoveContainer(
                1,
                removedEntries,
            )
        ).useComponentsV2().queue()
    }
}
