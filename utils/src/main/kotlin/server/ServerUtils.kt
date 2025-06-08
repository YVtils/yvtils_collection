package server

class ServerUtils {
    companion object {
        var serverInMaintenance: Boolean = false
        var serverIP: String = ""
        var serverPort: Int = -1

        fun setServerMaintenance(status: Boolean) {
            serverInMaintenance = status
        }
    }
}