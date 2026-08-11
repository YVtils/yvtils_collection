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

package yv.tils.yv_smp.logic.start

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import yv.tils.utils.modules.Core

class PhaseHandler {
    companion object {
        var currentPhase = Phase.NONE

        /**
         * Whether a start sequence is currently in progress. Used to guard against
         * overlapping `/yvsmp start` invocations, which would otherwise interleave
         * scheduled tasks from two independent phase runs.
         */
        @Volatile
        var isRunning: Boolean = false
            private set

        /**
         * Incremented every time [startPhases] is called. Scheduled phase callbacks
         * capture the generation they belong to, so a stale run (e.g. one cancelled
         * via [stopPhases]) can no longer advance the phase machine even if one of
         * its `runTaskLater` callbacks was already queued.
         */
        private var generation: Int = 0

        /**
         * The current generation number. Capture this at the time a phase is
         * scheduled (see [PhasePlugin.onPhaseEnd]) and pass it back into
         * [triggerNextPhase] so stale, already-queued callbacks from a stopped
         * run can be detected and ignored.
         */
        val currentGeneration: Int
            get() = generation

        val actions = mutableMapOf<Phase, List<(List<Player>) -> Unit>>()

        fun triggerNextPhase(players: List<Player>) {
            triggerNextPhase(players, generation)
        }

        fun triggerNextPhase(players: List<Player>, forGeneration: Int) {
            if (!isRunning || forGeneration != generation) return

            currentPhase = when (currentPhase) {
                Phase.NONE -> Phase.PREPARATION
                Phase.PREPARATION -> Phase.HEAL
                Phase.HEAL -> Phase.ITEM_CLEAR
                Phase.ITEM_CLEAR -> Phase.DEATH_VISUAL
                Phase.DEATH_VISUAL -> Phase.BORDER
                Phase.BORDER -> Phase.FINAL
                Phase.FINAL -> Phase.FINAL_TWO
                Phase.FINAL_TWO -> Phase.NONE
            }

            if (currentPhase == Phase.NONE) {
                isRunning = false
                return
            }

            actions[currentPhase]?.forEach { action ->
                action(players)
            }
        }

        /**
         * Starts a fresh phase sequence. Returns `false` (and does nothing) if a
         * sequence is already running - callers should check [isRunning] beforehand
         * to give the user proper feedback, this is just a safety net.
         */
        fun startPhases(players: List<Player>): Boolean {
            val gen = reserveRun() ?: return false
            return beginPhasesForReservedRun(players, gen)
        }

        /**
         * Reserves a run slot without starting any phase yet. Used by [yv.tils.yv_smp.logic.StartLogic]
         * to mark a sequence as "in progress" for the whole duration of the pre-start
         * warmup countdown, not just the cinematic phases themselves - this way
         * `/yvsmp start` is rejected (and `/yvsmp stop` works) even while the
         * countdown is still ticking down and no player has been touched yet.
         *
         * @return The reserved generation number, or `null` if a run is already in progress.
         */
        fun reserveRun(): Int? {
            if (isRunning) return null
            isRunning = true
            generation++
            return generation
        }

        /**
         * Whether [reservedGeneration] (as returned by [reserveRun]) is still the
         * active, non-cancelled run. Warmup countdown steps should check this
         * before each tick so a `/yvsmp stop` issued mid-countdown takes effect
         * immediately instead of the sequence starting anyway once the countdown
         * timer elapses.
         */
        fun isReservationValid(reservedGeneration: Int): Boolean = isRunning && reservedGeneration == generation

        /**
         * Begins the actual cinematic phase machine for a previously-[reserveRun]'d
         * generation. No-ops (returning `false`) if that reservation is no longer
         * valid, e.g. because [stopPhases] was called during the warmup countdown.
         */
        fun beginPhasesForReservedRun(players: List<Player>, reservedGeneration: Int): Boolean {
            if (!isReservationValid(reservedGeneration)) return false

            resetPhases()
            clearPhases()
            registerActions()
            triggerNextPhase(players, reservedGeneration)
            return true
        }

        /**
         * Forcibly aborts the currently running sequence. Already-scheduled
         * `runTaskLater` callbacks will still fire but will no-op because their
         * captured generation no longer matches [generation].
         */
        fun stopPhases() {
            isRunning = false
            generation++
            currentPhase = Phase.NONE
        }

        fun resetPhases() {
            currentPhase = Phase.NONE
        }

        fun clearPhases() {
            actions.clear()
        }

        fun registerActions() {
            actions[Phase.PREPARATION] = listOf { players -> PreparationPhase().onPhaseStart(players) }
            actions[Phase.HEAL] = listOf { players -> HealPhase().onPhaseStart(players) }
            actions[Phase.ITEM_CLEAR] = listOf { players -> ItemClearPhase().onPhaseStart(players) }
            actions[Phase.DEATH_VISUAL] = listOf { players -> DeathVisualizerPhase().onPhaseStart(players) }
            actions[Phase.BORDER] = listOf { players -> BorderPhase().onPhaseStart(players) }
            actions[Phase.FINAL] = listOf { players -> FinalPhase().onPhaseStart(players) }
            actions[Phase.FINAL_TWO] = listOf { players -> FinalPhasePartTwo().onPhaseStart(players) }
        }
    }
}

enum class Phase {
    PREPARATION,
    HEAL,
    ITEM_CLEAR,
    DEATH_VISUAL,
    BORDER,
    FINAL,
    FINAL_TWO,
    NONE
}

interface PhasePlugin {
    /**
     * The duration of this phase in ticks. After this duration, the next phase will start.
     * Default is 100 ticks (5 seconds).
     */
    fun getPhaseDuration(): Long {
        return 100L
    }

    /**
     * Called when a phase starts. Implement this method to perform actions specific to the phase.
     * @param players The list of players currently in the game.
     */
    fun onPhaseStart(players: List<Player>) {
        cinematicEffects(players)
        phaseActions(players)
        titleEffects(players)
        onPhaseEnd(players)
    }

    /**
     * Called when a phase ends. Implement this method to perform cleanup or transition actions.
     * This schedules the next phase to start after the phase duration.
     * @param players The list of players currently in the game.
     */
    fun onPhaseEnd(players: List<Player>) {
        val expectedGeneration = PhaseHandler.currentGeneration
        Bukkit.getScheduler().runTaskLater(Core.instance, Runnable {
            PhaseHandler.triggerNextPhase(players, expectedGeneration)
        }, getPhaseDuration())
    }

    /**
     * Called to apply cinematic effects during the phase. Implement this method to add visual or audio effects.
     * @param players The list of players currently in the game.
     */
    fun cinematicEffects(players: List<Player>)

    /**
     * Called to apply title effects during the phase. Implement this method to display titles or action bars to players.
     * @param players The list of players currently in the game.
     */
    fun titleEffects(players: List<Player>)

    /**
     * Called to perform the main actions of the phase. Implement this method to define the core mechanics or events that occur during the phase.
     * @param players The list of players currently in the game.
     */
    fun phaseActions(players: List<Player>)
}