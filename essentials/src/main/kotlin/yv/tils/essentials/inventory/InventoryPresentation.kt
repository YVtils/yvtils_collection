package yv.tils.essentials.inventory

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.SlotElement
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.ReferencingInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import yv.tils.config.language.LanguageHandler
import yv.tils.essentials.language.LangStrings
import yv.tils.utils.message.MessageUtils

internal object InventoryPresentation {
    fun message(key: LangStrings, viewer: Player, params: Map<String, Any> = emptyMap()): Component =
        LanguageHandler.getMessage(key, viewer, params)

    fun window(
        session: InventorySession,
        viewer: Player,
        targetName: String,
        exchange: (Int, Click, Boolean) -> Unit,
        preUpdate: (ItemPreUpdateEvent) -> Unit,
        closed: () -> Unit
    ): Window {
        val background = background(session, viewer, targetName)
        session.inventory = VirtualInventory(session.expected)
        session.adminInventory = ReferencingInventory.fromPlayerStorageContents(viewer.inventory)
        session.inventory.addPreUpdateHandler(preUpdate)
        session.adminInventory.addPreUpdateHandler(preUpdate)
        session.inventory.addClickHandler { event ->
            event.isCancelled = true
            exchange(event.slot, Click(event.player, event.clickType, event.hotbarButton), false)
        }
        session.adminInventory.addClickHandler { event ->
            if (event.clickType.isShiftClick || session.dragPending) {
                event.isCancelled = true
                exchange((event.slot + 9) % 36, Click(event.player, event.clickType, event.hotbarButton), true)
            }
        }
        val gui = Gui.empty(9, session.kind.viewSize / 9)
        (0 until session.kind.viewSize).forEach { viewSlot ->
            val targetSlot = session.kind.targetSlot(viewSlot)
            if (targetSlot == null) {
                gui.setItem(viewSlot, background[viewSlot]?.let(Item::simple) ?: Item.EMPTY)
            } else {
                gui.setSlotElement(viewSlot, SlotElement.InventoryLink(
                    session.inventory, targetSlot, background[viewSlot]?.let(::ItemWrapper)
                ))
            }
        }
        val lower = Gui.empty(9, 4)
        (0 until 36).forEach { lower.setSlotElement(it, SlotElement.InventoryLink(session.adminInventory, it)) }
        return Window.builder()
            .setUpperGui(gui)
            .setLowerGui(lower)
            .setTitle(
                message(
                    if (session.kind == InventoryKind.PLAYER) LangStrings.INVENTORY_PLAYER_TITLE else LangStrings.INVENTORY_ENDER_TITLE,
                    viewer, mapOf("player" to targetName)
                )
            )
            .addCloseHandler { _ -> closed() }
            .build(viewer)
    }

    private fun background(session: InventorySession, viewer: Player, targetName: String): Array<ItemStack?> {
        fun icon(material: Material, key: LangStrings, vararg lore: Component): ItemStack =
            ItemStack(material).apply {
                editMeta { meta ->
                    meta.displayName(message(key, viewer).decoration(TextDecoration.ITALIC, false))
                    meta.lore(lore.flatMap { MessageUtils.handleLore(it) }
                        .map { it.decoration(TextDecoration.ITALIC, false) })
                }
            }

        val background = arrayOfNulls<ItemStack>(session.kind.viewSize)
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            editMeta { it.displayName(Component.text(" ")) }
        }
        (0..8).forEach { background[it] = filler }
        background[0] = icon(
            Material.BOOK, LangStrings.INVENTORY_INFO,
            message(LangStrings.INVENTORY_TARGET, viewer, mapOf("player" to targetName)),
            message(if (session.offline) LangStrings.INVENTORY_OFFLINE else LangStrings.INVENTORY_ONLINE, viewer),
            message(
                if (viewer.hasPermission(session.kind.editPermission)) LangStrings.INVENTORY_EDIT_HINT
                else LangStrings.INVENTORY_READ_ONLY_HINT, viewer
            )
        )
        if (session.kind == InventoryKind.PLAYER) {
            mapOf(
                2 to LangStrings.INVENTORY_HELMET,
                3 to LangStrings.INVENTORY_CHESTPLATE,
                4 to LangStrings.INVENTORY_LEGGINGS,
                5 to LangStrings.INVENTORY_BOOTS,
                7 to LangStrings.INVENTORY_OFFHAND
            ).forEach { (slot, key) ->
                background[slot] = icon(Material.LIME_STAINED_GLASS_PANE, key, message(LangStrings.INVENTORY_EMPTY, viewer))
            }
            (36..44).forEach { background[it] = filler }
            background[36] = icon(Material.CHEST, LangStrings.INVENTORY_STORAGE)
            background[40] = icon(Material.LIME_STAINED_GLASS_PANE, LangStrings.INVENTORY_HOTBAR)
        } else {
            background[4] = icon(Material.ENDER_CHEST, LangStrings.INVENTORY_ENDER_CHEST)
        }
        return background
    }
}
