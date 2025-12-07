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
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.moderation.configs.saveFile.ModAction
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.Logger
import yv.tils.utils.time.TimeUtils
import kotlin.time.Duration

class TempMuteLogic {
    /**
     * Trigger a mute
     * @param targets The target(s) to mute
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the mute
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerTempMute(
        targets: List<PlayerProfile>,
        reason: String,
        duration: Int,
        unit: String,
        sender: CommandSender,
        silent: Boolean = false
    ) {
        val params = mapOf(
            "reason" to reason,
            "duration" to duration,
            "unit" to unit,
            "sender" to sender,
            "silent" to silent
        )

        TargetUtils.loopThroughTargets(targets, this::tempMuteLogic, params)
    }

    private fun tempMuteLogic(
        target: PlayerProfile,
        params: Map<String, Any>,
    ) {
        val reason = params["reason"] as String
        val duration = params["duration"] as Int
        val unit = params["unit"] as String
        val sender = params["sender"] as CommandSender
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                // TODO: Add some type of error message
                Logger.dev("Offline player is null!")
                return
            }

            if (TargetUtils.isTargetMuted(offlinePlayer)) {
                // TODO: target is already muted
                //  Either add logic to update mute or return and throw error to player
                Logger.dev("Target is muted!")
                return
            }

            val modAction = if (sender is Player) {
                ModAction(
                    sender.uniqueId.toString(),
                    System.currentTimeMillis().toString()
                )
            } else {
                ModAction(
                    "console",
                    System.currentTimeMillis().toString()
                )
            }

            val parsedTime = try {
                TimeUtils().parseTime(duration, unit)
            } catch (e: IllegalArgumentException) {
                // TODO: Add error handling
                Logger.dev("Failed to parse time for tempmute: ${e.message}")
                return
            }

            MuteSaveFile().mutePlayer(offlinePlayer.uniqueId, reason, true, modAction,parsedTime.timeInMillis.toString())

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                ModerationAction.TEMPMUTE
            )
        } catch (e: Exception) {
            // TODO: Add error handling
            Logger.dev("An error occurred while trying to mute the player: ${e.message}")
        }
    }
}