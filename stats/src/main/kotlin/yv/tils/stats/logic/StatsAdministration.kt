/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.logic

import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageHandler
import yv.tils.stats.StatsYVtils
import yv.tils.stats.language.RegisterStrings.LangStrings
import yv.tils.utils.logger.Logger

internal object StatsAdministration {
    fun canConfigure(sender: CommandSender): Boolean = sender.isOp ||
        sender.hasPermission("yvtils.stats.manage") || sender.hasPermission("yvtils.stats.optin")

    fun requirePermission(sender: CommandSender): Boolean {
        if (canConfigure(sender)) return true
        sender.sendMessage(LanguageHandler.getMessage(LangStrings.OPTIN_NO_PERMISSION.key, sender))
        return false
    }

    fun setConsent(sender: CommandSender, decision: Boolean): Boolean {
        if (!requirePermission(sender)) return false
        return try {
            StatsYVtils.markOptIn(decision)
            sender.sendMessage(LanguageHandler.getMessage(
                if (decision) LangStrings.OPTIN_ACCEPTED.key else LangStrings.OPTIN_DECLINED.key, sender,
            ))
            true
        } catch (e: Exception) {
            Logger.error("[Stats] Could not persist consent", e)
            sender.sendMessage(LanguageHandler.getMessage(LangStrings.CONFIG_SAVE_FAILED.key, sender))
            false
        }
    }
}
