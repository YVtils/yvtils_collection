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
import yv.tils.moderation.configs.saveFile.WarnSaveFile
import yv.tils.moderation.configs.saveFile.Warning
import yv.tils.moderation.utils.ModerationAction
import yv.tils.moderation.utils.PlayerUtils
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.data.UUID
import yv.tils.utils.logger.Logger

class WarnLogic {
    /**
     * Trigger a warn
     * @param targets The target(s) to warn
     * @param reason Reason to show to the target(s)
     * @param sender The sender initiating the warn
     * @param silent Toggle to enable/disable the announcement
     */
    fun triggerWarn(
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

        TargetUtils.loopThroughTargets(targets, this::warnLogic, params)
    }

    private fun warnLogic(
        target: PlayerProfile,
        params: Map<String, Any>,
    ) {
        val reason = params["reason"] as String
        val sender = params["sender"] as CommandSender
        val silent = params["silent"] as Boolean

        try {
            val offlinePlayer = TargetUtils.parseTargetToOfflinePlayer(target) ?: run {
                // TODO: Add some type of error message
                Logger.dev("Offline player is null!")
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

            val warn = Warning(
                UUID.generateUUID().toString(),
                reason,
                modAction
            )

            WarnSaveFile().warnPlayer(offlinePlayer.uniqueId, warn)

            PlayerUtils.broadcastAction(
                target,
                reason,
                sender,
                silent,
                ModerationAction.WARN
            )
        } catch (e: Exception) {
            // TODO: Add error handling
            Logger.dev("An error occurred while trying to warn the player: ${e.message}")
        }
    }
}