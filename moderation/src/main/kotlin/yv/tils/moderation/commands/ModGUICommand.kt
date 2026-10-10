/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.moderation.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.gui.PlayerGUI

class ModGUICommand {
    val command = commandTree("modgui") {
        withPermission(Permissions.COMMAND_MODERATION_MODGUI.permission.name)
        withUsage("modgui [player-name-or-UUID]")
        playerExecutor { player, _ -> PlayerGUI().openGUI(player) }
        stringArgument("target") {
            replaceSuggestions(ArgumentSuggestions.strings {
                PlayerGUI.knownPlayers().map { it.name ?: it.uniqueId.toString() }.toTypedArray()
            })
            playerExecutor { player, args -> PlayerGUI().openTarget(player, args["target"] as String) }
        }
    }
}
