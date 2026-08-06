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

package yv.tils.essentials.utils

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.modules.Core

class CheckArguments {
    companion object {
        /**
         * Checks if the target argument is valid for the command sender.
         * @param sender The command sender.
         * @param target The target argument to check.
         * @return True if the target argument is valid, false otherwise.
         */
        fun checkForTargetArg(sender: CommandSender, target: Any?): Boolean {
            if (sender !is Player && target == null) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        "command.missing.player",
                        params = mapOf("prefix" to Core.prefix)
                    )
                )
                return false
            }

            return true
        }
    }
}