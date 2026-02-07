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

package yv.tils.moderation.logic

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.ban.BanListType
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Exceptions
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.time.TimeUtils
import java.util.Calendar

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
        val parsedTime = try {
            TimeUtils().parseTime(duration, unit)
        } catch (e: IllegalArgumentException) {
            PlayerUtils.logicError(sender, Exceptions.TimeUnitParseException, e)
            return
        }

        val params = mapOf(
            "reason" to reason,
            "duration" to duration,
            "unit" to unit,
            "parsedTime" to parsedTime,
            "sender" to sender,
            "silent" to silent
        )

        TargetUtils.loopThroughTargets(targets, this::tempBanLogic, params)
    }

    private fun tempBanLogic(
        target: PlayerProfile,
        params: Map<String, Any>,
    ) {
        val reason = params["reason"] as String
        val duration = params["duration"] as Int
        val unit = params["unit"] as String
        val parsedTime = params["parsedTime"] as Calendar
        val sender = params["sender"] as CommandSender
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                PlayerUtils.logicError(sender, Exceptions.PlayerProfileToOfflinePlayerParseException)
                return
            }

            if (TargetUtils.isTargetBanned(offlinePlayer)) {
                sender.sendMessage(LanguageHandler.getMessage("command.moderation.player.already.banned", sender))
                return
            }

            try {
                Bukkit.getBanList(BanListType.PROFILE).addBan(target, reason, parsedTime.time, sender.name)
            } catch (e: IllegalArgumentException) {
                PlayerUtils.logicError(sender, Exceptions.PlayerProfileToOfflinePlayerParseException, e)
                return
            }

            KickLogic().triggerKick(
                listOf(target),
                "$reason<newline>" +
                        LanguageHandler.getRawMessage(
                            "moderation.placeholder.duration.expires",
                            sender,
                            mapOf("duration" to TimeUtils().formatDuration(parsedTime.timeInMillis - System.currentTimeMillis()))
                        ),
                sender,
                true
            )

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                duration = "$duration $unit",
                action = ModerationAction.TEMPBAN
            )
        } catch (e: Exception) {
            PlayerUtils.logicError(sender, Exceptions.ModerationActionException, e)
        }
    }
}