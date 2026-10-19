package yv.tils.essentials.commands.register

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import yv.tils.essentials.inventory.InventoryEditor
import yv.tils.essentials.inventory.InventoryKind

class InventoryInspection {
    init {
        InventoryKind.entries.forEach { kind ->
            commandTree(kind.command) {
                withPermission(kind.permission)
                stringArgument("target") {
                    replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        Bukkit.getOfflinePlayers().mapNotNull { it.name }.toTypedArray()
                    })
                    playerExecutor { viewer, args -> InventoryEditor.open(viewer, args["target"] as String, kind) }
                }
            }
        }
    }
}
