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

package yv.tils.waypoint.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import yv.tils.waypoint.Visibility
import yv.tils.waypoint.Waypoint
import yv.tils.waypoint.config.ManageGUI
import yv.tils.waypoint.config.WaypointStore
import yv.tils.waypoint.gui.WaypointsGui
import yv.tils.waypoint.language.WaypointText
import yv.tils.waypoint.logic.WaypointNavigation
import yv.tils.waypoint.permissions.Permissions

class WaypointCommand(private val store: WaypointStore, private val navigation: WaypointNavigation) {
    init {
        commandTree("waypoint") {
            withAliases("wp")
            withPermission(Permissions.USE.permission.name)
            playerExecutor { player, _ -> WaypointsGui(store, navigation).open(player) }
            literalArgument("manage") {
                withPermission(Permissions.ADMIN.permission.name)
                playerExecutor { player, _ -> WaypointsGui(store, navigation).open(player, true) }
            }
            literalArgument("config") {
                withPermission(Permissions.CONFIG.permission.name)
                playerExecutor { player, _ -> ManageGUI.open(player) }
            }
            literalArgument("list") {
                playerExecutor { player, _ ->
                    WaypointText.send(player, "listTitle")
                    val points = store.listed(player)
                    if (points.isEmpty()) WaypointText.send(player, "listEmpty")
                    points.forEach { point ->
                        val owner = Bukkit.getOfflinePlayer(point.owner).name ?: point.owner.toString()
                        player.sendMessage(
                            WaypointText.text(player, "listEntry", mapOf("waypoint" to point.name, "owner" to owner))
                                .clickEvent(ClickEvent.runCommand("/waypoint view ${point.name}"))
                                .hoverEvent(WaypointText.text(player, "openDetails"))
                        )
                    }
                }
            }
            literalArgument("view") {
                stringArgument("name") {
                    playerExecutor { player, args ->
                        val point = store.find(player, args["name"] as String)
                        if (point == null) WaypointText.send(player, "missing")
                        else WaypointsGui(store, navigation).details(player, point)
                    }
                }
            }
            literalArgument("create") {
                withPermission(Permissions.CREATE.permission.name)
                stringArgument("name") {
                    playerExecutor { player, args -> create(player, args["name"] as String, Visibility.PRIVATE) }
                    stringArgument("visibility") {
                        replaceSuggestions(ArgumentSuggestions.strings("public", "private", "unlisted", "shared"))
                        playerExecutor { player, args ->
                            val visibility =
                                Visibility.entries.firstOrNull { it.name.equals(args["visibility"] as String, true) }
                            if (visibility == null) WaypointText.send(player, "invalid")
                            else create(player, args["name"] as String, visibility)
                        }
                    }
                }
            }
            literalArgument("delete") {
                stringArgument("name") {
                    replaceSuggestions(ArgumentSuggestions.strings { info ->
                        (info.sender as? Player)?.let { store.listed(it).map(Waypoint::name).toTypedArray() }
                            ?: emptyArray()
                    })
                    playerExecutor { player, args ->
                        val point = store.find(player, args["name"] as String)
                        when {
                            point == null -> WaypointText.send(player, "missing")
                            point.owner != player.uniqueId && !player.hasPermission(Permissions.ADMIN.permission.name) -> WaypointText.send(
                                player,
                                "denied"
                            )

                            else -> {
                                store.save(store.state.waypoints - point)
                                navigation.remove(point)
                                WaypointText.send(player, "deleted")
                            }
                        }
                    }
                }
            }
            literalArgument("navigate") {
                stringArgument("name") {
                    replaceSuggestions(ArgumentSuggestions.strings { info ->
                        (info.sender as? Player)?.let { store.listed(it).map(Waypoint::name).toTypedArray() }
                            ?: emptyArray()
                    })
                    playerExecutor { player, args ->
                        val point = store.find(player, args["name"] as String)
                        if (point == null || Bukkit.getWorld(point.world) != player.world) WaypointText.send(
                            player,
                            "missing"
                        )
                        else navigation.toggle(player, point)
                    }
                }
            }
            literalArgument("stop") {
                playerExecutor { player, _ -> navigation.stop(player); WaypointText.send(player, "stopped") }
            }
        }
    }

    private fun create(player: Player, name: String, visibility: Visibility) {
        if (!player.hasPermission(Permissions.CREATE.permission.name) || !store.create(player, name, visibility)) {
            WaypointText.send(player, "invalid")
            return
        }
        WaypointText.send(player, "saved")
    }
}
