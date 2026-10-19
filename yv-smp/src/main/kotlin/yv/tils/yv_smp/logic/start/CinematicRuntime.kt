package yv.tils.yv_smp.logic.start

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Scoreboard
import yv.tils.utils.modules.Core
import yv.tils.yv_smp.utils.CinematicCamera
import java.util.UUID
import yv.tils.utils.logger.Logger
import yv.tils.yv_smp.logic.music.MusicHandler

/** Owns delayed cinematic callbacks and the player state borrowed by a run. */
object CinematicRuntime {
    private data class Snapshot(
        val location: Location, val mode: GameMode, val effects: Collection<PotionEffect>,
        val scoreboard: Scoreboard, val flight: Boolean, val flying: Boolean,
        val visible: Set<UUID>
    )

    private val snapshots = mutableMapOf<UUID, Snapshot>()
    private val tasks = mutableSetOf<Int>()
    private var epoch = 0

    fun capture(players: List<Player>) {
        players.forEach {
            snapshots.putIfAbsent(
                it.uniqueId, Snapshot(
                it.location.clone(), it.gameMode,
                it.activePotionEffects.toList(), it.scoreboard, it.allowFlight, it.isFlying,
                players.filter(it::canSee).map { other -> other.uniqueId }.toSet()
            )
            )
        }
    }

    fun restore(player: Player, returnToOrigin: Boolean) {
        val state = snapshots[player.uniqueId] ?: return
        if (player.gameMode == GameMode.SPECTATOR) player.spectatorTarget = null
        player.gameMode = state.mode
        player.allowFlight = state.flight
        player.isFlying = state.flying
        player.clearActivePotionEffects()
        player.addPotionEffects(state.effects)
        player.scoreboard = state.scoreboard
        if (returnToOrigin) player.teleport(state.location)
        player.clearTitle()
        player.stopAllSounds()
        state.visible.forEach { uuid -> Bukkit.getPlayer(uuid)?.let { player.showPlayer(Core.instance, it) } }
        PreparationPhase.savedGameModes.remove(player.uniqueId)
        snapshots.remove(player.uniqueId)
    }

    fun finish(aborted: Boolean) {
        epoch++
        tasks.toList().forEach(Bukkit.getScheduler()::cancelTask)
        tasks.clear()
        CinematicCamera.stopAll()
        if (aborted) MusicHandler.stopAll()
        snapshots.keys.toList().forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let { player ->
                try {
                    restore(player, aborted)
                } catch (error: Exception) {
                    Logger.error("Could not restore cinematic state for $uuid", error)
                }
            }
        }
    }

    fun getScheduler() = this
    fun getPlayer(uuid: UUID) = Bukkit.getPlayer(uuid)
    fun getScoreboardManager() = Bukkit.getScoreboardManager()
    fun cancelTask(id: Int) {
        Bukkit.getScheduler().cancelTask(id); tasks.remove(id)
    }

    fun runTaskLater(plugin: Plugin, action: Runnable, delay: Long): BukkitTask {
        val expected = epoch
        lateinit var task: BukkitTask
        task = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            tasks.remove(task.taskId)
            if (epoch == expected) execute(action)
        }, delay)
        tasks.add(task.taskId)
        return task
    }

    fun scheduleSyncRepeatingTask(plugin: Plugin, action: Runnable, delay: Long, period: Long): Int {
        val expected = epoch
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (epoch == expected) execute(action)
        }, delay, period)
        tasks.add(task.taskId)
        return task.taskId
    }

    private fun execute(action: Runnable) {
        try {
            action.run()
        } catch (error: Exception) {
            PhaseHandler.stopPhases()
            throw error
        }
    }
}
