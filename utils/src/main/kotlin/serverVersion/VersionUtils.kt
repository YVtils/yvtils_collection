package serverVersion

import data.Data
import logger.Logger

class VersionUtils {
    companion object {
        var serverVersion = "x.x.x"
    }

    fun loadServerVersion() {
        Logger.debug("Server is running on: ${Data.instance.server.minecraftVersion}")
        serverVersion = Data.instance.server.minecraftVersion
    }
}