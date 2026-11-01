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
import yv.tils.utils.player.TemporaryFlight
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
    private var participants = mutableListOf<Player>()
    var gameplayReleased = false
        private set

    fun prepare(players: List<Player>): List<Player> {
        participants = players.toMutableList()
        gameplayReleased = false
        capture(participants)
        participants.forEach { preparePlayer(it) }
        return participants
    }

    private fun preparePlayer(player: Player) {
        player.stopAllSounds()
        player.gameMode = GameMode.SPECTATOR
        participants.forEach { other ->
            if (other != player) {
                player.hidePlayer(Core.instance, other)
                other.hidePlayer(Core.instance, player)
            }
        }
    }

    fun addPlayer(player: Player): Boolean {
        if (gameplayReleased || participants.any { it.uniqueId == player.uniqueId }) return false
        // Record visibility before hiding the late joiner from the existing participants.
        participants.forEach { other ->
            val state = snapshots[other.uniqueId]
            if (state != null && other.canSee(player)) {
                snapshots[other.uniqueId] = state.copy(visible = state.visible + player.uniqueId)
            }
        }
        capture(listOf(player), participants + player)
        val reference = participants.firstOrNull()
        participants.add(player)
        preparePlayer(player)
        if (reference != null) {
            player.teleport(reference.location)
            if (PhaseHandler.currentPhase.ordinal >= Phase.DEATH_VISUAL.ordinal) {
                player.scoreboard = reference.scoreboard
                DeathCounter.configure(player.scoreboard, participants)
            }
        }
        CinematicCamera.join(player)
        return true
    }

    fun capture(players: List<Player>, visiblePlayers: List<Player> = players) {
        players.forEach {
            if (it.uniqueId in snapshots) return@forEach
            TemporaryFlight.release.accept(it)
            snapshots[it.uniqueId] = Snapshot(
                it.location.clone(), it.gameMode, it.activePotionEffects.toList(),
                it.scoreboard, it.allowFlight, it.isFlying,
                visiblePlayers.filter(it::canSee).map { other -> other.uniqueId }.toSet(),
            )
        }
    }

    fun restore(player: Player, returnToOrigin: Boolean) {
        val state = snapshots[player.uniqueId] ?: return
        if (player.gameMode == GameMode.SPECTATOR) player.spectatorTarget = null
        player.gameMode = state.mode
        restoreFlight(player, state.flight, state.flying)
        player.clearActivePotionEffects()
        player.addPotionEffects(state.effects)
        player.scoreboard = state.scoreboard
        if (returnToOrigin) player.teleport(state.location)
        player.clearTitle()
        player.stopAllSounds()
        state.visible.forEach { uuid -> Bukkit.getPlayer(uuid)?.let { player.showPlayer(Core.instance, it) } }
        snapshots.remove(player.uniqueId)
    }

    internal fun restoreFlight(player: Player, allowFlight: Boolean, flying: Boolean) {
        player.isFlying = false
        player.allowFlight = allowFlight
        player.isFlying = allowFlight && flying
    }

    fun releasePlayers() {
        CinematicCamera.stopAll()
        participants.forEach { restore(it, returnToOrigin = false) }
        gameplayReleased = true
        DeathCounter.enable()
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
        participants.clear()
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
