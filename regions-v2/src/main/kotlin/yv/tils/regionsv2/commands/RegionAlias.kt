package yv.tils.regionsv2.commands

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import yv.tils.utils.modules.Core

/** Route the shared shorthand without replacing WorldGuard's namespaced command. */
object RegionAlias : Listener {
    private var previous: Command? = null
    private var installed: Command? = null
    private var worldguardLabel = "worldguard:region"

    internal fun hasWorldGuardRights(granted: (String) -> Boolean, effective: Set<String>): Boolean =
        granted("worldguard.*") || granted("worldguard.region.*") ||
                effective.any {
                    it.startsWith("worldguard.region.") && !it.startsWith("worldguard.region.bypass.") && granted(it)
                }

    fun usesWorldGuard(sender: CommandSender): Boolean = sender !is Player || hasWorldGuardRights(
        sender::hasPermission, sender.effectivePermissions.filter { it.value }.map { it.permission }.toSet()
    )

    fun register() {
        val map = Bukkit.getCommandMap()
        previous = map.getCommand("rg")
        worldguardLabel = if (map.getCommand("worldguard:region") != null) "worldguard:region" else "worldguard:rg"
        val worldguard =
            requireNotNull(map.getCommand(worldguardLabel)) { "WorldGuard's region command is unavailable" }
        val regions = requireNotNull(map.getCommand("regionsv2"))
        val router = object : Command("rg") {
            private fun target(sender: CommandSender) = if (usesWorldGuard(sender)) worldguard else regions
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean =
                target(sender).execute(sender, if (usesWorldGuard(sender)) "region" else "regionsv2", args)

            override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> =
                tabComplete(sender, alias, args, (sender as? Player)?.location)

            override fun tabComplete(
                sender: CommandSender,
                alias: String,
                args: Array<out String>,
                location: Location?
            ): List<String> =
                target(sender).tabComplete(
                    sender,
                    if (usesWorldGuard(sender)) "region" else "regionsv2",
                    args,
                    location
                )
        }
        map.register("yvtils", router)
        map.knownCommands["rg"] = router
        installed = router
        Bukkit.getPluginManager().registerEvents(this, Core.instance)
    }

    /** Preserve raw quoting and route before Brigadier parses any colliding plugin alias. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val label = event.message.substringBefore(' ')
        if (!label.equals("/rg", true)) return
        val target = if (usesWorldGuard(event.player)) worldguardLabel else "regionsv2"
        event.message = "/$target" + event.message.substring(label.length)
    }

    fun shutdown() {
        val map = Bukkit.getCommandMap()
        if (map.getCommand("rg") === installed) {
            map.knownCommands.remove("rg")
            previous?.let { map.knownCommands["rg"] = it }
        }
        installed?.unregister(map)
        if (map.getCommand("yvtils:rg") === installed) map.knownCommands.remove("yvtils:rg")
        installed = null
        previous = null
        org.bukkit.event.HandlerList.unregisterAll(this)
    }
}
