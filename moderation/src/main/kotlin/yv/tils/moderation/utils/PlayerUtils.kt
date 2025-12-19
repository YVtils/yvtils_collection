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

package yv.tils.moderation.utils

import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageBroadcast
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Permissions
import yv.tils.utils.logger.Logger

class PlayerUtils {
    companion object {
        fun broadcastAction(
            target: PlayerProfile,
            reason: String,
            sender: CommandSender,
            silent: Boolean,
            duration: Int? = null,
            action: ModerationAction = ModerationAction.OTHER,
            offlineTarget: OfflinePlayer? = null,
        ) {
            if (silent) return

            val langKey = if (duration == null) {
                "command.moderation.broadcast.message.permanent"
            } else {
                "command.moderation.broadcast.message.temporary"
            }

            // Send message to all players with specific permission
            LanguageBroadcast.broadcast(
                langKey, Permissions.MODERATION_BROADCAST.permission.name,
                mapOf(
                    "target" to target.name!!,
                    "sender" to sender.name,
                    "reason" to reason,
                    "duration" to (duration ?: LanguageHandler.getMessage("moderation.placeholder.duration.none")),
                    "action" to LanguageHandler.getRawMessage(action.langString)
                )
            )

            // Send message to console
            Logger.info(LanguageHandler.getMessage(
                langKey.replace("broadcast", "target"),
                mapOf(
                    "reason" to reason,
                    "duration" to (duration ?: LanguageHandler.getMessage("moderation.placeholder.duration.none")),
                    "action" to LanguageHandler.getRawMessage(action.langString)
                )
            ))

            // Send message to target if online
            if (offlineTarget != null) {
                if (offlineTarget.isOnline) {
                    offlineTarget.player?.sendMessage(
                        LanguageHandler.getMessage(
                            langKey,
                            mapOf(
                                "target" to target.name!!,
                                "sender" to sender.name,
                                "reason" to reason,
                                "duration" to (duration
                                    ?: LanguageHandler.getMessage("moderation.placeholder.duration.none")),
                                "action" to LanguageHandler.getRawMessage(action.langString)
                            )
                        )
                    )
                }
            }
        }

        // TODO: Test this method
        fun logicError(sender: CommandSender, errorKey: Exception, extraInformation: Exception? = null) {
            val senderIsConsole = sender !is Player
            if (senderIsConsole) {
                Logger.error(errorKey.message!! + (if (extraInformation != null) "\n\n | $extraInformation" else ""))
            }
            if (!senderIsConsole) {
                sender.sendMessage(errorKey.message!! + (if (extraInformation != null) "\n\n | $extraInformation" else ""))
                Logger.error(errorKey.message!! + (if (extraInformation != null) "\n\n | $extraInformation" else ""))
            }
        }
    }
}

enum class ModerationAction(val langString: String) {
    BAN("command.moderation.broadcast.action.ban"),
    TEMPBAN("command.moderation.broadcast.action.tempban"),
    UNBAN("command.moderation.broadcast.action.unban"),

    MUTE("command.moderation.broadcast.action.mute"),
    TEMPMUTE("command.moderation.broadcast.action.tempmute"),
    UNMUTE("command.moderation.broadcast.action.unmute"),

    KICK("command.moderation.broadcast.action.kick"),

    WARN("command.moderation.broadcast.action.warn"),

    OTHER("command.moderation.broadcast.action.other");
}