package yv.tils.regions

import yv.tils.regions.commands.RegionCommand
import yv.tils.regions.configs.*
import yv.tils.regions.data.FlagManager
import yv.tils.regions.language.RegisterStrings
import yv.tils.regions.listeners.*
import yv.tils.regions.listeners.cause.*
import yv.tils.utils.data.Data

class RegionsYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "regions",
            "1.0.0-beta.2",
            "Regions module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/regions/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        PlayerSaveFile().registerStrings()
        RegionSaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        unregisterCommands()

        registerCommands()
        registerListeners()

        loadConfigs()

        FlagManager().initFlagList()
    }

    override fun onLateEnablePlugin() {

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
