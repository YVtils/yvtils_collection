package yv.tils.server.maintenance

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.stringArgument

class MaintenanceCMD {
    private val maintenanceHandler = MaintenanceHandler()

    val command = commandTree("maintenance") {
        withPermission("yvtils.command.maintenance")
        withPermission(CommandPermission.OP)
        withUsage("maintenance [state]")

        stringArgument("state", true) {
            replaceSuggestions(ArgumentSuggestions.strings("true", "false", "toggle"))
            anyExecutor { sender, args ->
                maintenanceHandler.maintenance(sender, args)
            }
        }

        anyExecutor { sender, _ ->
            maintenanceHandler.maintenance(sender)
        }
    }
}