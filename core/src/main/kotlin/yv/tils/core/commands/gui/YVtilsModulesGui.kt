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

package yv.tils.core.commands.gui

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.state.MutableProperty
import xyz.xenondevs.invui.window.Window
import yv.tils.common.language.LangStrings
import yv.tils.configv2.language.LanguageHandler
import yv.tils.core.loader.DynamicModuleRegistry
import yv.tils.core.loader.ModuleConfig
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import java.nio.file.Path

/**
 * InvUI-powered GUI for `/yvtils modules`, listing every dynamically-fetchable
 * module known to [DynamicModuleRegistry] and letting the player toggle its
 * enabled state in the shared `modules.yml` (read/written through
 * [ModuleConfig]).
 *
 * Statically-embedded modules (`common`, `utils`, `config-v2`) are not
 * togglable - they are always loaded by every core - and are therefore not
 * listed here. Only entries from [DynamicModuleRegistry.KNOWN_MODULES] are
 * shown, excluding any marked [DynamicModuleRegistry.ModuleArtifact.hidden]
 * (e.g. `gui-v2`, `migration`) - those are excluded from the dynamic-module
 * system entirely (see [ModuleConfig.readEnabledModules]), not just from
 * this GUI, and cannot be enabled through `modules.yml` either.
 *
 * Layout: a grid of [COLUMNS] module-columns, each with its info item
 * (identity icon, description, version/author, current status) directly
 * above its toggle item (a checkmark/cross switch) - so info/toggle stay
 * paired within a column. This is a [ScrollGui] with
 * [ScrollGui.LineOrientation.VERTICAL] lines (i.e. it scrolls *horizontally*):
 * each "line" is one module-column (exactly 2 items tall: info, then
 * toggle), so the `<`/`>` buttons shift the visible window by exactly one
 * module at a time, while still showing [COLUMNS] modules on screen
 * simultaneously - unlike a page flip. Since each module only ever
 * contributes exactly 2 content entries (no padding/grouping needed), this
 * works cleanly for any module count.
 *
 * The info item is shown (with a description and last-known version/author)
 * even for modules that are currently disabled, since
 * [DynamicModuleRegistry.ModuleArtifact.description] is static registry data,
 * not dependent on the module's entry-point class actually having been
 * instantiated.
 *
 * Toggling a module here only updates `modules.yml`; since dynamic modules
 * are resolved and instantiated once at boot (`DynamicModuleDriver.discover`
 * during `onLoad()`), changes only take effect after the server is restarted
 * - this is reflected in each info item's lore.
 */
object YVtilsModulesGui {
    /** Module-columns visible on screen at once. */
    private const val COLUMNS = 7

    /**
     * Identity icons per module, shown on the info item instead of a generic
     * material. Modules without a mapping fall back to [fallbackMaterial].
     */
    private val MODULE_ICONS: Map<String, Heads> = mapOf(
        "discord" to Heads.DISCORD_LOGO,
        "multiMine" to Heads.PICKAXE,
        "essentials" to Heads.TOOLBOX,
        "sit" to Heads.OAK_STOOL,
        "status" to Heads.HEART_RED,
        "message" to Heads.ENVELOPE,
        "moderation" to Heads.SHIELD,
        "stats" to Heads.CHART,
        "server" to Heads.SERVER_RACK,
    )

    fun open(player: Player) {
        InvUIBootstrap.ensure()

        val state = State(ModuleConfig.sharedDataDirectory(Core.instance.dataFolder.toPath()))
        buildWindow(player, state).open(player)
    }

    private class State(val dataDirectory: Path) {
        val enabled: MutableSet<String> = ModuleConfig.readEnabledModules(dataDirectory).toMutableSet()

        /**
         * Persisted across [buildWindow] rebuilds (e.g. after toggling a
         * module) so the player's current scroll position isn't reset every
         * time they flip a switch.
         */
        val line: MutableProperty<Int> = MutableProperty.of(0)
    }

    private fun buildWindow(player: Player, state: State): Window.Builder<*, *> {
        val gui = ScrollGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "< # # # # # # # >"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_VERTICAL)
            .addIngredient('<', scrollButton(player, Heads.PREVIOUS_PAGE, "action.gui.nav.previousPage") { it.line-- })
            .addIngredient('>', scrollButton(player, Heads.NEXT_PAGE, "action.gui.nav.nextPage") { it.line++ })
            .setContent(buildContentItems(player, state))
            .setLine(state.line)
            .build()

        return Window.builder()
            .setTitle(
                MessageUtils.convert(
                    "<${Colors.MAIN.color}>${
                        LanguageHandler.getRawMessage(
                            LangStrings.YVTILS_MODULES_GUI_TITLE.key,
                            player
                        )
                    }"
                )
            )
            .setUpperGui(gui)
    }

    /**
     * Builds the flat content list handed to [ScrollGui]: an `[info, toggle]`
     * pair per module, in order. With [ScrollGui.LineOrientation.VERTICAL]
     * content slots, each consecutive pair maps to exactly one column (info
     * on top, toggle below) - no padding/grouping bookkeeping needed
     * regardless of how many modules there are.
     */
    private fun buildContentItems(player: Player, state: State): List<Item> {
        val names = DynamicModuleRegistry.KNOWN_MODULES
            .filterValues { !it.hidden }
            .keys
            .sorted()

        return names.flatMap { name ->
            listOf(buildInfoItem(player, name, state), buildToggleItem(player, name, state))
        }
    }

    private fun scrollButton(
        player: Player,
        head: Heads,
        langKey: String,
        move: (ScrollGui<*>) -> Unit
    ): BoundItem.Builder<ScrollGui<*>> {
        return BoundItem.scrollBuilder()
            .setItemProvider { _, gui ->
                val available =
                    if (langKey == "action.gui.nav.previousPage") gui.line > 0 else gui.line < gui.maxLine
                if (available) {
                    HeadUtils.provider(head, player, langKey)
                } else {
                    ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").hideTooltip(true)
                }
            }
            .addClickHandler { _, gui, _ -> move(gui) }
    }

    // ---------------------------------------------------------------------
    // Info item (top of a module's column) - always shown, even when the
    // module is currently disabled/unloaded.
    // ---------------------------------------------------------------------

    private fun buildInfoItem(player: Player, name: String, state: State): Item {
        val docs = Module.getModule(name)?.documentation ?: DynamicModuleRegistry.KNOWN_MODULES[name]?.let {
            "https://docs.yvtils.net/$name/"
        }

        return Item.builder()
            .setItemProvider { buildInfoItemBuilder(player, name, state) }
            .addClickHandler { _, _ ->
                if (docs != null) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>$docs"))
                }
            }
            .build()
    }

    private fun buildInfoItemBuilder(player: Player, name: String, state: State): ItemBuilder {
        val enabledInConfig = state.enabled.contains(name)
        val loadedData = Module.getModule(name)
        val loaded = loadedData != null
        val artifact = DynamicModuleRegistry.KNOWN_MODULES[name]

        val version = loadedData?.version ?: artifact?.version ?: "?"
        val author = loadedData?.author ?: "YVtils"
        val description = loadedData?.description?.takeIf { it.isNotBlank() } ?: artifact?.description

        val statusLine = if (enabledInConfig) {
            LanguageHandler.getRawMessage(LangStrings.YVTILS_MODULES_STATUS_ENABLED.key, player)
        } else {
            LanguageHandler.getRawMessage(LangStrings.YVTILS_MODULES_STATUS_DISABLED.key, player)
        }

        val pendingLine = when {
            enabledInConfig && !loaded ->
                LanguageHandler.getRawMessage(LangStrings.YVTILS_MODULES_STATUS_PENDING_ENABLE.key, player)

            !enabledInConfig && loaded ->
                LanguageHandler.getRawMessage(LangStrings.YVTILS_MODULES_STATUS_PENDING_DISABLE.key, player)

            else -> null
        }

        val loreLines = buildList {
            add("<dark_gray>————————")
            if (!description.isNullOrBlank()) {
                add("<gray>$description")
                add("<dark_gray>————————")
            }
            add("<white>Version: <gray>$version")
            add("<white>Author: <gray>$author")
            add("<dark_gray>————————")
            add(statusLine)
            if (pendingLine != null) {
                add(pendingLine)
            }
        }

        val displayName = "<${Colors.MAIN.color}>$name"
        val head = MODULE_ICONS[name]

        val builder = if (head != null) {
            ItemBuilder(HeadUtils.createCustomHead(head, displayName))
        } else {
            ItemBuilder(fallbackMaterial(name)).setName(displayName)
        }

        return builder.addLoreLines(*loreLines.toTypedArray())
    }

    /**
     * Material used for the info item when [MODULE_ICONS] has no dedicated
     * identity head for a module.
     */
    private fun fallbackMaterial(name: String): Material = when (name) {
        "regions" -> Material.FILLED_MAP
        else -> Material.PAPER
    }

    // ---------------------------------------------------------------------
    // Toggle item (bottom of a module's column) - flips the module's
    // enabled state in `modules.yml`.
    // ---------------------------------------------------------------------

    private fun buildToggleItem(player: Player, name: String, state: State): Item {
        return Item.builder()
            .setItemProvider { buildToggleItemBuilder(player, name, state) }
            .addClickHandler { _, _ ->
                toggleModule(name, state)
                buildWindow(player, state).open(player)
            }
            .build()
    }

    private fun buildToggleItemBuilder(player: Player, name: String, state: State): ItemBuilder {
        val enabledInConfig = state.enabled.contains(name)
        val head = if (enabledInConfig) Heads.CHECK_MARK else Heads.X_CHARACTER

        val nameKey = if (enabledInConfig) {
            LangStrings.YVTILS_MODULES_TOGGLE_DISABLE
        } else {
            LangStrings.YVTILS_MODULES_TOGGLE_ENABLE
        }

        val displayName = LanguageHandler.getRawMessage(nameKey.key, player)
        val base = HeadUtils.createCustomHead(head, displayName)

        return ItemBuilder(base)
            .addLoreLines(LanguageHandler.getRawMessage(LangStrings.YVTILS_MODULES_LORE_TOGGLE.key, player))
    }

    private fun toggleModule(name: String, state: State) {
        val enable = !state.enabled.contains(name)

        if (enable) {
            state.enabled.add(name)
        } else {
            state.enabled.remove(name)
        }

        ModuleConfig.setModuleEnabled(state.dataDirectory, name, enable)
    }
}
