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
import yv.tils.sit.SitYVtils

class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils
    }

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("YVtils Collection v$yvtilsVersion is loading...")

        Data.yvtilsVersion = yvtilsVersion
        Data.instance = instance
        Data.key = NamespacedKey(this, "yvtils")

        CommandAPI.onLoad(CommandAPIBukkitConfig(instance).silentLogs(true).verboseOutput(false).setNamespace("yvtils"))
    }

    override fun onEnable() {
        Logger.debug("YVtils Collection v$yvtilsVersion is starting...")

        ConfigYVtils().enablePlugin()
        UtilsYVtils().enablePlugin()
        EssentialYVtils().enablePlugin()
        SitYVtils().enablePlugin()
        MessageYVtils().enablePlugin()
        MultiMineYVtils().enablePlugin()

        CommonYVtils().enablePlugin() // This should be the last one to load, as it handles the loading of all modules
    }

    override fun onDisable() {
        Logger.debug("YVtils Collection v$yvtilsVersion is stopping...")

        ConfigYVtils().disablePlugin()
        UtilsYVtils().disablePlugin()
        EssentialYVtils().disablePlugin()
        SitYVtils().disablePlugin()
        MessageYVtils().disablePlugin()
        MultiMineYVtils().disablePlugin()

        CommonYVtils().disablePlugin()
    }
}