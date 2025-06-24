package server

class ServerUtils {
    companion object {
        var serverInMaintenance: Boolean = false
        var serverIP: String = ""
        var serverPort: Int = -1

        var isWhitelistActive = false // TODO: Implement logic to toggle this based on server settings

        fun setServerMaintenance(status: Boolean) {
            serverInMaintenance = status
        }
    }
}