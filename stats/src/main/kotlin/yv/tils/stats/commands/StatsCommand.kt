/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.commands

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import yv.tils.stats.configs.ManageGUI
import yv.tils.stats.logic.StatsAdministration

/** Own root avoids coupling core's command tree to an optional feature module. */
internal object StatsCommand {
    fun register() {
        commandTree("yvtilsstats") {
            withRequirement(StatsAdministration::canConfigure)
            withUsage("yvtilsstats [config/optin <accept/decline>]")
            playerExecutor { player, _ -> ManageGUI().openGUI(player) }
            literalArgument("config") {
                playerExecutor { player, _ -> ManageGUI().openGUI(player) }
            }
            literalArgument("optin") {
                literalArgument("accept") {
                    anyExecutor { sender, _ -> StatsAdministration.setConsent(sender, true) }
                }
                literalArgument("decline") {
                    anyExecutor { sender, _ -> StatsAdministration.setConsent(sender, false) }
                }
            }
        }
    }

    fun unregister() {
        if (CommandAPI.isLoaded()) CommandAPI.unregister("yvtilsstats", true)
    }
}
