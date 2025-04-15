package yv.tils.core

import ConfigYVtils
import UtilsYVtils
import data.Data
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import logger.Logger
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.claim.ClaimYVtils
import yv.tils.common.CommonYVtils

class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils

        const val PLUGIN_NAME = "YVtils-Claim"
        const val PLUGIN_COLOR = "#4CAF50"
    }

    private val modules: List<Data.YVtilsModule> = listOf(
        ConfigYVtils(),
        UtilsYVtils(),
        ClaimYVtils(),
        CommonYVtils()
    )

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is loading...")

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
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is starting...")

        try {
            modules.forEach { it.enablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils startup: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is stopping...")

        try {
            modules.forEach { it.disablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils shutdown: ${e.message}")
            e.printStackTrace()
        }
    }
}