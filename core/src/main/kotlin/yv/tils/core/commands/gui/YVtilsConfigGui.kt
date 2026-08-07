/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.core.commands.gui

import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window
import yv.tils.common.language.LangStrings
import yv.tils.configv2.language.LanguageHandler
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Module

/**
 * InvUI-powered GUI for `/yvtils config`, listing every currently-active module that
 * registered a [Module.YVtilsModuleData.configGuiOpener] and opening that module's own
 * (`gui-v2`-backed, usually `DataClassConfigGui`) config editor on click.
 *
 * `core` has no compile-time dependency on any feature module (they're resolved and
 * instantiated dynamically at runtime - see `DynamicModuleDriver`), so this can't hold a
 * hardcoded `when (name) { "multiMine" -> ... }` dispatch table; instead, each module hands
 * its own opener closure to [Module.addModule] when constructing its `MODULE` data, and this
 * GUI just invokes whatever it finds already registered.
 *
 * Modules without a registered opener (e.g. `essentials`, whose only "config" is runtime
 * state rather than user settings) are simply not listed - there being nothing to open for
 * them, unlike `/yvtils modules` where every known module is always listed (togglable or
 * not).
 */
object YVtilsConfigGui {
    fun open(player: Player) {
        InvUIBootstrap.ensure()

        val entries = Module.getModules(sort = true).filter { it.configGuiOpener != null }
        buildWindow(player, entries).open(player)
    }

    private fun buildWindow(player: Player, entries: List<Module.YVtilsModuleData>): Window.Builder<*, *> {
        val items = entries.map { module -> buildModuleItem(player, module) }

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# # # < # > # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', pageButton(player, Heads.PREVIOUS_PAGE, "action.gui.nav.previousPage") { it.page-- })
            .addIngredient('>', pageButton(player, Heads.NEXT_PAGE, "action.gui.nav.nextPage") { it.page++ })
            .setContent(items)
            .build()

        return Window.builder()
            .setTitle(
                MessageUtils.convert(
                    "<${Colors.MAIN.color}>${LanguageHandler.getRawMessage(LangStrings.YVTILS_CONFIG_GUI_TITLE.key, player)}"
                )
            )
            .setUpperGui(gui)
    }

    private fun pageButton(
        player: Player,
        head: Heads,
        langKey: String,
        move: (PagedGui<*>) -> Unit
    ): BoundItem.Builder<PagedGui<*>> {
        return BoundItem.pagedBuilder()
            .setItemProvider { _, gui ->
                val available =
                    if (langKey == "action.gui.nav.previousPage") gui.page > 0 else gui.page < gui.pageCount - 1
                if (available) {
                    HeadUtils.provider(head, player, langKey)
                } else {
                    ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").hideTooltip(true)
                }
            }
            .addClickHandler { _, gui, _ -> move(gui) }
    }

    private fun buildModuleItem(player: Player, module: Module.YVtilsModuleData): Item {
        return Item.builder()
            .setItemProvider { buildModuleItemBuilder(player, module) }
            .addClickHandler { _, _ -> module.configGuiOpener?.accept(player) }
            .build()
    }

    private fun buildModuleItemBuilder(player: Player, module: Module.YVtilsModuleData): ItemBuilder {
        val displayName = "<${Colors.MAIN.color}>${module.name}"
        val head = ModuleIcons.ICONS[module.name]

        val builder = if (head != null) {
            ItemBuilder(HeadUtils.createCustomHead(head, displayName))
        } else {
            ItemBuilder(ModuleIcons.fallbackMaterial(module.name)).setName(displayName)
        }

        val loreLines = buildList {
            add("<dark_gray>————————")
            if (module.description.isNotBlank()) {
                add("<gray>${module.description}")
                add("<dark_gray>————————")
            }
            add("<white>Version: <gray>${module.version}")
            add("<dark_gray>————————")
            add(LanguageHandler.getRawMessage(LangStrings.YVTILS_CONFIG_GUI_LORE_OPEN.key, player))
        }

        return builder.addLoreLines(*loreLines.toTypedArray())
    }
}
