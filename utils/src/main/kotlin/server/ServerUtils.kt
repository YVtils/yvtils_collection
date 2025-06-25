package server

import data.Data

class ServerUtils {
    companion object {
        var serverInMaintenance: Boolean = false
        var serverIP: String = ""
        var serverPort: Int = -1

        val isWhitelistActive: Boolean
            get() {
                return Data.instance.server.hasWhitelist()
            }

        fun setServerMaintenance(status: Boolean) {
            serverInMaintenance = status
        }
    }
}