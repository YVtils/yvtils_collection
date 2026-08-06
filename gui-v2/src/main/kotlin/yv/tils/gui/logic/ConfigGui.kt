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

package yv.tils.gui.logic

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import yv.tils.configv2.data.ConfigEntry
import yv.tils.configv2.data.EntryType
import yv.tils.configv2.language.LanguageHandler
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils

/**
 * Builds an InvUI-powered GUI for editing a list of [ConfigEntry] values
 * (booleans, numbers, strings and string lists) and saving them back through
 * an optional `saver` callback.
 *
 * This replaces the legacy `gui` module's `ConfigGUI` + `ClickActions` +
 * `AsyncChat`/`InventoryClickListener` machinery: InvUI handles all
 * click/close events internally, and text input is collected through an
 * [AnvilWindow] instead of chat messages.
 */
object ConfigGui {
    /**
     * Opens (or re-opens) the config editor GUI for [player].
     *
     * @param configName Display name of the config, shown in the GUI title.
     * @param entries The config entries to edit. Mutated in place.
     * @param saver Invoked with [entries] when the GUI is closed with unsaved changes.
     */
    fun open(
        player: Player,
        configName: String,
        entries: MutableList<ConfigEntry>,
        saver: ((MutableList<ConfigEntry>) -> Unit)? = null
    ) {
        InvUIBootstrap.ensure()
        val state = State(configName, entries, saver)
        buildWindow(player, state).open(player)
    }

    private class State(
        val configName: String,
        val entries: MutableList<ConfigEntry>,
        val saver: ((MutableList<ConfigEntry>) -> Unit)?
    ) {
        var dirty = false
    }

    private fun displayEntries(state: State): List<ConfigEntry> =
        state.entries.filter { it.key != "documentation" }

    private fun buildWindow(player: Player, state: State): Window.Builder<*, *> {
        val items = displayEntries(state).map { entry -> buildEntryItem(player, entry, state) }

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
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${state.configName}"))
            .setUpperGui(gui)
            .addCloseHandler {
                if (state.dirty) saveConfig(player, state)
            }
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

    private fun saveConfig(player: Player, state: State) {
        try {
            state.saver?.invoke(state.entries)
            Logger.info("Config saved for ${state.configName}")
            player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.configSaved",
                    player,
                    mapOf("config" to state.configName)
                )
            )
        } catch (ex: Exception) {
            player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.configSaveFailed",
                    player,
                    mapOf("config" to state.configName, "error" to (ex.message ?: "Unknown error"))
                )
            )
            Logger.error("Failed to save config for ${state.configName}: ${ex.message}")
        }
    }

    // ---------------------------------------------------------------------
    // Entry items
    // ---------------------------------------------------------------------

    private fun buildEntryItem(player: Player, entry: ConfigEntry, state: State): Item {
        val material = entry.invItem ?: entry.dynamicInvItem?.invoke(entry) ?: Material.PAPER

        return Item.builder()
            .setItemProvider { buildEntryItemBuilder(material, entry, player) }
            .addClickHandler { item, click ->
                handleEntryClick(player, entry, click.clickType(), state)
                item.notifyWindows()
            }
            .build()
    }

    private fun buildEntryItemBuilder(material: Material, entry: ConfigEntry, player: Player): ItemBuilder {
        val valueLabel = LanguageHandler.getRawMessage("action.gui.lore.value", player)
        val defaultLabel = LanguageHandler.getRawMessage("action.gui.lore.default", player)
        val controlsKey = when (entry.type) {
            EntryType.BOOLEAN -> "action.gui.lore.controls.boolean"
            EntryType.INT, EntryType.DOUBLE -> "action.gui.lore.controls.number"
            EntryType.STRING -> "action.gui.lore.controls.text"
            EntryType.LIST, EntryType.MAP -> "action.gui.lore.controls.list"
            else -> null
        }

        val loreLines = buildList {
            add("<dark_gray>————————")
            if (!entry.description.isNullOrBlank()) {
                add("<gray>${entry.description}")
                add("<dark_gray>————————")
            }
            add("<white>$valueLabel: <green>${formatValue(entry.value, entry.defaultValue)}")
            add("<white>$defaultLabel: <yellow>${formatValue(entry.defaultValue)}")
            if (controlsKey != null) {
                add("<dark_gray> ")
                add(LanguageHandler.getRawMessage(controlsKey, player))
            }
            add("<dark_gray>————————")
        }

        return ItemBuilder(material)
            .setName("<${Colors.MAIN.color}>${entry.key}")
            .addLoreLines(*loreLines.toTypedArray())
    }

    private fun formatValue(value: Any?, fallback: Any? = null): String {
        val v = value ?: fallback ?: return "<none>"
        return when (v) {
            is List<*> -> {
                if (v.isEmpty()) return "<none>"
                val shown = v.take(5).joinToString(", ")
                if (v.size > 5) "$shown, ..." else shown
            }

            is Map<*, *> -> {
                if (v.isEmpty()) return "<none>"
                val shown = v.entries.take(5).joinToString(", ") { "${it.key}=${it.value}" }
                if (v.entries.size > 5) "$shown, ..." else shown
            }

            else -> v.toString()
        }
    }

    private fun handleEntryClick(player: Player, entry: ConfigEntry, click: ClickType, state: State) {
        when (entry.type) {
            EntryType.BOOLEAN -> if (click == ClickType.LEFT) {
                val current = entry.value as? Boolean ?: (entry.defaultValue as? Boolean ?: false)
                entry.value = !current
                state.dirty = true
            }

            EntryType.INT, EntryType.DOUBLE -> {
                val delta = when (click) {
                    ClickType.LEFT -> 1
                    ClickType.SHIFT_LEFT -> 10
                    ClickType.RIGHT -> -1
                    ClickType.SHIFT_RIGHT -> -10
                    else -> return
                }
                modifyNumericValue(entry, delta)
                state.dirty = true
            }

            EntryType.STRING -> if (click == ClickType.LEFT) {
                openTextInput(player, entry, state)
            }

            EntryType.LIST, EntryType.MAP -> if (click == ClickType.LEFT) {
                openListEditor(player, entry, state)
            }

            else -> player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.valueInfo",
                    player,
                    mapOf("value" to (entry.value ?: entry.defaultValue).toString())
                )
            )
        }
    }

    private fun modifyNumericValue(entry: ConfigEntry, delta: Int) {
        when (entry.type) {
            EntryType.INT -> {
                val current = (entry.value as? Number)?.toInt() ?: (entry.defaultValue as? Number)?.toInt() ?: 0
                entry.value = current + delta
            }

            EntryType.DOUBLE -> {
                val current = (entry.value as? Number)?.toDouble() ?: (entry.defaultValue as? Number)?.toDouble() ?: 0.0
                entry.value = current + delta.toDouble()
            }

            else -> {}
        }
    }

    // ---------------------------------------------------------------------
    // Text input (Anvil-based, replaces the legacy chat-based flow)
    // ---------------------------------------------------------------------

    private fun openTextInput(player: Player, entry: ConfigEntry, state: State) {
        var pending: String = (entry.value ?: entry.defaultValue)?.toString() ?: ""

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
            }
            .addClickHandler { _, click ->
                entry.value = pending
                state.dirty = true
                buildWindow(player, state).open(player)
            }
            .build()

        val upperGui = Gui.builder()
            .setStructure("# # c")
            .addIngredient('#', Filler.item())
            .addIngredient('c', confirmItem)
            .build()

        player.sendMessage(
            LanguageHandler.getMessage(
                "action.gui.enterValue.prompt",
                player,
                mapOf("key" to entry.key)
            )
        )

        AnvilWindow.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${entry.key}"))
            .setUpperGui(upperGui)
            .addRenameHandler { text -> pending = text }
            .setFallbackWindow(buildWindow(player, state).build(player))
            .open(player)
    }

    // ---------------------------------------------------------------------
    // List / Map editor
    // ---------------------------------------------------------------------

    private fun openListEditor(player: Player, entry: ConfigEntry, state: State) {
        @Suppress("UNCHECKED_CAST")
        val items = ((entry.value as? List<*>) ?: (entry.defaultValue as? List<*>))
            ?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()

        buildListEditorWindow(player, entry, items, state).open(player)
    }

    private fun buildListEditorWindow(
        player: Player,
        entry: ConfigEntry,
        items: MutableList<String>,
        state: State
    ): Window.Builder<*, *> {
        fun rebuild(): Window.Builder<*, *> = buildListEditorWindow(player, entry, items, state)

        fun commitAndGoBack() {
            entry.value = items.toList()
            state.dirty = true
            buildWindow(player, state).open(player)
        }

        val entryItems = items.map { name -> buildListEntryItem(player, name, items, ::rebuild) }

        val addItem = Item.builder()
            .setItemProvider { HeadUtils.provider(Heads.PLUS_CHARACTER, player, "action.gui.nav.addItem") }
            .addClickHandler { _, _ -> openAddItemInput(player, entry, items, state) }
            .build()

        val backItem = Item.builder()
            .setItemProvider { HeadUtils.provider(Heads.PREVIOUS_PAGE, player, "action.gui.nav.back") }
            .addClickHandler { _, _ -> commitAndGoBack() }
            .build()

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "b # # < # > # a #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', backItem)
            .addIngredient('a', addItem)
            .addIngredient('<', pageButton(player, Heads.PREVIOUS_PAGE, "action.gui.nav.previousPage") { it.page-- })
            .addIngredient('>', pageButton(player, Heads.NEXT_PAGE, "action.gui.nav.nextPage") { it.page++ })
            .setContent(entryItems)
            .build()

        return Window.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${entry.key}"))
            .setUpperGui(gui)
            .setFallbackWindow(buildWindow(player, state).build(player))
            .addCloseHandler {
                // if the player leaves via ESC/fallback, still persist the (possibly modified) list
                entry.value = items.toList()
                state.dirty = true
            }
    }

    private fun buildListEntryItem(
        player: Player,
        name: String,
        items: MutableList<String>,
        rebuild: () -> Window.Builder<*, *>
    ): Item {
        val material = try {
            Material.valueOf(name)
        } catch (_: Exception) {
            Material.BARRIER
        }

        return Item.builder()
            .setItemProvider {
                ItemBuilder(material)
                    .setName("<white>$name")
                    .addLoreLines(
                        "<dark_gray>————————",
                        LanguageHandler.getRawMessage("action.gui.lore.list.remove", player),
                        "<dark_gray>————————"
                    )
            }
            .addClickHandler { _, click ->
                if (click.clickType() == ClickType.RIGHT || click.clickType() == ClickType.SHIFT_RIGHT) {
                    items.remove(name)
                    rebuild().open(player)
                }
            }
            .build()
    }

    private fun openAddItemInput(
        player: Player,
        entry: ConfigEntry,
        items: MutableList<String>,
        state: State
    ) {
        var pending = ""

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
            }
            .addClickHandler { _, _ ->
                val name = pending.trim().uppercase().replace(' ', '_')
                if (name.isNotBlank()) {
                    try {
                        Material.valueOf(name)
                        items.add(name)
                        player.sendMessage(
                            LanguageHandler.getMessage(
                                "action.gui.itemAdded",
                                player,
                                mapOf("item" to name)
                            )
                        )
                    } catch (_: IllegalArgumentException) {
                        player.sendMessage(
                            LanguageHandler.getMessage(
                                "action.gui.invalidItem",
                                player,
                                mapOf("item" to name)
                            )
                        )
                    }
                }
                buildListEditorWindow(player, entry, items, state).open(player)
            }
            .build()

        val upperGui = Gui.builder()
            .setStructure("# # c")
            .addIngredient('#', Filler.item())
            .addIngredient('c', confirmItem)
            .build()

        player.sendMessage(LanguageHandler.getMessage("action.gui.enterValue.promptList", player))

        AnvilWindow.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${entry.key}"))
            .setUpperGui(upperGui)
            .addRenameHandler { text -> pending = text }
            .setFallbackWindow(buildListEditorWindow(player, entry, items, state).build(player))
            .open(player)
    }
}
