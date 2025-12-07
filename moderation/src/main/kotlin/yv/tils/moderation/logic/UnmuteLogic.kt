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
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.Logger
import java.util.Date

class UnmuteLogic {
    /**
     * Trigger an unmute
     * @param targets The target(s) to unmute
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the unmute
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerUnmute(
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

        TargetUtils.loopThroughTargets(targets, this::unmuteLogic, params)
    }

    private fun unmuteLogic(
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

            if (!TargetUtils.isTargetMuted(offlinePlayer)) {
                // TODO: target is not muted
                //  Return and throw error to player
                Logger.dev("Target ${target.name} is not muted")
                return
            }

            try {
                MuteSaveFile().unmutePlayer(offlinePlayer.uniqueId)
            } catch (e: IllegalArgumentException) {
                // TODO: Add error handling
                Logger.dev("Failed to unmute player ${target.name}: ${e.message}")
                return
            }

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                ModerationAction.UNMUTE
            )
        } catch (e: Exception) {
            // TODO: Add error handling
            Logger.dev("Failed to unmute player ${target.name}: ${e.message}")
        }
    }
}