package yv.tils.core

import ConfigYVtils
import UtilsYVtils
import data.Data
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import logger.Logger
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.common.CommonYVtils
import yv.tils.essentials.EssentialYVtils
import yv.tils.message.MessageYVtils
import yv.tils.multiMine.MultiMineYVtils
import yv.tils.server.ServerYVtils
import yv.tils.sit.SitYVtils
import yv.tils.status.StatusYVtils

class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils
    }

    private val modules: List<Data.YVtilsModule> = listOf(
        ConfigYVtils(),
        UtilsYVtils(),
        EssentialYVtils(),
        SitYVtils(),
        MessageYVtils(),
        MultiMineYVtils(),
        StatusYVtils(),
        ServerYVtils(),
        CommonYVtils()
    )

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("YVtils Collection v$yvtilsVersion is loading...")

        Data.yvtilsVersion = yvtilsVersion
        Data.instance = instance
        Data.key = NamespacedKey(this, "yvtils")

        CommandAPI.onLoad(CommandAPIBukkitConfig(instance).silentLogs(true).verboseOutput(false).setNamespace("yvtils"))

        try {
            modules.forEach { it.onLoad() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils loading: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onEnable() {
        Logger.debug("YVtils Collection v$yvtilsVersion is starting...")

        try {
            modules.forEach { it.enablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils startup: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        Logger.debug("YVtils Collection v$yvtilsVersion is stopping...")

        try {
            modules.forEach { it.disablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils shutdown: ${e.message}")
            e.printStackTrace()
        }
    }
}