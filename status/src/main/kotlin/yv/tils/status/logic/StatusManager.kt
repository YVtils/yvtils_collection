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

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.xenondevs.commons.provider.mutableProvider
import xyz.xenondevs.invui.ExperimentalReactiveApi
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.setItemProvider
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.addRenameHandler
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.status.utils.StatusUtils
import yv.tils.utils.message.MessageUtils

/**
 * Builds the InvUI-powered status management flow, split into three windows:
 *
 * - [buildMenuWindow]: the entry point, offering "edit", "clear" and "browse defaults".
 * - [buildEditorWindow]: an [AnvilWindow] with the current status (raw, editable) in the
 *   left slot and a live preview of the new status (rendered) in the result slot.
 * - [buildDefaultsWindow]: a paginated list of the configured default statuses.
 */
class StatusManager {
    fun manageStatus(
        player: Player,
        target: Player = player,
    ) {
        buildMenuWindow(player, target).open(player)
    }

    // ---------------------------------------------------------------------
    // Menu: clear / edit / browse defaults
    // ---------------------------------------------------------------------

    private fun buildMenuWindow(player: Player, target: Player): Window.Builder<*, *> {
        val statusHeadItem = Item.builder()
            .setItemProvider { buildStatusHead(player, target) }
            .build()

        val editItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.NAME_TAG)
                    .setName(LanguageHandler.getMessage("status.gui.menu.edit", player))
            }
            .addClickHandler { _, _ -> buildEditorWindow(player, target).open(player) }
            .build()

        val clearItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.BARRIER)
                    .setName(LanguageHandler.getMessage("status.gui.clear", player))
            }
            .addClickHandler { _, _ ->
                StatusUtils.setStatusDisplay(target, "")
                buildMenuWindow(player, target).open(player)
            }
            .build()

        val defaultsItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.BOOK)
                    .setName(LanguageHandler.getMessage("status.gui.menu.defaults", player))
            }
            .addClickHandler { _, _ -> buildDefaultsWindow(player, target, 0).open(player) }
            .build()

        val gui = Gui.builder()
            .setStructure(
                "h # # # # # # # #",
                "# # e # c # d # #",
                "# # # # # # # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('h', statusHeadItem)
            .addIngredient('e', editItem)
            .addIngredient('c', clearItem)
            .addIngredient('d', defaultsItem)
            .build()

        return Window.builder()
            .setTitle(LanguageHandler.getMessage("status.gui.title", player))
            .setUpperGui(gui)
    }

    private fun buildStatusHead(player: Player, target: Player): ItemBuilder {
        val status = StatusUtils.currentStatus(target)?.content ?: ""

        val head = ItemStack(Material.PLAYER_HEAD)
        val meta = head.itemMeta as SkullMeta
        meta.playerProfile = target.playerProfile
        head.itemMeta = meta

        return ItemBuilder(head)
            // setCustomName (not setName!): PLAYER_HEAD has a hardcoded
            // vanilla "<owner>'s Head" name override that only yields to
            // CUSTOM_NAME, not the cosmetic ITEM_NAME set by setName.
            .setCustomName(MessageUtils.convert("<white>${target.name}"))
            .addLoreLines(
                LanguageHandler.getMessage("status.gui.editor.currentLabel", player),
                if (status.isEmpty())
                    LanguageHandler.getMessage("status.gui.editor.empty", player)
                else
                    MessageUtils.convert(status)
            )
    }

    // ---------------------------------------------------------------------
    // Editor: anvil with raw current status -> rendered preview
    // ---------------------------------------------------------------------

    @OptIn(ExperimentalReactiveApi::class)
    private fun buildEditorWindow(player: Player, target: Player): AnvilWindow.Builder {
        val currentStatus = StatusUtils.currentStatus(target)?.content ?: ""
        val input = mutableProvider(currentStatus)

        // Static (not reactive!): this item sits in the anvil's actual input
        // slot. Vanilla anvils seed their rename text field from this item's
        // CUSTOM_NAME and treat any change to this item as "the input item
        // was swapped", resetting the text field. So it must be built once,
        // from the original status, and never touched again while editing.
        val currentItem = Item.simple(
            ItemBuilder(Material.NAME_TAG)
                // setCustomName (not setName!) is required here: anvils read
                // CUSTOM_NAME to populate their rename text field, while
                // setName only sets the cosmetic ITEM_NAME component.
                .setCustomName(Component.text(currentStatus))
                .addLoreLines(
                    LanguageHandler.getMessage("status.gui.editor.currentLabel", player),
                    if (currentStatus.isEmpty())
                        LanguageHandler.getMessage("status.gui.editor.empty", player)
                    else
                        MessageUtils.convert(currentStatus)
                )
        )

        val previewItem = Item.builder()
            .setItemProvider(
                input.map { raw ->
                    ItemBuilder(Material.LIME_DYE)
                        // Fully rendered preview as the title, matching the real display design.
                        .setName(
                            if (raw.isEmpty())
                                LanguageHandler.getMessage("status.gui.editor.empty", player)
                            else
                                MessageUtils.convert(raw)
                        )
                        .addLoreLines(
                            LanguageHandler.getMessage("action.gui.nav.confirm", player)
                        )
                }
            )
            .addClickHandler { _, _ ->
                StatusUtils.setStatusDisplay(target, input.get())
                buildMenuWindow(player, target).open(player)
            }
            .build()

        val upperGui = Gui.builder()
            .setStructure("a b c")
            .addIngredient('a', currentItem)
            .addIngredient('b', Filler.item())
            .addIngredient('c', previewItem)
            .build()

        return AnvilWindow.builder()
            .setTitle(LanguageHandler.getMessage("status.gui.title", player))
            .setUpperGui(upperGui)
            .addRenameHandler(input)
            .setFallbackWindow(buildMenuWindow(player, target).build(player))
    }

    // ---------------------------------------------------------------------
    // Defaults: paginated list of configured default statuses
    // ---------------------------------------------------------------------

    private fun buildDefaultsWindow(player: Player, target: Player, page: Int): Window.Builder<*, *> {
        val items = StatusUtils.generateDefaultStatus().map { status -> buildDefaultItem(player, target, status) }

        val backItem = Item.builder()
            .setItemProvider { HeadUtils.provider(Heads.PREVIOUS_PAGE, player, "action.gui.nav.back") }
            .addClickHandler { _, _ -> buildMenuWindow(player, target).open(player) }
            .build()

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "b # # < # > # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', backItem)
            .addIngredient('<', pageButton(player, Heads.PREVIOUS_PAGE, "action.gui.nav.previousPage") { it.page-- })
            .addIngredient('>', pageButton(player, Heads.NEXT_PAGE, "action.gui.nav.nextPage") { it.page++ })
            .setContent(items)
            .build()

        gui.page = page

        return Window.builder()
            .setTitle(LanguageHandler.getMessage("status.gui.menu.defaults", player))
            .setUpperGui(gui)
            .setFallbackWindow(buildMenuWindow(player, target).build(player))
    }

    private fun buildDefaultItem(player: Player, target: Player, status: String): Item =
        Item.builder()
            .setItemProvider {
                ItemBuilder(Material.NAME_TAG)
                    .setName(MessageUtils.convert(status))
                    .addLoreLines(
                        LanguageHandler.getMessage("action.gui.nav.confirm", player)
                    )
            }
            .addClickHandler { _, _ ->
                StatusUtils.setStatusDisplay(target, status)
                buildMenuWindow(player, target).open(player)
            }
            .build()

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
}
