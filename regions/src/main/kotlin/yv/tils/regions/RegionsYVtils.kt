package yv.tils.regions

import data.Data
import yv.tils.regions.commands.RegionCommand
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.configs.PlayerSaveFile
import yv.tils.regions.configs.RegionSaveFile
import yv.tils.regions.data.FlagManager
import yv.tils.regions.language.RegisterStrings
import yv.tils.regions.listeners.BlockFlagTrigger
import yv.tils.regions.listeners.PlayerEntryRegion
import yv.tils.regions.listeners.PlayerFlagTrigger
import yv.tils.regions.listeners.PlayerLeaveRegion
import yv.tils.regions.listeners.cause.*

class RegionsYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "regions"
        const val MODULE_VERSION = "1.0.0-beta.1"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        PlayerSaveFile().registerStrings()
        RegionSaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        unregisterCommands()

        registerCommands()
        registerListeners()

        loadConfigs()

        FlagManager().initFlagList()
    }

    override fun disablePlugin() {
    }

    private fun registerCommands() {
        RegionCommand()
    }

    private fun unregisterCommands() {

    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerMove(), plugin)
        pm.registerEvents(BlockBreak(), plugin)
        pm.registerEvents(BlockPlace(), plugin)
        pm.registerEvents(PlayerInteract(), plugin)
        pm.registerEvents(EntityDamageByEntity(), plugin)
        pm.registerEvents(InventoryOpen(), plugin)
        pm.registerEvents(PlayerItemFrameChange(), plugin)
        pm.registerEvents(PlayerTeleport(), plugin)

        pm.registerEvents(PlayerEntryRegion(), plugin)
        pm.registerEvents(PlayerLeaveRegion(), plugin)
        pm.registerEvents(BlockFlagTrigger(), plugin)
        pm.registerEvents(PlayerFlagTrigger(), plugin)
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        PlayerSaveFile().loadConfig()
        RegionSaveFile().loadConfig()
    }
}