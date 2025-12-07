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

package yv.tils.moderation.logic

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.ban.BanListType
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.Logger
import java.util.Date

class UnbanLogic {
    /**
     * Trigger an unban
     * @param targets The target(s) to unban
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the unban
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerUnban(
        targets: List<PlayerProfile>,
        reason: String,
        sender: CommandSender,
        silent: Boolean = false
    ) {
        val params = mapOf(
            "reason" to reason,
            "sender" to sender,
            "silent" to silent
        )

        TargetUtils.loopThroughTargets(targets, this::unbanLogic, params)
    }

    private fun unbanLogic(
        target: PlayerProfile,
        params: Map<String, Any>,
    ) {
        val reason = params["reason"] as String
        val sender = params["sender"] as CommandSender
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                // TODO: Add some type of error message
                Logger.dev("Failed to parse offline player ${target.name}")
                return
            }

            if (!TargetUtils.isTargetBanned(offlinePlayer)) {
                // TODO: target is not banned
                //  Return and throw error to player
                Logger.dev("Target ${target.name} is not banned")
                return
            }

            try {
                val date = Date()
                date.time = 0L

                Bukkit.getBanList(BanListType.PROFILE).addBan(target, reason, date, sender.name)
            } catch (e: IllegalArgumentException) {
                // TODO: Add error handling
                Logger.dev("Failed to unban player ${target.name}: ${e.message}")
                return
            }

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                ModerationAction.UNBAN
            )
        } catch (e: Exception) {
            // TODO: Add error handling
            Logger.dev("Failed to unban player ${target.name}: ${e.message}")
        }
    }
}