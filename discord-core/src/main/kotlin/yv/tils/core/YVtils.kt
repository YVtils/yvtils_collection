package yv.tils.core

import yv.tils.config.ConfigYVtils
import yv.tils.utils.UtilsYVtils
import yv.tils.utils.data.Data
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import yv.tils.utils.logger.Logger
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.common.CommonYVtils
import yv.tils.discord.DiscordYVtils
import yv.tils.migration.MigrationYVtils

class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils

        const val PLUGIN_NAME_FULL = "YVtils-Discord"
        const val PLUGIN_NAME = "Discord"
        const val PLUGIN_NAME_SHORT = "dc"
        const val PLUGIN_COLOR = "#7579ff"
    }

    private val modules: List<Data.YVtilsModule> = listOf(
        ConfigYVtils(),
        UtilsYVtils(),
        MigrationYVtils(), // TODO: Remove with 4.1.0
        DiscordYVtils(),
        CommonYVtils()
    )

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("$PLUGIN_NAME_FULL v$yvtilsVersion is loading...")

        val core = Data.YVtilsCore(
            description = "Discord Minecraft Bridge Plugin",
            url = "https://modrinth.com/plugin/yvtils_dc",

            dependencies = listOf(
                "discord"
            ),

            name = PLUGIN_NAME,
            colorHex = PLUGIN_COLOR,
            pluginShort = PLUGIN_NAME_SHORT,

            version = yvtilsVersion,
            instance = instance,

            key = NamespacedKey(this, "yvtils"),
        )

        Data.initCore(core)

        CommandAPI.onLoad(
            CommandAPIBukkitConfig(instance)
                .setNamespace("yvtils")
                .silentLogs(true)
                .verboseOutput(false)
                .beLenientForMinorVersions(true)
        )

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

        if (instance.isEnabled) {
            onLateEnablePlugin()
        }
    }

    fun onLateEnablePlugin() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is performing late enable...")

        try {
            modules.forEach { it.onLateEnablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils late startup: ${e.message}")
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
