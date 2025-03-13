package yv.tils.essentials.commands.register

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import yv.tils.essentials.commands.handler.SeedHandler

class SeedCMD {
    val command = commandTree("seed") {
        withPermission("yvtils.smp.command.seed")
        withPermission(CommandPermission.OP)
        withUsage("seed show")

        literalArgument("show", false) {
            anyExecutor { sender, _ ->
                val seedHandler = SeedHandler()
                seedHandler.seed(sender)
            }
        }
    }
}