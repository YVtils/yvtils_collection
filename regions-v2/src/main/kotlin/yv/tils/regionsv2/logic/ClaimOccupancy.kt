package yv.tils.regionsv2.logic

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import net.kyori.adventure.text.Component
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.language.LangStrings
import yv.tils.regionsv2.language.message
import yv.tils.utils.modules.Core
import java.util.UUID

/** Main-thread API. Queries use current locations, not stale movement caches. */
object ClaimOccupancy {
    private val previous = mutableMapOf<UUID, Pair<UUID, String>?>()
    private var task: BukkitTask? = null
    fun claimOf(player: Player): Claim? = ClaimService.at(player.location)
    fun playersIn(uuid: UUID): Set<UUID> =
        Bukkit.getOnlinePlayers().filter { claimOf(it)?.uuid == uuid }.map { it.uniqueId }.toSet()

    fun forget(uuid: UUID) {
        previous.remove(uuid)
    }

    fun start() {
        task?.cancel()
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            if (!ConfigFile.state.enabled) {
                previous.clear(); return@Runnable
            }
            for (player in Bukkit.getOnlinePlayers()) {
                val claim = claimOf(player)
                val current = claim?.let { it.uuid to it.name }
                val old = previous[player.uniqueId]
                if (old?.first != current?.first && ConfigFile.state.enabled && ConfigFile.state.actionBarTransitions) {
                    val parts = mutableListOf<Component>()
                    if (old != null) {
                        val custom =
                            yv.tils.regionsv2.data.ClaimMetadata.state.claims[old.first.toString()]?.goodbye.orEmpty()
                        parts += if (custom.isEmpty()) LangStrings.GOODBYE_BAR.message("region" to old.second)
                            .component(player)
                        else Component.text(custom.replace("{region}", old.second).replace("{player}", player.name))
                    }
                    if (claim != null) parts += claim.metadata.welcome.takeIf { it.isNotEmpty() }
                        ?.let { Component.text(it.replace("{region}", claim.name).replace("{player}", player.name)) }
                        ?: LangStrings.WELCOME_BAR.message("region" to claim.name).component(player)
                    player.sendActionBar(parts.fold(Component.empty() as Component) { result, part ->
                        if (result == Component.empty()) part else result.append(Component.text(" • ")).append(part)
                    })
                }
                previous[player.uniqueId] = current
            }
        }, 1L, 10L)
    }

    fun shutdown() {
        task?.cancel(); task = null; previous.clear()
    }
}
