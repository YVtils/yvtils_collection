package server

import data.Data
import message.MessageUtils
import net.kyori.adventure.text.Component

class ServerUtils {
    companion object {
        var serverInMaintenance: Boolean = false
        var serverIP: String = ""
        var serverPort: Int = -1

        val difficulty: String
            get() = Data.instance.server.worlds[0].difficulty.name

        val isWhitelistActive: Boolean
            get() {
                return Data.instance.server.hasWhitelist()
            }

        fun setServerMaintenance(status: Boolean) {
            serverInMaintenance = status
        }

        val serverName: String
            get() {
                return "A Minecraft Server" // TODO: Logic for custom server names
            }

        val motd: Component
            get() {
                return Data.instance.server.motd()
            }

        val motdAsString: String
            get() {
                return MessageUtils.convert(motd)
            }
    }
}
