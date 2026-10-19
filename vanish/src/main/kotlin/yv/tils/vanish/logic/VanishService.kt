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

package yv.tils.vanish.logic

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import yv.tils.utils.modules.ConnectionMessages
import yv.tils.utils.modules.Core
import yv.tils.vanish.VanishPolicy
import yv.tils.vanish.configs.ConfigFile
import yv.tils.vanish.configs.PlayerSettings
import yv.tils.vanish.language.VanishText
import yv.tils.vanish.permissions.VanishPermissions
import java.util.*

class VanishService {
    private data class State(val layer: Int, val collidable: Boolean, val fakeLeave: Boolean)

    private val hidden = mutableMapOf<UUID, State>()
    private val pairs = mutableSetOf<Pair<UUID, UUID>>()

    fun hidden(uuid: UUID?) = uuid in hidden
    fun capability(uuid: UUID?, permission: String, enabled: Boolean): Boolean =
        enabled && hidden(uuid) && uuid != null && PlayerSettings.get(uuid)
            .enabled(permission) &&
                Bukkit.getPlayer(uuid)?.hasPermission(permission) == true

    fun toggle(player: Player, layer: Int, silent: Boolean = false) {
        if (!player.hasPermission(VanishPermissions.USE) ||
            (!hidden(player.uniqueId) && (layer !in 1..ConfigFile.state.maxLayer || !player.hasPermission(
                VanishPermissions.layer(layer)
            )))
        ) {
            VanishText.send(player, "denied")
            return
        }
        val previous = hidden.remove(player.uniqueId)
        if (previous == null) {
            hidden[player.uniqueId] = State(
                layer, player.isCollidable,
                !silent && PlayerSettings.get(player.uniqueId).announce
            )
            if (capability(
                    player.uniqueId,
                    VanishPermissions.COLLISION,
                    ConfigFile.state.disableCollision
                )
            ) player.isCollidable = false
            if (capability(player.uniqueId, VanishPermissions.MOB_TARGET, ConfigFile.state.preventMobTarget)) {
                player.world.livingEntities.filterIsInstance<Mob>().filter { it.target == player }
                    .forEach { it.target = null }
            }
        } else player.isCollidable = previous.collidable
        refresh()
        if (!silent && PlayerSettings.get(player.uniqueId).announce) announce(
            player,
            previous != null
        )
        VanishText.send(player, if (previous == null) "enabled" else "disabled", mapOf("layer" to layer))
    }

    fun changeLayer(player: Player, layer: Int) {
        if (!player.hasPermission(VanishPermissions.USE) || !player.hasPermission(VanishPermissions.LAYERS) ||
            layer !in 1..ConfigFile.state.maxLayer || !player.hasPermission(VanishPermissions.layer(layer))
        ) return
        hidden[player.uniqueId]?.let { hidden[player.uniqueId] = it.copy(layer = layer) }
        refresh()
    }

    private fun announce(player: Player, joining: Boolean) {
        val custom = ConnectionMessages.announcer
        if (custom != null) custom.accept(player, joining)
        else Bukkit.broadcast(
            Component.translatable(
                if (joining) "multiplayer.player.joined" else "multiplayer.player.left",
                ConnectionMessages.displayName.apply(player)
            ).color(NamedTextColor.YELLOW)
        )
    }

    fun refresh() {
        hidden.toMap().forEach { (uuid, state) ->
            Bukkit.getPlayer(uuid)?.let { player ->
                if (!player.hasPermission(VanishPermissions.USE) || state.layer > ConfigFile.state.maxLayer ||
                    !player.hasPermission(VanishPermissions.layer(state.layer))
                ) {
                    hidden.remove(uuid)
                    player.isCollidable = state.collidable
                    if (state.fakeLeave) announce(player, true)
                } else player.isCollidable = !capability(
                    uuid,
                    VanishPermissions.COLLISION,
                    ConfigFile.state.disableCollision
                ) && state.collidable
            }
        }
        Bukkit.getOnlinePlayers().forEach { viewer ->
            Bukkit.getOnlinePlayers().forEach { target ->
                val pair = viewer.uniqueId to target.uniqueId
                if (viewer == target || VanishPolicy.visible(
                        hidden[viewer.uniqueId]?.layer ?: 0,
                        hidden[target.uniqueId]?.layer ?: 0
                    )
                ) {
                    if (pairs.remove(pair)) viewer.showPlayer(Core.instance, target)
                } else {
                    pairs.add(pair)
                    viewer.hidePlayer(Core.instance, target)
                }
            }
        }
    }

    fun quit(player: Player) {
        hidden.remove(player.uniqueId)?.let { player.isCollidable = it.collidable }
        refresh()
        pairs.removeIf { it.first == player.uniqueId || it.second == player.uniqueId }
    }

    fun close() {
        Bukkit.getOnlinePlayers().forEach { player ->
            hidden.remove(player.uniqueId)?.let { player.isCollidable = it.collidable }
        }
        refresh()
        hidden.clear()
        pairs.clear()
    }
}
