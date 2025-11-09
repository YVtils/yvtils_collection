/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

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
