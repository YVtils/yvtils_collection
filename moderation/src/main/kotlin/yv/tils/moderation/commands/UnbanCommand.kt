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

package yv.tils.moderation.commands

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.asyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import org.bukkit.OfflinePlayer
import yv.tils.common.other.AsyncActionAnnounce
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.UnbanLogic
import yv.tils.utils.data.Data
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import java.util.concurrent.CompletableFuture

class UnbanCommand {
    val command = commandTree("unban") {
        withPermission(Permissions.COMMAND_MODERATION_UNBAN.permission.name)
        withUsage("unban <player> [reason]")
        withAliases("pardon")

        asyncPlayerProfileArgument("target") {
            replaceSuggestions(ArgumentSuggestions.stringsAsync { info ->
                CompletableFuture.supplyAsync {
                    val announceTask = AsyncActionAnnounce.announceSuggestion(info.sender)

                    val bannedPlayers: MutableSet<OfflinePlayer> = Data.instance.server.bannedPlayers
                    val bannedPlayersNames: MutableList<String> = mutableListOf()

                    for (player in bannedPlayers) {
                        if (player.name == null) {
                            continue
                        }

                        bannedPlayersNames.add(player.name!!)
                    }

                    announceTask.cancel()
                    val suggestions = bannedPlayersNames.toTypedArray()
                    suggestions
                }
            })

            greedyStringArgument("reason", true) {
                anyExecutor { sender, args ->
                    @Suppress("UNCHECKED_CAST")
                    val target = args["target"] as CompletableFuture<List<PlayerProfile>>
                    val reason = (args["reason"] ?: LanguageHandler.getRawMessage("moderation.placeholder.reason.none")) as String

                    val announceTask = AsyncActionAnnounce.announceAction(sender)

                    target.thenAccept { offlinePlayers ->
                        announceTask.cancel()
                        UnbanLogic().triggerUnban(offlinePlayers, reason, sender)
                    }.exceptionally { throwable ->
                        announceTask.cancel()
                        AsyncActionAnnounce.announcePlayerError(sender)
                        Logger.error("Failed to fetch player profiles for the command")
                        Logger.debug("Error details", throwable, DEBUGLEVEL.DETAILED)
                        null
                    }
                }
            }
        }
    }
}