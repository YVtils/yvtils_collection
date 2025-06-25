package yv.tils.discord.actions.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.actions.buttons.handler.JDAAccountReplace
import yv.tils.discord.actions.buttons.handler.JDAPageSwitch

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
            "whitelist:force:remove:site:previous" -> {
                JDAPageSwitch().executeSitePrevious(e)
            }

            "whitelist:force:remove:site:next" -> {
                JDAPageSwitch().executeSiteNext(e)
            }
            else -> {
                e.reply("Unknown button interaction").setEphemeral(true).queue()
            }
        }
    }
}
