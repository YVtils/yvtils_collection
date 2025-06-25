package yv.tils.discord.actions.select

import logger.Logger
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.actions.select.handler.JDAAccountRemove

class JDASelectListener : ListenerAdapter() {
    override fun onStringSelectInteraction(e: StringSelectInteractionEvent) {
        JDAAccountRemove().handleForceRemove(e)
    }
}