package yv.tils.essentials

import data.Data
import dev.jorel.commandapi.CommandAPI
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import yv.tils.essentials.commands.register.*
import yv.tils.essentials.language.RegisterStrings
import yv.tils.essentials.listeners.*

class EssentialYVtils {
    companion object {
        const val MODULENAME = "essentials"
        const val MODULEVERSION = "1.0.0"
    }

    init {
        RegisterStrings().registerStrings()
    }

    fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        unregisterCommands()

        registerCommands()
        registerListeners()
        registerPermissions()
    }

    fun disablePlugin() {

    }

    private fun registerCommands() {
        FlyCMD()
        GamemodeCMD()
        GlobalMuteCMD()
        GodCMD()
        HealCMD()
        SeedCMD()
        SpeedCMD()
        PingCMD()
    }

    private fun unregisterCommands() {
        CommandAPI.unregister("gamemode")
        CommandAPI.unregister("seed")
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(AsyncChat(), plugin)
        pm.registerEvents(EntityDamage(), plugin)
        pm.registerEvents(PlayerChangedWorld(), plugin)
        pm.registerEvents(PlayerGameModeChange(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
    }

    private fun registerPermissions() {
        val pm = Data.instance.server.pluginManager
        pm.addPermission(Permission.loadPermission("yvtils.bypass.globalmute", mapOf(
            "description" to "Bypass the global mute",
            "default" to PermissionDefault.OP
        )))
    }
}