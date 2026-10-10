/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.essentials.config

import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.essentials.language.LangStrings
import yv.tils.essentials.permissions.Permissions
import yv.tils.gui.logic.DataClassConfigGui

class ManageGUI {
    fun openGUI(player: Player) {
        if (!player.hasPermission(Permissions.CONFIG.permission.name)) {
            player.sendMessage(LanguageHandler.getMessage(LangStrings.CONFIG_DENIED, player))
            return
        }
        val original = ConfigFile.state.copy(
            spawnElytra = ConfigFile.state.spawnElytra.copy(
                worlds = ConfigFile.state.spawnElytra.worlds.toList()
            )
        )
        val draft =
            original.copy(spawnElytra = original.spawnElytra.copy(worlds = original.spawnElytra.worlds.toList()))
        var expected = original
        DataClassConfigGui.open(
            player,
            LanguageHandler.getRawMessage(LangStrings.CONFIG_TITLE.key, player),
            draft,
            saver = { updated ->
                check(player.hasPermission(Permissions.CONFIG.permission.name)) { "No permission to save Essentials configuration" }
                check(ConfigFile.state == expected) { "Essentials configuration changed; reopen the editor" }
                ConfigFile().applyState(updated)
                expected = ConfigFile.state
            }
        )
    }
}
