/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.utils.player

import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import yv.tils.utils.data.Data
import yv.tils.utils.message.MessageUtils
import java.util.*

class PlayerUtils {
    companion object {
        const val PLAYER_HEAD_API = "https://cravatar.eu/helmhead/<uuid>/600"

        fun getSkinHash(player: Player): String {
            val skin = player.playerProfile.textures.skin

            if (skin == null) {
                return "default"
            }

            val hash = skin.path.split("/").last()

            return hash.ifBlank {
                "default"
            }
        }

        val onlinePlayersAsCount: Int
            get() {
                val onlinePlayers = Data.instance.server.onlinePlayers.size

                // TODO: Add vanish logic

                return onlinePlayers
            }

        val onlinePlayersAsNames: List<String>
            get() {
                val players = mutableListOf<String>()

                for (player in Data.instance.server.onlinePlayers) {
                    // TODO: Add vanish logic

                    players.add(player.name)
                }

                return players
            }

        val onlinePlayersAsPlayers: List<Player>
            get() {
                return onlinePlayersAsPlayers()
            }

        val maxOnlinePlayers: Int
            get() {
                return Data.instance.server.maxPlayers
            }

        /**
         * Returns a list of online players with or without the specified permission.
         *
         * @param permission The permission to check for.
         * @param needPerm If true, only players with the permission will be included. If false, only players without the permission will be included.
         * @return A list of online players with or without the specified permission.
         */
        fun onlinePlayersAsPlayers(permission: String = "", needPerm: Boolean = true): List<Player> {
            val players = mutableListOf<Player>()

            for (player in Data.instance.server.onlinePlayers) {
                if (permission.isNotEmpty()) {
                    if (needPerm && player.hasPermission(permission)) {
                        players.add(player)
                    } else if (!needPerm && !player.hasPermission(permission)) {
                        players.add(player)
                    }
                } else {
                    players.add(player)
                }
            }
            return players
        }

        /**
         * Broadcasts a message to all online players with the specified permission.
         * @param message The message to broadcast.
         * @param permission The permission to check for. If empty, the message will be sent to all online players.
         * @param needPerm If true, only players with the permission will receive the message. If false, only players without the permission will receive the message.
         */
        fun broadcast(message: String, permission: String = "", needPerm: Boolean = true) {
            broadcast(MessageUtils.convert(message), permission, needPerm)
        }

        /**
         * Broadcasts a message to all online players with the specified permission.
         * @param message The message to broadcast.
         * @param permission The permission to check for. If empty, the message will be sent to all online players.
         * @param needPerm If true, only players with the permission will receive the message. If false, only players without the permission will receive the message.
         */
        fun broadcast(message: Component, permission: String = "", needPerm: Boolean = true) {
            for (player in onlinePlayersAsPlayers(permission, needPerm)) {
                player.sendMessage(message)
            }
        }

        /**
         * Parses a UUID to a Player object.
         * @param uuid The UUID string to parse.
         * @return OfflinePlayer object.
         */
        fun uuidToPlayer(uuid: UUID): OfflinePlayer {
            return Data.instance.server.getOfflinePlayer(uuid)
        }

        /**
         * Parses a UUID to a name string.
         * @param uuid The UUID string to parse.
         * @return The name string of the player.
         */
        fun uuidToName(uuid: UUID): String? {
            return uuidToPlayer(uuid).name
        }

        /**
         * Parses a name string to a Player object.
         * @param name The name string to parse.
         * @return OfflinePlayer object.
         */
        fun nameToPlayer(name: String): OfflinePlayer {
            return Data.instance.server.getOfflinePlayer(name)
        }

        /**
         * Parses a name string to a UUID object.
         * @param name The name string to parse.
         * @return UUID object.
         */
        fun nameToUUID(name: String): UUID {
            return nameToPlayer(name).uniqueId
        }
    }
}
