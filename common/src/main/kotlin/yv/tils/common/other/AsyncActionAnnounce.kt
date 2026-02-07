/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.common.other

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitTask
import yv.tils.common.language.LangStrings
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data

class AsyncActionAnnounce {
    companion object {
        /**
         * Announces an async action to the sender after a 3-second delay.
         * Only sends the message if the operation is still running after 3 seconds.
         *
         * @param sender The command sender to notify
         * @return A BukkitTask that can be cancelled if the operation completes quickly
         */
        fun announceAction(sender: CommandSender): BukkitTask {
            return Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.COMMAND_EXECUTOR_ASYNC_ACTION.key,
                        sender,
                    )
                )
            }, 20L)
        }

        /**
         * Announces an async suggestion loading to the sender after a 3-second delay.
         * Only sends the message if the operation is still running after 3 seconds.
         *
         * @param sender The command sender to notify
         * @return A BukkitTask that can be cancelled if the operation completes quickly
         */
        fun announceSuggestion(sender: CommandSender): BukkitTask {
            return Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.COMMAND_SUGGESTION_ASYNC_ACTION.key,
                        sender,
                    )
                )
            }, 20L)
        }

        /**
         * Announces an async error to the sender immediately.
         *
         * @param sender The command sender to notify
         */
        fun announcePlayerError(sender: CommandSender) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_EXECUTOR_ASYNC_ERROR_PLAYER.key,
                    sender,
                )
            )
        }
    }
}
