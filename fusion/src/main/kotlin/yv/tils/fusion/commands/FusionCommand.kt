/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.commands

import dev.jorel.commandapi.kotlindsl.*
import yv.tils.configv2.language.LanguageHandler
import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.api.FusionRegistry
import yv.tils.fusion.configs.RecipeStore
import yv.tils.fusion.gui.FusionGui
import yv.tils.fusion.gui.RecipeEditor
import yv.tils.fusion.language.FusionText
import yv.tils.utils.logger.Logger

class FusionCommand {
    val command = commandTree("fusion") {
        withAliases("fc", "ccr")
        withPermission("yvtils.fusion.use")
        playerExecutor { player, _ -> FusionGui.browser(player) }
        literalArgument("manage") {
            withPermission("yvtils.fusion.manage")
            playerExecutor { player, _ -> FusionGui.browser(player, manage = true) }
        }
        literalArgument("new") {
            withPermission("yvtils.fusion.manage")
            stringArgument("id") {
                playerExecutor { player, args ->
                    val id = args["id"] as String
                    if (id.matches(Regex("[a-z0-9][a-z0-9_-]{0,63}")) && FusionRegistry.recipe(id) == null)
                        RecipeEditor.open(player, FusionRecipe(id = id), null)
                    else FusionText.send(player, "error", mapOf("error" to "Invalid or existing ID"))
                }
            }
        }
        literalArgument("reload") {
            withPermission("yvtils.fusion.manage")
            anyExecutor { sender, _ ->
                try {
                    RecipeStore.reload()
                    sender.sendMessage(LanguageHandler.getMessage("fusion.reloaded", sender))
                } catch (error: Exception) {
                    Logger.error("Fusion reload rejected", error)
                    sender.sendMessage(
                        LanguageHandler.getMessage(
                            "fusion.error", sender,
                            mapOf("error" to (error.message ?: "Unknown"))
                        )
                    )
                }
            }
        }
    }
}
