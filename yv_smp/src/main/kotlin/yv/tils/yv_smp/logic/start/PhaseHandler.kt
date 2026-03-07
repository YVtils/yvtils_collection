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
import yv.tils.utils.data.Data

class PhaseHandler {
    companion object {
        var currentPhase = Phase.NONE

        val actions = mutableMapOf<Phase, List<(List<Player>) -> Unit>>()

        fun triggerNextPhase(players: List<Player>) {
            currentPhase = when (currentPhase) {
                Phase.NONE        -> Phase.PREPARATION
                Phase.PREPARATION -> Phase.HEAL
                Phase.HEAL        -> Phase.ITEM_CLEAR
                Phase.ITEM_CLEAR  -> Phase.DEATH_VISUAL
                Phase.DEATH_VISUAL -> Phase.BORDER
                Phase.BORDER      -> Phase.FINAL
                Phase.FINAL       -> Phase.FINAL_TWO
                Phase.FINAL_TWO   -> Phase.NONE
            }

            if (currentPhase == Phase.NONE) return

            actions[currentPhase]?.forEach { action ->
                action(players)
            }
        }

        fun startPhases(players: List<Player>) {
            resetPhases()
            clearPhases()
            registerActions()
            triggerNextPhase(players)
        }

        fun resetPhases() {
            currentPhase = Phase.NONE
        }

        fun clearPhases() {
            actions.clear()
        }

        fun registerActions() {
            actions[Phase.PREPARATION]  = listOf { players -> PreparationPhase().onPhaseStart(players) }
            actions[Phase.HEAL]         = listOf { players -> HealPhase().onPhaseStart(players) }
            actions[Phase.ITEM_CLEAR]   = listOf { players -> ItemClearPhase().onPhaseStart(players) }
            actions[Phase.DEATH_VISUAL] = listOf { players -> DeathVisualizerPhase().onPhaseStart(players) }
            actions[Phase.BORDER]       = listOf { players -> BorderPhase().onPhaseStart(players) }
            actions[Phase.FINAL]        = listOf { players -> FinalPhase().onPhaseStart(players) }
            actions[Phase.FINAL_TWO]    = listOf { players -> FinalPhasePartTwo().onPhaseStart(players) }
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
        Bukkit.getScheduler().runTaskLater(Data.instance, Runnable {
            PhaseHandler.triggerNextPhase(players)
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