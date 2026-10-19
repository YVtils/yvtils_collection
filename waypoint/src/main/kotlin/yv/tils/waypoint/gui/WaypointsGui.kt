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

package yv.tils.waypoint.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.GuiStyle
import yv.tils.gui.utils.PlayerHeads
import yv.tils.utils.logger.Logger
import yv.tils.waypoint.Visibility
import yv.tils.waypoint.Waypoint
import yv.tils.waypoint.config.ManageGUI
import yv.tils.waypoint.config.WaypointStore
import yv.tils.waypoint.language.WaypointText
import yv.tils.waypoint.logic.WaypointNavigation
import yv.tils.waypoint.permissions.Permissions
import java.util.*

class WaypointsGui(private val store: WaypointStore, private val navigation: WaypointNavigation) {
    private fun allowed(player: Player, admin: Boolean = false) = player.hasPermission(Permissions.USE.permission.name) &&
            (!admin || player.hasPermission(Permissions.ADMIN.permission.name))

    private fun button(
        material: Material,
        key: String,
        params: Map<String, Any> = emptyMap(),
        action: (Player) -> Unit
    ): Item =
        Item.builder().setItemProvider { viewer ->
            val stack = ItemStack(material)
            stack.editMeta { it.displayName(GuiStyle.title(WaypointText.text(viewer, key, params))) }
            ItemWrapper(stack)
        }.addClickHandler { _, event ->
            try {
                action(event.player())
            } catch (error: Exception) {
                Logger.error("Waypoint management failed", error)
                WaypointText.send(event.player(), "failure")
            }
        }.build()

    fun open(player: Player, admin: Boolean = false) {
        if (!allowed(player, admin)) {
            WaypointText.send(player, "denied"); return
        }
        InvUIBootstrap.ensure()
        val points = if (admin) store.state.waypoints else store.listed(player)
        val content = points.map { point ->
            Item.builder().setItemProvider {
                val stack = ItemStack(Material.FILLED_MAP)
                stack.editMeta {
                    it.displayName(GuiStyle.title(Component.text(point.name)))
                    it.lore(
                        listOf(
                            GuiStyle.lore(
                                WaypointText.text(
                                    player,
                                    "visibility",
                                    mapOf("value" to point.visibility.name)
                                )
                            ),
                            Component.text("${point.x.toInt()}, ${point.y.toInt()}, ${point.z.toInt()}")
                        )
                    )
                }
                ItemWrapper(stack)
            }.addClickHandler { _, event -> details(event.player(), point, admin) }.build()
        }
        val gui = PagedGui.itemsBuilder().setStructure(
            "# # # # # # # # #", "# x x x x x x x #", "# x x x x x x x #", "# g < c s > a f #"
        )
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('g', button(Material.SPYGLASS, "lookup") { actor -> lookup(actor, admin) })
            .addIngredient(
                'c',
                button(Material.WRITABLE_BOOK, "create") { actor -> if (allowed(actor)) create(actor, admin) })
            .addIngredient(
                's',
                button(Material.COMPASS, "stopped") { actor -> if (allowed(actor)) navigation.stop(actor) })
            .addIngredient(
                'a',
                if (player.hasPermission(Permissions.ADMIN.permission.name)) button(
                    Material.ENDER_EYE,
                    if (admin) "personal" else "admin"
                ) {
                    open(
                        it,
                        !admin
                    )
                } else Filler.item())
            .addIngredient(
                'f',
                if (player.hasPermission(Permissions.CONFIG.permission.name)) button(
                    Material.COMPARATOR,
                    "config"
                ) { ManageGUI.open(it) } else Filler.item())
            .addIngredient('<', BoundItem.pagedBuilder().setItemProvider { viewer, _ ->
                ItemWrapper(ItemStack(Material.ARROW).apply {
                    editMeta {
                        it.displayName(
                            WaypointText.text(
                                viewer,
                                "previous"
                            )
                        )
                    }
                })
            }.addClickHandler { _, paged, event -> if (allowed(event.player(), admin) && paged.page > 0) paged.page-- })
            .addIngredient('>', BoundItem.pagedBuilder().setItemProvider { viewer, _ ->
                ItemWrapper(ItemStack(Material.ARROW).apply {
                    editMeta {
                        it.displayName(
                            WaypointText.text(
                                viewer,
                                "next"
                            )
                        )
                    }
                })
            }.addClickHandler { _, paged, event ->
                if (allowed(
                        event.player(),
                        admin
                    ) && paged.page < paged.pageCount - 1
                ) paged.page++
            })
            .setContent(content).build()
        Window.builder().setTitle(WaypointText.text(player, if (admin) "admin" else "title")).setUpperGui(gui)
            .open(player)
    }

    private fun current(player: Player, point: Waypoint, admin: Boolean, edit: Boolean = false): Boolean {
        if (!allowed(player, admin) || point !in store.state.waypoints ||
            (!admin && store.find(player, point.name) != point) ||
            (edit && point.owner != player.uniqueId && !player.hasPermission(Permissions.ADMIN.permission.name))
        ) {
            WaypointText.send(player, "denied")
            return false
        }
        return true
    }

    fun details(player: Player, point: Waypoint, admin: Boolean = false) {
        if (!current(player, point, admin)) return
        val canEdit = point.owner == player.uniqueId || player.hasPermission(Permissions.ADMIN.permission.name)
        val gui = Gui.builder().setStructure("# n h v r m # d #", "b # # # # # # # #")
            .addIngredient('#', Filler.item())
            .addIngredient('b', button(Material.ARROW, "back") { open(it, admin) })
            .addIngredient(
                'h',
                if (canEdit) button(Material.PLAYER_HEAD, "sharing") { sharing(it, point, admin) } else Filler.item())
            .addIngredient(
                'r',
                if (canEdit) button(Material.NAME_TAG, "rename") { rename(it, point, admin) } else Filler.item())
            .addIngredient('n', button(Material.COMPASS, "navigate") {
                if (current(it, point, admin)) {
                    if (it.world.uid != point.world) WaypointText.send(it, "missing")
                    else {
                        navigation.toggle(it, point); it.closeInventory()
                    }
                }
            })
            .addIngredient(
                'v',
                if (canEdit) button(Material.ENDER_EYE, "visibility", mapOf("value" to point.visibility.name)) {
                    if (current(it, point, admin, true)) {
                        val next =
                            point.copy(visibility = Visibility.entries[(point.visibility.ordinal + 1) % Visibility.entries.size])
                        store.save(store.state.waypoints - point + next)
                        navigation.remove(point)
                        details(it, next, admin)
                    }
                } else Filler.item())
            .addIngredient('m', if (canEdit) button(Material.RECOVERY_COMPASS, "move") {
                if (current(it, point, admin, true)) {
                    val location = it.location
                    val next = point.copy(world = it.world.uid, x = location.x, y = location.y, z = location.z)
                    store.save(store.state.waypoints - point + next)
                    navigation.remove(point)
                    details(it, next, admin)
                }
            } else Filler.item())
            .addIngredient(
                'd',
                if (canEdit) button(Material.TNT, "delete") { actor ->
                    confirm(
                        actor,
                        point,
                        admin
                    )
                } else Filler.item()).build()
        Window.builder().setTitle(Component.text(point.name)).setUpperGui(gui).open(player)
    }

    private fun confirm(player: Player, point: Waypoint, admin: Boolean) {
        if (!current(player, point, admin, true)) return
        val gui = Gui.builder().setStructure("# # b # c # # # #").addIngredient('#', Filler.item())
            .addIngredient('b', button(Material.ARROW, "back") { details(it, point, admin) })
            .addIngredient('c', button(Material.LIME_DYE, "confirm") {
                if (current(it, point, admin, true)) {
                    store.save(store.state.waypoints - point)
                    navigation.remove(point)
                    WaypointText.send(it, "deleted")
                    open(it, admin)
                }
            }).build()
        Window.builder().setTitle(WaypointText.text(player, "delete")).setUpperGui(gui).open(player)
    }

    private fun create(player: Player, admin: Boolean) {
        if (!allowed(player, admin) || !player.hasPermission(Permissions.CREATE.permission.name)) {
            WaypointText.send(player, "denied"); return
        }
        var name = ""
        val gui = Gui.builder().setStructure("i b c").addIngredient('i', GuiStyle.inputPaper(""))
            .addIngredient('b', button(Material.BARRIER, "back") { open(it, admin) })
            .addIngredient('c', button(Material.LIME_DYE, "confirm") { actor ->
                if (!allowed(actor, admin) || !actor.hasPermission(Permissions.CREATE.permission.name)) WaypointText.send(
                    actor,
                    "denied"
                )
                else if (store.create(actor, name.trim(), Visibility.PRIVATE)) {
                    WaypointText.send(actor, "saved"); open(actor, admin)
                } else WaypointText.send(actor, "invalid")
            }).build()
        AnvilWindow.builder().setTitle(WaypointText.text(player, "name")).setUpperGui(gui)
            .addRenameHandler { name = it }.open(player)
    }

    private fun input(player: Player, key: String, back: () -> Unit, submit: (Player, String) -> Unit) {
        var pending = ""
        val gui = Gui.builder().setStructure("i b c")
            .addIngredient('i', GuiStyle.inputPaper(""))
            .addIngredient('b', button(Material.BARRIER, "back") { back() })
            .addIngredient('c', button(Material.LIME_DYE, "confirm") { submit(it, pending.trim()) }).build()
        AnvilWindow.builder().setTitle(WaypointText.text(player, key)).setUpperGui(gui)
            .addRenameHandler { pending = it }.open(player)
    }

    private fun lookup(player: Player, admin: Boolean) {
        if (!allowed(player, admin)) return
        input(player, "lookup", { open(player, admin) }) { actor, name ->
            if (!allowed(actor, admin)) {
                WaypointText.send(actor, "denied"); return@input
            }
            val point = store.find(actor, name)
            if (point == null || point.world != actor.world.uid) WaypointText.send(actor, "missing")
            else {
                navigation.toggle(actor, point); actor.closeInventory()
            }
        }
    }

    private fun rename(player: Player, point: Waypoint, admin: Boolean) {
        if (!current(player, point, admin, true)) return
        input(player, "rename", { details(player, point, admin) }) { actor, name ->
            if (!current(actor, point, admin, true)) return@input
            if (!store.available(name)) {
                WaypointText.send(actor, "invalid"); return@input
            }
            val next = point.copy(name = name)
            store.save(store.state.waypoints - point + next)
            navigation.remove(point)
            details(actor, next, admin)
        }
    }

    private fun sharing(player: Player, point: Waypoint, admin: Boolean) {
        if (!current(player, point, admin, true)) return
        val entries = point.sharedWith.map { uuid ->
            val item = Item.builder().setItemProvider { viewer ->
                val stack = PlayerHeads.head(uuid)
                stack.editMeta {
                    it.displayName(
                        GuiStyle.title(
                            WaypointText.text(
                                viewer, "unshare",
                                mapOf("player" to (Bukkit.getOfflinePlayer(uuid).name ?: uuid))
                            )
                        )
                    )
                }
                ItemWrapper(stack)
            }.addClickHandler { _, event ->
                val actor = event.player()
                if (!current(actor, point, admin, true)) return@addClickHandler
                try {
                    val next = point.copy(sharedWith = point.sharedWith - uuid)
                    store.save(store.state.waypoints - point + next)
                    navigation.remove(point)
                    sharing(actor, next, admin)
                } catch (error: Exception) {
                    Logger.error("Could not update waypoint sharing", error)
                    WaypointText.send(actor, "failure")
                }
            }.build()
            PlayerHeads.load(uuid, item)
            item
        }
        val addPlayer = button(Material.LIME_DYE, "share") { actor ->
            if (!current(actor, point, admin, true)) return@button
            input(actor, "share", { sharing(actor, point, admin) }) { viewer, name ->
                if (!current(viewer, point, admin, true)) return@input
                val uuid = runCatching { UUID.fromString(name) }.getOrNull()
                val target =
                    uuid?.let(Bukkit::getOfflinePlayer) ?: Bukkit.getOfflinePlayerIfCached(name)
                if (target == null || !target.hasPlayedBefore() && !target.isOnline) {
                    WaypointText.send(viewer, "unknownPlayer"); return@input
                }
                val next = point.copy(visibility = Visibility.SHARED, sharedWith = point.sharedWith + target.uniqueId)
                store.save(store.state.waypoints - point + next)
                navigation.remove(point)
                sharing(viewer, next, admin)
            }
        }
        val gui = PagedGui.itemsBuilder().setStructure(
            "# # # # # # # # #", "# x x x x x x x #",
            "# x x x x x x x #", "b # # < # > # # a"
        )
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', button(Material.ARROW, "back") { details(it, point, admin) })
            .addIngredient('a', addPlayer)
            .addIngredient('<', BoundItem.pagedBuilder().setItemProvider { viewer, _ ->
                GuiStyle.field(Material.ARROW, WaypointText.text(viewer, "previous"), emptyList())
            }.addClickHandler { _, paged, event ->
                if (current(
                        event.player(),
                        point,
                        admin,
                        true
                    ) && paged.page > 0
                ) paged.page--
            })
            .addIngredient('>', BoundItem.pagedBuilder().setItemProvider { viewer, _ ->
                GuiStyle.field(Material.ARROW, WaypointText.text(viewer, "next"), emptyList())
            }.addClickHandler { _, paged, event ->
                if (current(
                        event.player(),
                        point,
                        admin,
                        true
                    ) && paged.page < paged.pageCount - 1
                ) paged.page++
            })
            .setContent(entries).build()
        Window.builder().setTitle(GuiStyle.title(WaypointText.text(player, "sharing"))).setUpperGui(gui).open(player)
    }
}
