package serverVersion

import data.Data
import logger.Logger

class VersionUtils {
    companion object {
        var serverVersion = "x.x.x"
        var isViaVersion = false
    }

    fun loadServerVersion() {
        serverVersion = Data.instance.server.minecraftVersion
        isViaVersion = Data.instance.server.pluginManager.getPlugin("ViaVersion") != null

        Logger.debug("Server is running on version: $serverVersion${if (isViaVersion) " with ViaVersion" else ""}")
    }
}