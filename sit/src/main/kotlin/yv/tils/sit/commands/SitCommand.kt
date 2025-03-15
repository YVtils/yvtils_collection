package yv.tils.sit.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import yv.tils.sit.logic.SitManager

class SitCommand {
    val command = commandTree("sit") {
        withPermission("yvtils.smp.command.sit")
        withPermission(CommandPermission.NONE)
        withUsage("sit")
        withAliases("chair")

        playerExecutor { player, _ ->
            if (SitManager().isSitting(player.uniqueId)) {
                SitManager().sitGetter(player)
            } else {
                SitManager().sit(player)
            }
        }
    }
}