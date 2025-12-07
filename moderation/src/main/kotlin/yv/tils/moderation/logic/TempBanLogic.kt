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
import yv.tils.utils.time.TimeUtils
import java.util.Date

class TempBanLogic {
    /**
     * Trigger a ban with custom duration
     * @param targets The target(s) to ban
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the ban
     * @param duration The duration in ticks to ban the target(s)
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerTempBan(
        targets: List<PlayerProfile>,
        reason: String,
        duration: Int,
        unit: String,
        sender: CommandSender,
        silent: Boolean = false
    ) {
        val params = mapOf(
            "reason" to reason,
            "sender" to sender,
            "duration" to duration,
            "unit" to unit,
            "silent" to silent
        )

        TargetUtils.loopThroughTargets(targets, this::tempBanLogic, params)
    }

    private fun tempBanLogic(
        target: PlayerProfile,
        params: Map<String, Any>,
    ) {
        val reason = params["reason"] as String
        val sender = params["sender"] as CommandSender
        val duration = params["duration"] as Int
        val unit = params["unit"] as String
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                // TODO: Add some type of error message
                Logger.dev("Target is null")
                return
            }

            if (TargetUtils.isTargetBanned(offlinePlayer)) {
                // TODO: target is already banned
                //  Either add logic to update ban or return and throw error to player
                Logger.dev("Target ${target.name} is already banned")
                return
            }

            val parsedTime = try {
                TimeUtils().parseTime(duration, unit)
            } catch (e: IllegalArgumentException) {
                // TODO: Add error handling
                Logger.dev("Failed to parse time for tempban: ${e.message}")
                return
            }

            try {
                Bukkit.getBanList(BanListType.PROFILE).addBan(target, reason, parsedTime.time, sender.name)
            } catch (e: IllegalArgumentException) {
                // TODO: Add error handling
                Logger.dev("Failed to ban player ${target.name}: ${e.message}")
                return
            }

            KickLogic().triggerKick(
                listOf(target),
                reason,
                sender,
                true
            )

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                ModerationAction.TEMPBAN
            )
        } catch (e: Exception) {
            // TODO: Add error handling
            Logger.dev("Failed to tempban player ${target.name}: ${e.message}")
        }
    }
}