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
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerKickEvent
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Exceptions
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils

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
                PlayerUtils.logicError(sender, Exceptions.PlayerProfileToOfflinePlayerParseException)
                return
            }

            if (!TargetUtils.isTargetOnline(offlinePlayer)) {
                if (silent) return

                sender.sendMessage(LanguageHandler.getMessage("command.moderation.player.not.online", sender))
                return
            }

            val player = offlinePlayer.player ?: run {
                PlayerUtils.logicError(sender, Exceptions.ModerationActionException)
                Logger.debug("KickLogic.kickLogic: offlinePlayer.player is null despite being online", DEBUGLEVEL.BASIC)
                return
            }

            val styledReason = MessageUtils.convert(
                """
                    ${LanguageHandler.getRawMessage("moderation.target.disconnected", player)}
                    
                    <white>$reason
                """.trimIndent()
            )

            player.kick(styledReason, PlayerKickEvent.Cause.PLUGIN)

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                action = ModerationAction.KICK
            )
        } catch (e: Exception) {
            PlayerUtils.logicError(sender, Exceptions.ModerationActionException, e)
        }
    }
}