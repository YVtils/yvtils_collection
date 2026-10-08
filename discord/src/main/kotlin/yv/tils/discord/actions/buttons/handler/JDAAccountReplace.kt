package yv.tils.discord.actions.buttons.handler

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import yv.tils.discord.actions.modals.JDARegistration
import yv.tils.discord.actions.commands.handler.JDAWhitelist
import yv.tils.discord.language.AccountText
import yv.tils.discord.logic.whitelist.*
import yv.tils.utils.coroutine.CoroutineHandler

class JDAAccountReplace {
    fun executeConfirm(e: ButtonInteractionEvent) = handle(e, true)
    fun executeCancel(e: ButtonInteractionEvent) = handle(e, false)
    private fun handle(e: ButtonInteractionEvent, confirm: Boolean) {
        e.deferReply(true).queue({ hook ->
            CoroutineHandler.launchTask(task = {
                val result = try {
                    if (!JDARegistration.allowedGuild(e.guild?.id)) throw AccountService.Failure("guild")
                    val cached = WhitelistManage.getCacheEntryAsMap(e.user.id, true)
                    if (cached.size != 1) throw AccountService.Failure("stale")
                    val key = cached.keys.first()
                    val target = if ('_' in key) key.substringAfter('_') else e.user.id
                    if (target != e.user.id && !JDAWhitelist.authorized(e.member)) throw AccountService.Failure("permission")
                    val name = WhitelistManage.accountReplaceCache.remove(key) ?: throw AccountService.Failure("stale")
                    val expected = WhitelistManage.replacementSnapshots.remove(key)
                    if (confirm) {
                        val old = expected ?: throw AccountService.Failure("stale")
                        AccountService.register(name, target, e.guild!!.id, "Discord:${e.user.id}", old)
                        AccountText.raw("completed")
                    } else AccountText.raw("cancel")
                } catch (error: Exception) {
                    AccountText.error(error)
                }
                hook.editOriginal(result).queue()
            }, isOnce = true)
        })
    }
}
