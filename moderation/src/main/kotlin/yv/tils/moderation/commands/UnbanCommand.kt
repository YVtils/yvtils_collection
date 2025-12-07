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

package yv.tils.moderation.commands

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.OfflinePlayer
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.UnbanLogic
import yv.tils.utils.data.Data

class UnbanCommand {
    val command = commandTree("unban") {
        withPermission(Permissions.COMMAND_MODERATION_UNBAN.permission.name)
        withUsage("unban <player> [reason]")
        withAliases("pardon")

        playerProfileArgument("target") {
            replaceSuggestions(ArgumentSuggestions.strings { _ ->
                // TODO: Look into possibilities to optimize this
                val bannedPlayers: MutableSet<OfflinePlayer> = Data.instance.server.bannedPlayers
                val bannedPlayersNames: MutableList<String> = mutableListOf()

                for (player in bannedPlayers) {
                    if (player.name == null) {
                        continue
                    }

                    bannedPlayersNames.add(player.name!!)
                }

                val suggestions = bannedPlayersNames.toTypedArray()
                suggestions
            })
            greedyStringArgument("reason", true) {
                anyExecutor { sender, args ->
                    val target = args["target"] as List<PlayerProfile>
                    val reason = (args["reason"] ?: "No reason provided") as String // TODO: Localize

                    UnbanLogic().triggerUnban(target, reason, sender)
                }
            }
        }
    }
}