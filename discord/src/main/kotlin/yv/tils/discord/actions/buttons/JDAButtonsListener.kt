package yv.tils.discord.actions.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.actions.buttons.handler.JDAAccountReplace

class JDAButtonsListener : ListenerAdapter() {
    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        val buttonId = e.componentId

        when (buttonId) {
            "whitelist:change:confirm" -> {
                JDAAccountReplace().executeConfirm(e)
            }
            "whitelist:change:cancel" -> {
                JDAAccountReplace().executeCancel(e)
            }
            else -> {
                e.reply("Unknown button interaction").setEphemeral(true).queue()
            }
        }
    }
}