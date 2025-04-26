package yv.tils.common.other

import language.LanguageHandler
import org.bukkit.command.CommandSender
import yv.tils.common.language.LangStrings

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