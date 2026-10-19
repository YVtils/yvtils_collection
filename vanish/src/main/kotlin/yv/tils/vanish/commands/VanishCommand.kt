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

package yv.tils.vanish.commands

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import yv.tils.vanish.configs.ManageGUI
import yv.tils.vanish.configs.PlayerSettings
import yv.tils.vanish.gui.SettingsGui
import yv.tils.vanish.logic.VanishService
import yv.tils.vanish.permissions.VanishPermissions

class VanishCommand(service: VanishService) {
    init {
        commandTree("vanish") {
            withAliases("v")
            withPermission(VanishPermissions.USE)
            playerExecutor { player, _ -> service.toggle(player, PlayerSettings.get(player.uniqueId).layer) }
            literalArgument("settings") {
                playerExecutor { player, _ -> SettingsGui(service).open(player) }
            }
            literalArgument("silent") {
                playerExecutor { player, _ -> service.toggle(player, PlayerSettings.get(player.uniqueId).layer, true) }
            }
            literalArgument("config") {
                withPermission(VanishPermissions.CONFIG)
                playerExecutor { player, _ -> ManageGUI.open(player) }
            }
            integerArgument("layer", 1, 100) {
                withPermission(VanishPermissions.LAYERS)
                playerExecutor { player, args -> service.toggle(player, args["layer"] as Int) }
            }
        }
    }
}
