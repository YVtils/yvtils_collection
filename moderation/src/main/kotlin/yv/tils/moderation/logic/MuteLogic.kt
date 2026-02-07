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
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.configs.saveFile.ModAction
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.data.Exceptions
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.logger.Logger

class MuteLogic {
    /**
     * Trigger a mute
     * @param targets The target(s) to mute
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the mute
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerMute(
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

        TargetUtils.loopThroughTargets(targets, this::muteLogic, params)
    }

    private fun muteLogic(
        target: PlayerProfile,
        params: Map<String, Any>,
    ) {
        val reason = params["reason"] as String
        val sender = params["sender"] as CommandSender
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                PlayerUtils.logicError(sender, Exceptions.PlayerProfileToOfflinePlayerParseException)
                return
            }

            if (TargetUtils.isTargetMuted(offlinePlayer)) {
                sender.sendMessage(LanguageHandler.getMessage("command.moderation.player.already.muted", sender))
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

            MuteSaveFile().mutePlayer(offlinePlayer.uniqueId, reason, true, modAction)

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                action = ModerationAction.MUTE
            )
        } catch (e: Exception) {
            PlayerUtils.logicError(sender, Exceptions.ModerationActionException, e)
        }
    }
}