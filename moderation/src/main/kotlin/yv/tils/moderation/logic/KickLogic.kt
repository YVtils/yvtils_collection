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
import dev.jorel.commandapi.CommandAPILogger.silent
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerKickEvent
import org.jline.utils.Log
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.StyleReason
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.Logger

class KickLogic {
    /**
     * Trigger a kick
     * @param targets The target(s) to kick
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the kick
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerKick(
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

        TargetUtils.loopThroughTargets(targets, this::kickLogic, params)
    }

    private fun kickLogic(target: PlayerProfile, params: Map<String, Any>) {
        val reason = params["reason"] as String
        val sender = params["sender"] as CommandSender
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                // TODO: Add some type of error message
                //  Only if not silent
                Logger.dev("Failed to parse offline player ${target.name}")
                return
            }

            if (!TargetUtils.isTargetOnline(offlinePlayer)) {
                // TODO: target is not online
                Logger.dev("Failed to parse offline player ${offlinePlayer.name}")
                return
            }

            val player = offlinePlayer.player ?: run {
                // TODO: Add some type of error message
                // This should never occur as an error
                Logger.dev("Offline player's player is null for ${offlinePlayer.name}")
                return
            }

            val styledReason = StyleReason.styleReason(reason)
            player.kick(styledReason, PlayerKickEvent.Cause.KICK_COMMAND)

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                ModerationAction.KICK
            )
        } catch (e: Exception) {
            // TODO: Add error handling
            Logger.dev("Failed to kick player ${target.name}: ${e.message}")
        }
    }
}