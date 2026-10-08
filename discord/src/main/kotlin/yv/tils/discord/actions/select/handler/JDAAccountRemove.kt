package yv.tils.discord.actions.select.handler

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import yv.tils.discord.actions.commands.handler.JDAWhitelist
import yv.tils.discord.actions.modals.JDARegistration
import yv.tils.discord.language.AccountText
import yv.tils.discord.logic.whitelist.*
import yv.tils.utils.coroutine.CoroutineHandler

class JDAAccountRemove {
    fun handleForceRemove(e: StringSelectInteractionEvent) {
        if (e.componentId != "whitelist:force:remove") return
        e.deferReply(true).queue({ hook ->
            CoroutineHandler.launchTask(task = {
                val result = try {
                    if (!JDARegistration.allowedGuild(e.guild?.id)) throw AccountService.Failure("guild")
                    if (!JDAWhitelist.authorized(e.member)) throw AccountService.Failure("permission")
                    val results = mutableListOf<String>()
                    for (id in e.values) {
                        results += try {
                            WhitelistManage().unlinkAccount(
                                id,
                                e.guild!!.id,
                                e.user
                            ); "$id: ${AccountText.raw("completed")}"
                        } catch (error: Exception) {
                            "$id: ${AccountText.error(error)}"
                        }
                    }
                    results.joinToString("\n").ifEmpty { AccountText.raw("missing") }
                } catch (error: Exception) {
                    AccountText.error(error)
                }
                hook.editOriginal(result).queue()
            }, isOnce = true)
        })
    }
}
