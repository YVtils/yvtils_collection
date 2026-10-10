package yv.tils.remadeEnderDragon.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import yv.tils.remadeEnderDragon.configs.Attack

class AttackDirectorTest {
    @Test
    fun `healing and player departures cannot reverse encounter progression`() {
        assertEquals(0, AttackDirector.phase(0, 0.71, 0.70, 0.35))
        assertEquals(1, AttackDirector.phase(0, 0.70, 0.70, 0.35))
        assertEquals(2, AttackDirector.phase(0, 0.35, 0.70, 0.35))
        assertEquals(2, AttackDirector.phase(2, 1.0, 0.70, 0.35))
        assertEquals(1, AttackDirector.phase(1, 0.95, 0.70, 0.35))
    }

    @Test
    fun `exhausted basics sustain even in a long opening but disabled and major casts do not`() {
        assertTrue(AttackDirector.budgetAvailable(100, 4, true))
        assertFalse(AttackDirector.budgetAvailable(100, 4, false))
        assertFalse(AttackDirector.budgetAvailable(0, 0, true))
        assertTrue(AttackDirector.budgetAvailable(100, -1, false))
        assertTrue(AttackDirector.budgetAvailable(3, 4, false))
    }

    @Test
    fun `support never blocks offense but forced movement and slowing hazards cannot overlap`() {
        assertTrue(AttackDirector.compatible(Attack.ISLAND_WAVE, setOf(Attack.HEALING_POOL, Attack.SANCTUARY)))
        assertFalse(AttackDirector.compatible(Attack.ISLAND_WAVE, setOf(Attack.EFFECT_AREAS)))
        assertFalse(AttackDirector.compatible(Attack.MAGNETISM, setOf(Attack.INFESTATION)))
        assertFalse(AttackDirector.compatible(Attack.EXPLOSIVES, setOf(Attack.MAGNETISM)))
        assertFalse(AttackDirector.compatible(Attack.BREATH_SWEEP, setOf(Attack.MARKED_HUNTERS)))
        assertFalse(AttackDirector.compatible(Attack.ISLAND_WAVE, setOf(Attack.ISLAND_WAVE)))
        assertTrue(AttackDirector.compatible(Attack.EFFECT_AREAS, setOf(Attack.RIFT_ANCHORS)))
    }

    @Test
    fun `advanced attacks are introduced after opening and selection produces a safe followup`() {
        assertFalse(AttackDirector.phaseAllows(Attack.BREATH_SWEEP, 0))
        assertFalse(AttackDirector.phaseAllows(Attack.MARKED_HUNTERS, 0))
        assertTrue(AttackDirector.phaseAllows(Attack.BREATH_SWEEP, 1))
        assertTrue(AttackDirector.phaseAllows(Attack.SANCTUARY, 0))
        assertEquals(
            Attack.ISLAND_WAVE,
            AttackDirector.choose(listOf(Attack.ISLAND_WAVE, Attack.EXPLOSIVES), Attack.EFFECT_AREAS, emptyMap())
        )
        assertEquals(
            Attack.EXPLOSIVES,
            AttackDirector.choose(listOf(Attack.MONSTERS, Attack.EXPLOSIVES), Attack.MONSTERS, emptyMap())
        )
        assertEquals(
            Attack.MONSTERS, AttackDirector.choose(
                listOf(Attack.MONSTERS, Attack.EXPLOSIVES), null,
                mapOf(Attack.MONSTERS to 10L, Attack.EXPLOSIVES to 20L)
            )
        )
        assertEquals(null, AttackDirector.choose(emptyList(), null, emptyMap()))
    }
}
