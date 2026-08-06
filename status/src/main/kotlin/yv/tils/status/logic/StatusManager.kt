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

package yv.tils.status.logic

import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.commons.provider.mutableProvider
import xyz.xenondevs.invui.ExperimentalReactiveApi
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.addRenameHandler
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.utils.Filler
import yv.tils.utils.message.MessageUtils

class StatusManager {
    @OptIn(ExperimentalReactiveApi::class)
    fun manageStatus(
        player: Player,
        taget: Any?,
    ) {


//        val currentStatus = StatusUtils.currentStatus(target)?.content ?: ""
        val search = mutableProvider("currentStatus")

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.NAME_TAG)
                    .setName(MessageUtils.convert(search.get()))
            }
            .addClickHandler { _, click ->

            }
            .build()

        val clearItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.BARRIER)
                    .setName(
                        LanguageHandler.getMessage(
                            "status.gui.clear",
                            player
                        )
                    )
            }

        val upperGUI: Gui = Gui.builder()
            .setStructure("a b c")
            .addIngredient('a', clearItem)
            .addIngredient('b', Filler.item())
            .addIngredient('c', confirmItem)
            .build()

        AnvilWindow.builder()
            .addRenameHandler(search)
            .setUpperGui(upperGUI)
            .open(player)
    }
}