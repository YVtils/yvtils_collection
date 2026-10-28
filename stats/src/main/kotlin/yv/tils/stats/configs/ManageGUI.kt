/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.stats.configs

import org.bukkit.entity.Player
import org.bukkit.Material
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.gui.utils.Filler
import yv.tils.stats.language.RegisterStrings.LangStrings
import yv.tils.stats.logic.StatsAdministration
import yv.tils.utils.logger.Logger
import yv.tils.utils.stats.ModuleTelemetry

class ManageGUI {
    fun openGUI(sender: Player) {
        if (!StatsAdministration.requirePermission(sender)) return
        InvUIBootstrap.ensure()
        fun item(material: Material, key: LangStrings, action: () -> Unit): Item = Item.builder()
            .setItemProvider { ItemBuilder(material).setName(LanguageHandler.getMessage(key.key, sender)) }
            .addClickHandler { _, _ -> if (StatsAdministration.requirePermission(sender)) action() }
            .build()

        val status = when (ConfigFile.state.opt_in) {
            true -> LangStrings.GUI_ENABLED
            false -> LangStrings.GUI_DISABLED
            null -> LangStrings.GUI_UNDECIDED
        }
        val info = Item.builder().setItemProvider {
            ItemBuilder(Material.BOOK)
                .setName(LanguageHandler.getMessage(status.key, sender))
                .addLoreLines(*LanguageHandler.getRawMessage(LangStrings.GUI_PRIVACY.key, sender).split('\n').toTypedArray())
        }.build()
        val gui = Gui.builder().setStructure(
            "# # # # # # # # #",
            "# # a # i # d # #",
            "# # # # c # # # #",
        ).addIngredient('#', Filler.item())
            .addIngredient('i', info)
            .addIngredient('a', item(Material.LIME_DYE, LangStrings.GUI_ACCEPT) {
                StatsAdministration.setConsent(sender, true)
                openGUI(sender)
            })
            .addIngredient('d', item(Material.RED_DYE, LangStrings.GUI_DECLINE) {
                StatsAdministration.setConsent(sender, false)
                openGUI(sender)
            })
            .addIngredient('c', item(Material.COMPARATOR, LangStrings.GUI_SETTINGS) { openSettings(sender) })
            .build()
        Window.builder().setTitle(LanguageHandler.getMessage(LangStrings.GUI_TITLE.key, sender))
            .setUpperGui(gui).open(sender)
        ModuleTelemetry.record(ModuleTelemetry.Event.STATS_CONFIG_OPENS)
    }

    private fun openSettings(sender: Player) {
        if (!StatsAdministration.requirePermission(sender)) return
        DataClassConfigGui.openWithBack(
            sender,
            LanguageHandler.getRawMessage(LangStrings.GUI_SETTINGS.key, sender),
            // The editor mutates its input; do not change live consent before saving.
            ConfigFile.state.let { it.copy(metadata = it.metadata.copy()) },
            saver = { updated ->
                if (StatsAdministration.requirePermission(sender)) {
                    try {
                        // Another admin may have changed consent while this draft was open.
                        ConfigFile().applyState(updated.copy(opt_in = ConfigFile.state.opt_in))
                    } catch (e: Exception) {
                        Logger.error("[Stats] Could not save in-game configuration", e)
                        sender.sendMessage(LanguageHandler.getMessage(LangStrings.CONFIG_SAVE_FAILED.key, sender))
                    }
                }
            },
            back = { openGUI(sender) },
        )
    }
}
