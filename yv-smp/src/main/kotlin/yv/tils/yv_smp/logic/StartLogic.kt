/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.yv_smp.logic

import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.modules.Core
import yv.tils.utils.player.PlayerUtils
import yv.tils.yv_smp.configs.ConfigFile
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.logic.music.MusicAPI
import yv.tils.yv_smp.logic.start.PhaseHandler
import yv.tils.yv_smp.logic.start.PreparationPhase.Companion.savedGameModes
import java.time.Duration

class StartLogic {
    /**
     * Kicks off the SMP start sequence for all currently online players.
     *
     * Guards against:
     * - Starting again while a sequence is already running, including during
     *   the warmup countdown itself (would otherwise interleave two independent
     *   runs and their scheduled tasks).
     * - Starting with fewer players online than [ConfigFile.getMinPlayers].
     *
     * If [ConfigFile.getWarmupSeconds] is greater than 0, all online players are
     * shown a countdown (title + sound) before anything else happens - no one is
     * teleported, hidden, or moved to spectator until the countdown finishes. This
     * gives players a heads-up instead of yanking them into the cinematic instantly.
     * `/yvsmp stop` (or a race with another start attempt) cancels the countdown
     * cleanly if issued before it finishes.
     *
     * @return `true` if the sequence (or its warmup countdown) was started, `false` if rejected.
     */
    fun start(sender: CommandSender): Boolean {
        if (PhaseHandler.isRunning) {
            sender.sendMessage(LanguageHandler.getMessage(LangStrings.START_ALREADY_RUNNING, sender))
            return false
        }

        val players = PlayerUtils.onlinePlayersAsPlayers
        val minPlayers = ConfigFile.getMinPlayers()
        if (players.size < minPlayers) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.START_NOT_ENOUGH_PLAYERS,
                    sender,
                    mapOf("required" to minPlayers.toString(), "online" to players.size.toString())
                )
            )
            return false
        }

        val generation = PhaseHandler.reserveRun()
        if (generation == null) {
            sender.sendMessage(LanguageHandler.getMessage(LangStrings.START_ALREADY_RUNNING, sender))
            return false
        }

        val warmupSeconds = ConfigFile.getWarmupSeconds()

        sender.sendMessage(LanguageHandler.getMessage(LangStrings.START_COMMAND_SUCCESS, sender))

        if (warmupSeconds <= 0) {
            launchSequence(players, generation)
        } else {
            runWarmupCountdown(players, warmupSeconds, generation)
        }

        return true
    }

    /**
     * Aborts a currently running start sequence, whether it is still in its
     * warmup countdown or already running cinematic phases.
     *
     * @return `true` if a running sequence was aborted, `false` if none was running.
     */
    fun stop(sender: CommandSender): Boolean {
        if (!PhaseHandler.isRunning) {
            sender.sendMessage(LanguageHandler.getMessage(LangStrings.START_STOP_NOT_RUNNING, sender))
            return false
        }

        PhaseHandler.stopPhases()
        sender.sendMessage(LanguageHandler.getMessage(LangStrings.START_STOP_SUCCESS, sender))
        return true
    }

    /**
     * Broadcasts a per-second countdown (title + sound) to [players], then hands off
     * to [launchSequence] once it reaches zero - unless [generation] was invalidated
     * (via [PhaseHandler.stopPhases]) at any point during the countdown.
     */
    private fun runWarmupCountdown(players: List<Player>, seconds: Long, generation: Int) {
        for (secondsLeft in seconds downTo 1) {
            val delayTicks = (seconds - secondsLeft) * 20L
            Bukkit.getScheduler().runTaskLater(Core.instance, Runnable {
                if (!PhaseHandler.isReservationValid(generation)) return@Runnable

                players.forEach { player ->
                    val title = Title.title(
                        LanguageHandler.getMessage(
                            LangStrings.START_WARMUP_TITLE,
                            player,
                            mapOf("seconds" to secondsLeft.toString())
                        ),
                        LanguageHandler.getMessage(LangStrings.START_WARMUP_SUBTITLE, player),
                        Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(900), Duration.ofMillis(100))
                    )
                    player.showTitle(title)
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f + (seconds - secondsLeft) * 0.1f)
                }
            }, delayTicks)
        }

        Bukkit.getScheduler().runTaskLater(Core.instance, Runnable {
            if (!PhaseHandler.isReservationValid(generation)) return@Runnable
            launchSequence(players, generation)
        }, seconds * 20L)
    }

    /**
     * Actually moves [players] into spectator mode, hides them from each other,
     * starts the background music, and begins the cinematic phase machine.
     * Called either immediately (warmup disabled) or after [runWarmupCountdown] completes.
     */
    private fun launchSequence(players: List<Player>, generation: Int) {
        players.forEach { player ->
            player.stopAllSounds()
            savedGameModes[player.uniqueId] = player.gameMode
            player.gameMode = GameMode.SPECTATOR
            players.forEach { other -> if (other != player) player.hidePlayer(Core.instance, other) }
        }

        MusicAPI.broadcastUrl(
            "https://soundcloud.com/tunetankcom/victor-wayne-battle-phase-dark-epic-tension-music-copyright-free",
        )

        PhaseHandler.beginPhasesForReservedRun(players, generation)
    }
}
