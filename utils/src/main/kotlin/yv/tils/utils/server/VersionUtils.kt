package yv.tils.utils.server

import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger


class VersionUtils {
    companion object {
        var serverVersion = "x.x.x"
        var isViaVersion = false
    }

    fun loadServerVersion() {
        serverVersion = Data.Companion.instance.server.minecraftVersion
        isViaVersion = Data.Companion.instance.server.pluginManager.getPlugin("ViaVersion") != null

        Logger.Companion.debug("Server is running on version: $serverVersion${if (isViaVersion) " with ViaVersion" else ""}")
    }
}
