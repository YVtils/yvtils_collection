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

package yv.tils.yv_smp.gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.commons.provider.mutableProvider
import xyz.xenondevs.invui.ExperimentalReactiveApi
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.setItemProvider
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.addRenameHandler
import yv.tils.configv2.language.LanguageHandler
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.utils.player.PlayerUtils
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.logic.BorderHandler
import yv.tils.yv_smp.logic.SetupLogic
import yv.tils.yv_smp.logic.StartLogic
import yv.tils.yv_smp.logic.start.PhaseHandler
import yv.tils.yv_smp.permissions.Permissions

/**
 * InvUI-based control panel for `/yvsmp`, mirroring the `start`/`stop`/`setup`
 * command tree ([yv.tils.yv_smp.commands.YVSmpCommand]) as clickable menus.
 *
 * Every action performed from here goes through the exact same backing logic
 * as the commands ([StartLogic]/[SetupLogic]) - the GUI is a thin, permission-
 * aware presentation layer, not a separate code path. Since InvUI click
 * handlers bypass CommandAPI's own permission checks entirely, every button
 * re-checks the matching [Permissions] entry itself and greys out / explains
 * itself when the viewer lacks it.
 */
class YvSmpGui {

    /** Opens the main control menu for [player]. */
    fun openMainMenu(player: Player) {
        InvUIBootstrap.ensure()
        buildMainMenu(player).open(player)
    }

    // ── Main menu ──────────────────────────────────────────────────────────

    private fun buildMainMenu(player: Player): Window.Builder<*, *> {
        val running = PhaseHandler.isRunning

        val startItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_START,
            material = if (running) Material.GRAY_DYE else Material.LIME_DYE,
            name = LangStrings.GUI_ITEM_START_NAME,
            lore = listOf(if (running) LangStrings.GUI_ITEM_START_RUNNING_LORE else LangStrings.GUI_ITEM_START_LORE),
        ) {
            if (running) {
                it.sendMessage(LanguageHandler.getMessage(LangStrings.START_ALREADY_RUNNING, it))
            } else {
                buildConfirmStartWindow(it).open(it)
            }
        }

        val stopItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_START_STOP,
            material = if (running) Material.BARRIER else Material.GRAY_STAINED_GLASS_PANE,
            name = LangStrings.GUI_ITEM_STOP_NAME,
            lore = listOf(if (running) LangStrings.GUI_ITEM_STOP_LORE else LangStrings.GUI_ITEM_STOP_IDLE_LORE),
        ) {
            StartLogic().stop(it)
            buildMainMenu(it).open(it)
        }

        val statusItem = Item.builder()
            .setItemProvider { buildStatusItem(player) }
            .build()

        val setupItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_SETUP,
            material = Material.COMPASS,
            name = LangStrings.GUI_ITEM_SETUP_NAME,
            lore = listOf(LangStrings.GUI_ITEM_SETUP_LORE),
        ) {
            buildSetupMenu(it).open(it)
        }

        val closeItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.BARRIER).setName(
                    LanguageHandler.getMessage(
                        LangStrings.GUI_ITEM_CLOSE_NAME,
                        player
                    )
                )
            }
            .addClickHandler { _, click -> click.player().closeInventory() }
            .build()

        val gui = Gui.builder()
            .setStructure(
                "# # # # # # # # #",
                "# s # t # u # p #",
                "# # # # c # # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('s', startItem)
            .addIngredient('t', stopItem)
            .addIngredient('u', statusItem)
            .addIngredient('p', setupItem)
            .addIngredient('c', closeItem)
            .build()

        return Window.builder()
            .setTitle(LanguageHandler.getMessage(LangStrings.GUI_TITLE_MAIN, player))
            .setUpperGui(gui)
    }

    private fun buildStatusItem(player: Player): ItemBuilder {
        val running = PhaseHandler.isRunning
        val border = BorderHandler().getBorderStatus(player.world)
        val spawn = player.world.spawnLocation

        return ItemBuilder(Material.PAPER)
            .setName(LanguageHandler.getMessage(LangStrings.GUI_ITEM_STATUS_NAME, player))
            .addLoreLines(
                LanguageHandler.getMessage(
                    LangStrings.GUI_STATUS_LORE_PLAYERS,
                    player,
                    mapOf("count" to PlayerUtils.onlinePlayersAsCount.toString())
                ),
                LanguageHandler.getMessage(
                    LangStrings.GUI_STATUS_LORE_RUNNING,
                    player,
                    mapOf(
                        "status" to LanguageHandler.getMessage(
                            if (running) LangStrings.MUSIC_STATUS_YES else LangStrings.MUSIC_STATUS_NO,
                            player
                        )
                    )
                ),
                LanguageHandler.getMessage(
                    LangStrings.GUI_STATUS_LORE_BORDER,
                    player,
                    mapOf("status" to border)
                ),
                LanguageHandler.getMessage(
                    LangStrings.GUI_STATUS_LORE_SPAWN,
                    player,
                    mapOf(
                        "x" to spawn.blockX.toString(),
                        "y" to spawn.blockY.toString(),
                        "z" to spawn.blockZ.toString(),
                    )
                ),
            )
    }

    // ── Confirm start ──────────────────────────────────────────────────────

    private fun buildConfirmStartWindow(player: Player): Window.Builder<*, *> {
        val infoItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.BOOK)
                    .setName(LanguageHandler.getMessage(LangStrings.GUI_TITLE_CONFIRM_START, player))
                    .addLoreLines(
                        LanguageHandler.getMessage(
                            LangStrings.GUI_CONFIRM_START_WARNING_1,
                            player,
                            mapOf("count" to PlayerUtils.onlinePlayersAsCount.toString())
                        ),
                        LanguageHandler.getMessage(LangStrings.GUI_CONFIRM_START_WARNING_2, player),
                    )
            }
            .build()

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.LIME_WOOL).setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
            }
            .addClickHandler { _, click ->
                click.player().closeInventory()
                StartLogic().start(click.player())
            }
            .build()

        val cancelItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.RED_WOOL).setName(
                    LanguageHandler.getMessage(
                        LangStrings.GUI_CONFIRM_NO_NAME,
                        player
                    )
                )
            }
            .addClickHandler { _, click -> buildMainMenu(click.player()).open(click.player()) }
            .build()

        val gui = Gui.builder()
            .setStructure(
                "# # # # i # # # #",
                "# # y # # # n # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('i', infoItem)
            .addIngredient('y', confirmItem)
            .addIngredient('n', cancelItem)
            .build()

        return Window.builder()
            .setTitle(LanguageHandler.getMessage(LangStrings.GUI_TITLE_CONFIRM_START, player))
            .setUpperGui(gui)
            .setFallbackWindow(buildMainMenu(player).build(player))
    }

    // ── Setup menu ─────────────────────────────────────────────────────────

    private fun buildSetupMenu(player: Player): Window.Builder<*, *> {
        val borderSizeItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_SETUP_BORDER,
            material = Material.MAP,
            name = LangStrings.GUI_ITEM_BORDER_SIZE_NAME,
            lore = listOf(LangStrings.GUI_ITEM_BORDER_SIZE_LORE),
            loreParams = mapOf("size" to "%.0f".format(player.world.worldBorder.size)),
        ) {
            buildBorderSizeEditorWindow(it).open(it)
        }

        val borderCenterItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_SETUP_BORDERCENTER,
            material = Material.COMPASS,
            name = LangStrings.GUI_ITEM_BORDERCENTER_NAME,
            lore = listOf(LangStrings.GUI_ITEM_BORDERCENTER_LORE),
        ) {
            SetupLogic().setupBorderCenter(it)
            buildSetupMenu(it).open(it)
        }

        val spawnItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_SETUP_SPAWN,
            material = Material.RED_BED,
            name = LangStrings.GUI_ITEM_SPAWN_NAME,
            lore = listOf(LangStrings.GUI_ITEM_SPAWN_LORE),
        ) {
            SetupLogic().setupSpawn(it)
            buildSetupMenu(it).open(it)
        }

        val rescanItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_SETUP_RESCAN,
            material = Material.SPYGLASS,
            name = LangStrings.GUI_ITEM_RESCAN_NAME,
            lore = listOf(LangStrings.GUI_ITEM_RESCAN_LORE),
        ) {
            SetupLogic().setupRescan(it)
            buildSetupMenu(it).open(it)
        }

        val setupAllItem = actionItem(
            player = player,
            permission = Permissions.COMMAND_SETUP_ALL,
            material = Material.TNT,
            name = LangStrings.GUI_ITEM_SETUP_ALL_NAME,
            lore = listOf(LangStrings.GUI_ITEM_SETUP_ALL_LORE),
        ) {
            buildConfirmSetupAllWindow(it).open(it)
        }

        val backItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.ARROW).setName(LanguageHandler.getMessage("action.gui.nav.back", player))
            }
            .addClickHandler { _, click -> buildMainMenu(click.player()).open(click.player()) }
            .build()

        val gui = Gui.builder()
            .setStructure(
                "# # # # # # # # #",
                "# b # c # s # r #",
                "# # # # a # # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('b', borderSizeItem)
            .addIngredient('c', borderCenterItem)
            .addIngredient('s', spawnItem)
            .addIngredient('r', rescanItem)
            .addIngredient('a', setupAllItem)
            .build()

        return Window.builder()
            .setTitle(LanguageHandler.getMessage(LangStrings.GUI_TITLE_SETUP, player))
            .setUpperGui(gui)
            .setFallbackWindow(buildMainMenu(player).build(player))
    }

    private fun buildConfirmSetupAllWindow(player: Player): Window.Builder<*, *> {
        val infoItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.BOOK)
                    .setName(LanguageHandler.getMessage(LangStrings.GUI_TITLE_CONFIRM_SETUP_ALL, player))
                    .addLoreLines(
                        LanguageHandler.getMessage(LangStrings.GUI_CONFIRM_SETUP_ALL_WARNING_1, player),
                        LanguageHandler.getMessage(LangStrings.GUI_CONFIRM_SETUP_ALL_WARNING_2, player),
                    )
            }
            .build()

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.LIME_WOOL).setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
            }
            .addClickHandler { _, click ->
                SetupLogic().setupAll(click.player())
                buildSetupMenu(click.player()).open(click.player())
            }
            .build()

        val cancelItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.RED_WOOL).setName(
                    LanguageHandler.getMessage(
                        LangStrings.GUI_CONFIRM_NO_NAME,
                        player
                    )
                )
            }
            .addClickHandler { _, click -> buildSetupMenu(click.player()).open(click.player()) }
            .build()

        val gui = Gui.builder()
            .setStructure(
                "# # # # i # # # #",
                "# # y # # # n # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('i', infoItem)
            .addIngredient('y', confirmItem)
            .addIngredient('n', cancelItem)
            .build()

        return Window.builder()
            .setTitle(LanguageHandler.getMessage(LangStrings.GUI_TITLE_CONFIRM_SETUP_ALL, player))
            .setUpperGui(gui)
            .setFallbackWindow(buildSetupMenu(player).build(player))
    }

    // ── Border size editor (anvil) ────────────────────────────────────────

    @OptIn(ExperimentalReactiveApi::class)
    private fun buildBorderSizeEditorWindow(player: Player): AnvilWindow.Builder {
        val currentSize = "%.0f".format(player.world.worldBorder.size)
        val input = mutableProvider(currentSize)

        val currentItem = Item.simple(
            ItemBuilder(Material.MAP)
                .setCustomName(Component.text(currentSize))
                .addLoreLines(
                    LanguageHandler.getMessage(
                        LangStrings.GUI_ITEM_BORDER_SIZE_LORE,
                        player,
                        mapOf("size" to currentSize)
                    )
                )
        )

        val confirmItem = Item.builder()
            .setItemProvider(
                input.map { raw ->
                    val parsed = raw.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        ItemBuilder(Material.BARRIER)
                            .setName(LanguageHandler.getMessage(LangStrings.GUI_BORDER_EDITOR_INVALID, player))
                    } else {
                        ItemBuilder(Material.LIME_DYE)
                            .setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
                            .addLoreLines(
                                LanguageHandler.getMessage(
                                    LangStrings.GUI_ITEM_BORDER_SIZE_LORE,
                                    player,
                                    mapOf("size" to raw)
                                )
                            )
                    }
                }
            )
            .addClickHandler { _, click ->
                val parsed = input.get().toDoubleOrNull()
                if (parsed == null || parsed <= 0.0) {
                    click.player()
                        .sendMessage(LanguageHandler.getMessage(LangStrings.GUI_BORDER_EDITOR_INVALID, click.player()))
                    return@addClickHandler
                }

                SetupLogic().setupBorder(click.player(), parsed)
                buildSetupMenu(click.player()).open(click.player())
            }
            .build()

        val upperGui = Gui.builder()
            .setStructure("a b c")
            .addIngredient('a', currentItem)
            .addIngredient('b', Filler.item())
            .addIngredient('c', confirmItem)
            .build()

        return AnvilWindow.builder()
            .setTitle(LanguageHandler.getMessage(LangStrings.GUI_TITLE_BORDER_EDITOR, player))
            .setUpperGui(upperGui)
            .addRenameHandler(input)
            .setFallbackWindow(buildSetupMenu(player).build(player))
    }

    // ── Shared helpers ─────────────────────────────────────────────────────

    /**
     * Builds a simple clickable item that manually re-checks [permission] (InvUI
     * click handlers bypass CommandAPI's permission system entirely). Players
     * lacking the permission see a barrier item explaining why, instead of the
     * button silently doing nothing.
     */
    private fun actionItem(
        player: Player,
        permission: Permissions,
        material: Material,
        name: LangStrings,
        lore: List<LangStrings>,
        loreParams: Map<String, String> = emptyMap(),
        onClick: (Player) -> Unit,
    ): Item {
        val allowed = player.hasPermission(permission.permission.name)

        return Item.builder()
            .setItemProvider {
                val builder = ItemBuilder(if (allowed) material else Material.GRAY_STAINED_GLASS_PANE)
                    .setName(LanguageHandler.getMessage(name, player))

                val loreLines = lore.map { LanguageHandler.getMessage(it, player, loreParams) }.toMutableList()
                if (!allowed) loreLines.add(LanguageHandler.getMessage(LangStrings.GUI_NO_PERMISSION, player))

                builder.addLoreLines(*loreLines.toTypedArray())
            }
            .addClickHandler { _, click ->
                if (allowed) {
                    onClick(click.player())
                } else {
                    click.player()
                        .sendMessage(LanguageHandler.getMessage(LangStrings.GUI_NO_PERMISSION, click.player()))
                }
            }
            .build()
    }
}
