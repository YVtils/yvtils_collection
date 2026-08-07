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
import yv.tils.configv2.data.annotations.*
import yv.tils.configv2.language.LanguageHandler
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Reflection/annotation-driven equivalent of [ConfigGui], for Kotlin `data class` config
 * state (the kind persisted via `ObjectMapperFileUtils`) instead of a hand-built
 * `List<yv.tils.configv2.data.ConfigEntry>`.
 *
 * Where [ConfigGui] needs every field described up front as a `ConfigEntry` (key, type,
 * description, icon - all kept in sync by hand), this enumerates the target data class's
 * primary-constructor properties via reflection, deriving:
 * - the field's edit behavior from its Kotlin type (`Boolean` -> toggle, `Int`/`Double` ->
 *   increment, `String` -> anvil text input, `List<String>` -> list editor, another
 *   `data class` -> drills down into a sub-screen for it)
 * - its GUI description/icon from [ConfigDescription]/[ConfigIcon]/[BooleanIcon]/
 *   [DefaultValue] annotations on the property itself
 *
 * so adding a new editable config option only ever requires touching the data class
 * declaration - nothing here needs to change.
 *
 * Edits mutate the root [T] instance in place - including through any number of nested
 * `data class` fields, since navigating into one doesn't copy it, it just reflects over the
 * *same* nested object instance the root already holds a reference to - only `var`
 * properties are editable (`val` properties are shown read-only, same as an unrecognized
 * type). [saver] is invoked with the root instance whenever *any* screen in the session
 * (root or nested) closes with unsaved changes, mirroring [ConfigGui]'s dirty-tracking save
 * flow (harmless to invoke more than once per session - it's always the full current state).
 */
object DataClassConfigGui {
    /**
     * Opens (or re-opens) the config editor GUI for [player].
     *
     * @param configName Display name of the config, shown in the GUI title.
     * @param instance The live config state instance to edit. Mutated in place.
     * @param saver Invoked with [instance] when the GUI is closed with unsaved changes.
     */
    fun <T : Any> open(
        player: Player,
        configName: String,
        instance: T,
        saver: ((T) -> Unit)? = null,
    ) {
        val klass = instance::class
        if (!klass.isData) {
            Logger.error("DataClassConfigGui.open: ${klass.simpleName} is not a Kotlin data class, cannot build a GUI for it")
            return
        }

        InvUIBootstrap.ensure()
        val root = RootState(configName, instance, saver)
        buildLevelWindow(player, root, instance, configName, parent = null).open(player)
    }

    // ---------------------------------------------------------------------
    // Field discovery
    // ---------------------------------------------------------------------

    private enum class FieldKind {
        BOOLEAN, INT, DOUBLE, STRING, STRING_LIST, NESTED, UNKNOWN,
    }

    private class FieldDescriptor(
        val name: String,
        val kind: FieldKind,
        val description: String?,
        val defaultValue: String?,
        val staticIcon: Material?,
        val booleanIcon: BooleanIcon?,
        val isMaterialNameList: Boolean,
        val property: KProperty1<Any, Any?>,
        val mutableProperty: KMutableProperty1<Any, Any?>?,
    ) {
        fun get(instance: Any): Any? = property.get(instance)

        fun set(instance: Any, value: Any?) {
            mutableProperty?.set(instance, value)
        }
    }

    /** Fixed for the whole editing session, regardless of how deep a nested screen goes. */
    private class RootState<R : Any>(
        val configName: String,
        val rootInstance: R,
        val saver: ((R) -> Unit)?,
    ) {
        var dirty = false
    }

    /** One screen's worth of state - the root screen, or any nested `data class` drill-down. */
    private class Screen(
        val title: String,
        val instance: Any,
        val fields: List<FieldDescriptor>,
        val parent: (() -> Window.Builder<*, *>)?,
    )

    @Suppress("UNCHECKED_CAST")
    private fun discoverFields(instance: Any): List<FieldDescriptor> {
        val klass = instance::class
        val constructor = klass.primaryConstructor ?: return emptyList()
        val properties = klass.memberProperties.associateBy { it.name }

        return constructor.parameters.mapNotNull { param ->
            if (param.findAnnotation<NotGuiEditable>() != null) return@mapNotNull null
            val name = param.name ?: return@mapNotNull null
            val property = properties[name] as? KProperty1<Any, Any?> ?: return@mapNotNull null

            FieldDescriptor(
                name = name,
                kind = kindOf(param),
                description = param.findAnnotation<ConfigDescription>()?.value,
                defaultValue = param.findAnnotation<DefaultValue>()?.value,
                staticIcon = param.findAnnotation<ConfigIcon>()?.material,
                booleanIcon = param.findAnnotation<BooleanIcon>(),
                isMaterialNameList = param.findAnnotation<MaterialNameList>() != null,
                property = property,
                mutableProperty = property as? KMutableProperty1<Any, Any?>,
            )
        }
    }

    private fun kindOf(param: KParameter): FieldKind {
        val classifier = param.type.classifier
        return when {
            param.type == typeOf<Boolean>() -> FieldKind.BOOLEAN
            param.type == typeOf<Int>() -> FieldKind.INT
            param.type == typeOf<Double>() -> FieldKind.DOUBLE
            param.type == typeOf<String>() -> FieldKind.STRING
            classifier == List::class &&
                    param.type.arguments.firstOrNull()?.type == typeOf<String>() -> FieldKind.STRING_LIST

            classifier is KClass<*> && classifier.isData -> FieldKind.NESTED
            else -> FieldKind.UNKNOWN
        }
    }

    // ---------------------------------------------------------------------
    // Window / items (structurally mirrors ConfigGui)
    // ---------------------------------------------------------------------

    private fun <R : Any> buildLevelWindow(
        player: Player,
        root: RootState<R>,
        instance: Any,
        title: String,
        parent: (() -> Window.Builder<*, *>)?,
    ): Window.Builder<*, *> {
        val screen = Screen(title, instance, discoverFields(instance), parent)
        val items = screen.fields.map { field -> buildFieldItem(player, field, root, screen) }

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "b # # < # > # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', backItem(player, screen))
            .addIngredient('<', pageButton(player, Heads.PREVIOUS_PAGE, "action.gui.nav.previousPage") { it.page-- })
            .addIngredient('>', pageButton(player, Heads.NEXT_PAGE, "action.gui.nav.nextPage") { it.page++ })
            .setContent(items)
            .build()

        return Window.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>$title"))
            .setUpperGui(gui)
            .addCloseHandler {
                if (root.dirty) saveConfig(player, root)
            }
    }

    private fun backItem(player: Player, screen: Screen): Item {
        val parent = screen.parent ?: return Filler.item()

        return Item.builder()
            .setItemProvider { HeadUtils.provider(Heads.PREVIOUS_PAGE, player, "action.gui.nav.back") }
            .addClickHandler { _, _ -> parent().open(player) }
            .build()
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

    private fun <R : Any> saveConfig(player: Player, root: RootState<R>) {
        try {
            root.saver?.invoke(root.rootInstance)
            root.dirty = false
            Logger.info("Config saved for ${root.configName}")
            player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.configSaved",
                    player,
                    mapOf("config" to root.configName)
                )
            )
        } catch (ex: Exception) {
            player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.configSaveFailed",
                    player,
                    mapOf("config" to root.configName, "error" to (ex.message ?: "Unknown error"))
                )
            )
            Logger.error("Failed to save config for ${root.configName}: ${ex.message}")
        }
    }

    private fun <R : Any> buildFieldItem(
        player: Player,
        field: FieldDescriptor,
        root: RootState<R>,
        screen: Screen
    ): Item {
        return Item.builder()
            // Recomputed on every provider invocation (including after `notifyWindows()`),
            // NOT captured once up front - a `BooleanIcon`'s material depends on the field's
            // *current* value, which changes after every click.
            .setItemProvider {
                buildFieldItemBuilder(
                    resolveIcon(field, screen.instance),
                    field,
                    screen.instance,
                    player
                )
            }
            .addClickHandler { item, click ->
                handleFieldClick(player, field, click.clickType(), root, screen)
                item.notifyWindows()
            }
            .build()
    }

    private fun resolveIcon(field: FieldDescriptor, instance: Any): Material {
        field.staticIcon?.let { return it }
        field.booleanIcon?.let { icon ->
            val current = field.get(instance) as? Boolean ?: false
            return if (current) icon.whenTrue else icon.whenFalse
        }
        if (field.kind == FieldKind.NESTED) return Material.CHEST
        return Material.PAPER
    }

    private fun buildFieldItemBuilder(
        material: Material,
        field: FieldDescriptor,
        instance: Any,
        player: Player,
    ): ItemBuilder {
        val valueLabel = LanguageHandler.getRawMessage("action.gui.lore.value", player)
        val defaultLabel = LanguageHandler.getRawMessage("action.gui.lore.default", player)
        val controlsKey = when (field.kind) {
            FieldKind.BOOLEAN -> "action.gui.lore.controls.boolean"
            FieldKind.INT, FieldKind.DOUBLE -> "action.gui.lore.controls.number"
            FieldKind.STRING -> "action.gui.lore.controls.text"
            FieldKind.STRING_LIST -> "action.gui.lore.controls.list"
            FieldKind.NESTED -> "action.gui.lore.controls.nested"
            FieldKind.UNKNOWN -> null
        }

        val loreLines = buildList {
            add("<dark_gray>————————")
            if (!field.description.isNullOrBlank()) {
                add("<gray>${field.description}")
                add("<dark_gray>————————")
            }
            if (field.kind != FieldKind.NESTED) {
                add("<white>$valueLabel: <green>${formatValue(field.get(instance))}")
                if (!field.defaultValue.isNullOrBlank()) {
                    add("<white>$defaultLabel: <yellow>${field.defaultValue}")
                }
                if (field.mutableProperty == null) {
                    add("<dark_gray>(read-only)")
                }
            }
            if (controlsKey != null && (field.kind == FieldKind.NESTED || field.mutableProperty != null)) {
                add("<dark_gray> ")
                add(LanguageHandler.getRawMessage(controlsKey, player))
            }
            add("<dark_gray>————————")
        }

        return ItemBuilder(material)
            .setName("<${Colors.MAIN.color}>${field.name}")
            .addLoreLines(*loreLines.toTypedArray())
    }

    private fun formatValue(value: Any?): String {
        val v = value ?: return "<none>"
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

    private fun <R : Any> handleFieldClick(
        player: Player,
        field: FieldDescriptor,
        click: ClickType,
        root: RootState<R>,
        screen: Screen,
    ) {
        if (field.kind == FieldKind.NESTED) {
            if (click == ClickType.LEFT) openNestedGui(player, field, root, screen)
            return
        }

        if (field.mutableProperty == null) {
            player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.valueInfo",
                    player,
                    mapOf("value" to field.get(screen.instance).toString())
                )
            )
            return
        }

        when (field.kind) {
            FieldKind.BOOLEAN -> if (click == ClickType.LEFT) {
                val current = field.get(screen.instance) as? Boolean ?: false
                field.set(screen.instance, !current)
                root.dirty = true
            }

            FieldKind.INT, FieldKind.DOUBLE -> {
                val delta = when (click) {
                    ClickType.LEFT -> 1
                    ClickType.SHIFT_LEFT -> 10
                    ClickType.RIGHT -> -1
                    ClickType.SHIFT_RIGHT -> -10
                    else -> return
                }
                modifyNumericValue(field, screen.instance, delta)
                root.dirty = true
            }

            FieldKind.STRING -> if (click == ClickType.LEFT) {
                openTextInput(player, field, root, screen)
            }

            FieldKind.STRING_LIST -> if (click == ClickType.LEFT) {
                openListEditor(player, field, root, screen)
            }

            else -> player.sendMessage(
                LanguageHandler.getMessage(
                    "action.gui.valueInfo",
                    player,
                    mapOf("value" to field.get(screen.instance).toString())
                )
            )
        }
    }

    private fun modifyNumericValue(field: FieldDescriptor, instance: Any, delta: Int) {
        when (field.kind) {
            FieldKind.INT -> {
                val current = (field.get(instance) as? Number)?.toInt() ?: 0
                field.set(instance, current + delta)
            }

            FieldKind.DOUBLE -> {
                val current = (field.get(instance) as? Number)?.toDouble() ?: 0.0
                field.set(instance, current + delta.toDouble())
            }

            else -> {}
        }
    }

    // ---------------------------------------------------------------------
    // Nested data class drill-down
    // ---------------------------------------------------------------------

    /**
     * Opens a sub-screen for a nested `data class` field. No copying happens - the nested
     * object is the exact same instance already referenced by [screen], so mutating its
     * properties in the sub-screen is immediately reflected in the parent (and root).
     */
    private fun <R : Any> openNestedGui(player: Player, field: FieldDescriptor, root: RootState<R>, screen: Screen) {
        val nestedInstance = field.get(screen.instance) ?: return
        val parentBuilder = { buildLevelWindow(player, root, screen.instance, screen.title, screen.parent) }

        buildLevelWindow(player, root, nestedInstance, field.name, parent = parentBuilder).open(player)
    }

    // ---------------------------------------------------------------------
    // Text input (Anvil-based)
    // ---------------------------------------------------------------------

    private fun <R : Any> openTextInput(player: Player, field: FieldDescriptor, root: RootState<R>, screen: Screen) {
        var pending: String = field.get(screen.instance)?.toString() ?: ""

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
            }
            .addClickHandler { _, _ ->
                field.set(screen.instance, pending)
                root.dirty = true
                buildLevelWindow(player, root, screen.instance, screen.title, screen.parent).open(player)
            }
            .build()

        val cancelItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setName(LanguageHandler.getMessage("action.gui.nav.cancel", player))
            }
            .addClickHandler { _, _ ->
                buildLevelWindow(player, root, screen.instance, screen.title, screen.parent).open(player)
            }
            .build()

        val upperGui = Gui.builder()
            .setStructure("# b c")
            .addIngredient('#', Filler.item())
            .addIngredient('b', cancelItem)
            .addIngredient('c', confirmItem)
            .build()

        player.sendMessage(
            LanguageHandler.getMessage(
                "action.gui.enterValue.prompt",
                player,
                mapOf("key" to field.name)
            )
        )

        AnvilWindow.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${field.name}"))
            .setUpperGui(upperGui)
            .addRenameHandler { text -> pending = text }
            .setFallbackWindow(
                buildLevelWindow(
                    player,
                    root,
                    screen.instance,
                    screen.title,
                    screen.parent
                ).build(player)
            )
            .open(player)
    }

    // ---------------------------------------------------------------------
    // String list editor
    // ---------------------------------------------------------------------

    private fun <R : Any> openListEditor(player: Player, field: FieldDescriptor, root: RootState<R>, screen: Screen) {
        @Suppress("UNCHECKED_CAST")
        val items = (field.get(screen.instance) as? List<String>)?.toMutableList() ?: mutableListOf()

        buildListEditorWindow(player, field, items, root, screen).open(player)
    }

    private fun <R : Any> buildListEditorWindow(
        player: Player,
        field: FieldDescriptor,
        items: MutableList<String>,
        root: RootState<R>,
        screen: Screen,
    ): Window.Builder<*, *> {
        fun rebuild(): Window.Builder<*, *> = buildListEditorWindow(player, field, items, root, screen)

        fun commitAndGoBack() {
            field.set(screen.instance, items.toList())
            root.dirty = true
            buildLevelWindow(player, root, screen.instance, screen.title, screen.parent).open(player)
        }

        val entryItems =
            items.map { name -> buildListEntryItem(player, name, field.isMaterialNameList, items, ::rebuild) }

        val addItem = Item.builder()
            .setItemProvider { HeadUtils.provider(Heads.PLUS_CHARACTER, player, "action.gui.nav.addItem") }
            .addClickHandler { _, _ -> openAddItemInput(player, field, items, root, screen) }
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
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${field.name}"))
            .setUpperGui(gui)
            .setFallbackWindow(
                buildLevelWindow(
                    player,
                    root,
                    screen.instance,
                    screen.title,
                    screen.parent
                ).build(player)
            )
            .addCloseHandler {
                // if the player leaves via ESC/fallback, still persist the (possibly modified) list
                field.set(screen.instance, items.toList())
                root.dirty = true
                saveConfig(player, root)
            }
    }

    private fun buildListEntryItem(
        player: Player,
        name: String,
        isMaterialNameList: Boolean,
        items: MutableList<String>,
        rebuild: () -> Window.Builder<*, *>,
    ): Item {
        val material = if (isMaterialNameList) {
            try {
                Material.valueOf(name)
            } catch (_: Exception) {
                Material.BARRIER
            }
        } else {
            Material.PAPER
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

    private fun <R : Any> openAddItemInput(
        player: Player,
        field: FieldDescriptor,
        items: MutableList<String>,
        root: RootState<R>,
        screen: Screen,
    ) {
        var pending = ""

        val confirmItem = Item.builder()
            .setItemProvider {
                ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .setName(LanguageHandler.getMessage("action.gui.nav.confirm", player))
            }
            .addClickHandler { _, _ ->
                val name =
                    if (field.isMaterialNameList) pending.trim().uppercase().replace(' ', '_') else pending.trim()
                if (name.isNotBlank()) {
                    if (field.isMaterialNameList) {
                        try {
                            Material.valueOf(name)
                            items.add(name)
                            player.sendMessage(
                                LanguageHandler.getMessage("action.gui.itemAdded", player, mapOf("item" to name))
                            )
                        } catch (_: IllegalArgumentException) {
                            player.sendMessage(
                                LanguageHandler.getMessage("action.gui.invalidItem", player, mapOf("item" to name))
                            )
                        }
                    } else {
                        items.add(name)
                        player.sendMessage(
                            LanguageHandler.getMessage("action.gui.itemAdded", player, mapOf("item" to name))
                        )
                    }
                }
                buildListEditorWindow(player, field, items, root, screen).open(player)
            }
            .build()

        val upperGui = Gui.builder()
            .setStructure("# # c")
            .addIngredient('#', Filler.item())
            .addIngredient('c', confirmItem)
            .build()

        player.sendMessage(LanguageHandler.getMessage("action.gui.enterValue.promptList", player))

        AnvilWindow.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>${field.name}"))
            .setUpperGui(upperGui)
            .addRenameHandler { text -> pending = text }
            .setFallbackWindow(buildListEditorWindow(player, field, items, root, screen).build(player))
            .open(player)
    }
}
