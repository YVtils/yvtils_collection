package yv.tils.common.other

import org.bukkit.command.CommandSender
import yv.tils.common.language.LangStrings
import yv.tils.config.language.LanguageHandler

class AsyncActionAnnounce {
    companion object {
        fun announceAction(sender: CommandSender) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_EXECUTOR_ASYNC_ACTION.key,
                    sender,
                )
            )
        }
    }
}
