package yv.tils.utils.server

import yv.tils.utils.data.Data
import yv.tils.utils.message.MessageUtils
import net.kyori.adventure.text.Component

class ServerUtils {
    companion object {
        var serverInMaintenance: Boolean = false
        var serverIP: String = ""
        var serverPort: Int = -1

        val difficulty: String
            get() = Data.Companion.instance.server.worlds[0].difficulty.name

        val isWhitelistActive: Boolean
            get() {
                return Data.Companion.instance.server.hasWhitelist()
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
                return Data.Companion.instance.server.motd()
            }

        val motdAsString: String
            get() {
                return MessageUtils.Companion.convert(motd)
            }
    }
}
