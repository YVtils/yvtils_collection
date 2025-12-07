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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import yv.tils.moderation.data.Permissions
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

class PlayerUtils {
    companion object {
        fun broadcastAction(
            target: PlayerProfile,
            reason: String,
            sender: CommandSender,
            silent: Boolean,
            action: ModerationAction = ModerationAction.OTHER
        ) {
            if (silent) return

            // Send message to all players with specific permission
            for (player in Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission(Permissions.MODERATION_BROADCAST.permission.name)) continue

                player.sendMessage("[TEMP MESSAGE] $action") // TODO: Add message
            }

            // Send message to console
            Logger.info("[TEMP MESSAGE] $action")
        }
    }
}

enum class ModerationAction {
    BAN,
    TEMPBAN,
    UNBAN,

    MUTE,
    TEMPMUTE,
    UNMUTE,

    KICK,

    WARN,

    OTHER;
}