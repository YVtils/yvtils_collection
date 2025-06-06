package player

import data.Data
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

class PlayerUtils {
    companion object {
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