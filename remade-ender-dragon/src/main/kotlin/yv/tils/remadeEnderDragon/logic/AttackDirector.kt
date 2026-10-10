/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import yv.tils.remadeEnderDragon.configs.Attack

/** Pure encounter policy. Timers and world suitability remain owned by the fight. */
object AttackDirector {
    val support = setOf(Attack.SANCTUARY, Attack.RESCUE_UPDRAFT, Attack.HEALING_POOL)
    val basics = setOf(Attack.ISLAND_WAVE, Attack.DRAGON_WAVE, Attack.EFFECT_AREAS, Attack.EXPLOSIVES)
    private val opening = basics + setOf(Attack.MONSTERS, Attack.BLUE_ENDERMEN)
    private val movement =
        setOf(Attack.ISLAND_WAVE, Attack.DRAGON_WAVE, Attack.MAGNETISM, Attack.INFESTATION, Attack.BREATH_SWEEP)

    fun phase(previous: Int, health: Double, middle: Double, finale: Double): Int =
        maxOf(previous, if (health <= finale) 2 else if (health <= middle) 1 else 0)

    fun budgetAvailable(uses: Int, maximum: Int, sustain: Boolean): Boolean =
        maximum != 0 && (maximum < 0 || uses < maximum || sustain)

    fun phaseAllows(attack: Attack, phase: Int): Boolean = attack in support || attack in opening || phase >= 1

    fun compatible(attack: Attack, active: Set<Attack>): Boolean {
        val hostile = active - support - Attack.RIFT_ANCHORS
        if (attack in active) return false
        if (attack in support || attack == Attack.RIFT_ANCHORS) return true
        // A moving cast must never trap players in another cast or in a slowing cloud.
        if (attack in movement || hostile.any { it in movement }) return hostile.isEmpty()
        return hostile.isEmpty() || hostile.all { it == Attack.MONSTERS || it == Attack.BLUE_ENDERMEN }
    }

    /** Prefer a follow-up wave, then variety, then the least recently used cast. */
    fun choose(eligible: List<Attack>, last: Attack?, lastUse: Map<Attack, Long>): Attack? {
        if (last == Attack.EFFECT_AREAS && Attack.ISLAND_WAVE in eligible) return Attack.ISLAND_WAVE
        return eligible.filter { it != last }.ifEmpty { eligible }
            .minByOrNull { lastUse[it] ?: Long.MIN_VALUE }
    }
}
