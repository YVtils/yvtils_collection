/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import org.bukkit.World
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TemporaryKeepInventoryTest {
    @Test
    fun `disabled setting and preview never change the rule or create a recovery entry`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        assertTrue(rules.begin(world, enabled = false, preview = false))
        assertTrue(rules.begin(world, enabled = true, preview = true))
        assertFalse(state.get(world))
        assertTrue(state.saved.originalRules.isEmpty())
        assertEquals(0, state.writes)
        assertEquals(0, state.saves)
        assertTrue(state.activations.isEmpty())
        assertTrue(state.restorations.isEmpty())
    }

    @Test
    fun `overworld and nether cannot acquire temporary keep inventory or announce activation`() {
        val state = Rules()
        val rules = state.controller()
        listOf(World.Environment.NORMAL, World.Environment.NETHER).forEach { environment ->
            listOf(false, true).forEach { original ->
                val world = state.world(original, environment)
                assertTrue(rules.begin(world, enabled = true, preview = false))
                assertEquals(original, state.get(world))
                assertFalse(rules.isActive(world))
            }
        }
        assertEquals(0, state.writes)
        assertEquals(0, state.saves)
        assertTrue(state.saved.originalRules.isEmpty())
        assertTrue(state.activations.isEmpty())
        assertTrue(state.restorations.isEmpty())
    }

    @Test
    fun `only the encounter End world gets an override and announcements`() {
        val state = Rules()
        val dragonEnd = state.world(false)
        val otherEnd = state.world(false)
        val overworld = state.world(false, World.Environment.NORMAL)
        val nether = state.world(false, World.Environment.NETHER)
        val rules = state.controller()
        assertTrue(rules.begin(dragonEnd, enabled = true, preview = false))
        assertTrue(rules.isActive(dragonEnd))
        listOf(otherEnd, overworld, nether).forEach { world ->
            assertFalse(rules.isActive(world))
            assertFalse(state.get(world))
        }
        assertEquals(listOf(dragonEnd.uid), state.activations)
        assertTrue(rules.restore(dragonEnd))
        assertEquals(listOf(dragonEnd.uid to false), state.restorations)
    }

    @Test
    fun `activation and restoration announcements occur once per successful encounter lease`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        assertFalse(rules.isActive(world))
        assertTrue(rules.begin(world, enabled = true, preview = false))
        assertTrue(rules.begin(world, enabled = true, preview = false))
        assertEquals(listOf(world.uid), state.activations)
        assertTrue(rules.restore(world))
        assertTrue(rules.restore(world))
        assertFalse(rules.isActive(world))
        assertEquals(listOf(world.uid to false), state.restorations)
    }

    @Test
    fun `restoration announcement reports that an originally enabled rule remains enabled`() {
        val state = Rules()
        val world = state.world(true)
        val rules = state.controller()
        assertTrue(rules.begin(world, enabled = true, preview = false))
        assertTrue(rules.restore(world))
        assertEquals(listOf(world.uid), state.activations)
        assertEquals(listOf(world.uid to true), state.restorations)
    }

    @Test
    fun `rejected activation does not announce a temporary protection that never became active`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        state.rejectChanges = true
        assertFalse(rules.begin(world, enabled = true, preview = false))
        assertFalse(rules.isActive(world))
        assertTrue(state.activations.isEmpty())
        assertTrue(state.restorations.isEmpty())
    }

    @Test
    fun `failed restoration is announced only after a successful recovery`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        assertTrue(rules.begin(world, enabled = true, preview = false))
        state.rejectChanges = true
        assertFalse(rules.restore(world))
        assertTrue(state.restorations.isEmpty())
        state.rejectChanges = false
        assertTrue(rules.recover(world))
        assertEquals(listOf(world.uid to false), state.restorations)
    }

    @Test
    fun `entry notifications are eligible only while the End override and actual rule are active`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        assertFalse(rules.isActive(world))
        rules.begin(world, enabled = true, preview = false)
        assertTrue(rules.isActive(world))
        state.set(world, false) // An admin can change the rule while the encounter is running.
        assertFalse(rules.isActive(world))
        rules.restore(world)
        assertFalse(rules.isActive(world))
    }

    @Test
    fun `normal encounters capture the old rule once and restore false on cleanup`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        assertTrue(rules.begin(world, enabled = true, preview = false))
        assertTrue(state.get(world))
        assertEquals(false, state.saved.originalRules[world.uid.toString()])
        assertTrue(rules.begin(world, enabled = true, preview = false))
        assertTrue(rules.recover(world)) // Must not restore a currently active encounter.
        assertTrue(state.get(world))
        assertEquals(1, state.saves)
        assertTrue(rules.restore(world))
        assertFalse(state.get(world))
        assertTrue(state.saved.originalRules.isEmpty())
        assertTrue(rules.restore(world)) // Stop/death/disable cleanup is idempotent.
        assertEquals(2, state.saves)
    }

    @Test
    fun `a rule that was already true remains true after the fight`() {
        val state = Rules()
        val world = state.world(true)
        val rules = state.controller()
        assertTrue(rules.begin(world, enabled = true, preview = false))
        assertTrue(rules.restore(world))
        assertTrue(state.get(world))
        assertEquals(0, state.writes)
    }

    @Test
    fun `overlapping fights in separate worlds restore independently`() {
        val state = Rules()
        val first = state.world(false)
        val second = state.world(false)
        val rules = state.controller()
        assertTrue(rules.begin(first, enabled = true, preview = false))
        assertTrue(rules.begin(second, enabled = true, preview = false))
        assertTrue(rules.restore(first))
        assertFalse(state.get(first))
        assertTrue(state.get(second))
        assertEquals(setOf(second.uid.toString()), state.saved.originalRules.keys)
        assertTrue(rules.restore(second))
        assertFalse(state.get(second))
    }

    @Test
    fun `recovery journal restores an interrupted fight before capturing a new original value`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        assertTrue(rules.begin(world, enabled = true, preview = false))
        val restarted = state.controller()
        assertTrue(restarted.recover(world))
        assertFalse(state.get(world))
        assertTrue(state.saved.originalRules.isEmpty())
        assertTrue(restarted.begin(world, enabled = true, preview = false))
        assertEquals(false, state.saved.originalRules[world.uid.toString()])
        assertTrue(restarted.restore(world))
        assertFalse(state.get(world))
    }

    @Test
    fun `recovery keeps unloaded worlds pending until they load`() {
        val state = Rules()
        val loaded = state.world(false)
        val later = state.world(false)
        val rules = state.controller()
        rules.begin(loaded, enabled = true, preview = false)
        rules.begin(later, enabled = true, preview = false)
        val restarted = state.controller()
        assertTrue(restarted.recover(loaded))
        assertEquals(setOf(later.uid.toString()), state.saved.originalRules.keys)
        assertTrue(restarted.recover(later))
        assertTrue(state.saved.originalRules.isEmpty())
    }

    @Test
    fun `journal write must succeed before the world rule can change`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        state.failSave = true
        assertFailsWith<IllegalStateException> { rules.begin(world, enabled = true, preview = false) }
        assertFalse(state.get(world))
        assertEquals(0, state.writes)
        assertTrue(state.saved.originalRules.isEmpty())
    }

    @Test
    fun `a rejected rule change does not leave an active override`() {
        val state = Rules()
        val world = state.world(false)
        state.rejectChanges = true
        val rules = state.controller()
        assertFalse(rules.begin(world, enabled = true, preview = false))
        assertFalse(state.get(world))
        assertTrue(state.saved.originalRules.isEmpty())
    }

    @Test
    fun `rejected restoration retains the journal and can be retried`() {
        val state = Rules()
        val world = state.world(false)
        val rules = state.controller()
        rules.begin(world, enabled = true, preview = false)
        state.rejectChanges = true
        assertFalse(rules.restore(world))
        assertTrue(state.get(world))
        assertEquals(false, state.saved.originalRules[world.uid.toString()])
        state.rejectChanges = false
        assertTrue(rules.recover(world))
        assertFalse(state.get(world))
        assertTrue(state.saved.originalRules.isEmpty())
    }

    private class Rules {
        var saved = KeepInventoryJournal()
        var writes = 0
        var saves = 0
        var failSave = false
        var rejectChanges = false
        val activations = mutableListOf<UUID>()
        val restorations = mutableListOf<Pair<UUID, Boolean>>()
        private val values = mutableMapOf<UUID, Boolean>()

        fun world(initial: Boolean, environment: World.Environment = World.Environment.THE_END): World {
            val id = UUID.randomUUID()
            values[id] = initial
            return eggStub { name, _ ->
                when (name) {
                    "getUID" -> id
                    "getEnvironment" -> environment
                    else -> error("Unexpected world call: $name")
                }
            }
        }

        fun get(world: World) = values.getValue(world.uid)

        fun set(world: World, value: Boolean) { values[world.uid] = value }

        fun controller() = TemporaryKeepInventory(saved,
            persist = { next ->
                check(!failSave) { "Could not save journal" }
                saved = next.copy(originalRules = next.originalRules.toMap())
                saves++
            },
            readRule = ::get,
            writeRule = { world, value ->
                check(world.uid.toString() in saved.originalRules) { "Rule changed before journaling" }
                writes++
                if (rejectChanges) false else { values[world.uid] = value; true }
            },
            onActivated = { world ->
                assertTrue(get(world))
                assertTrue(world.uid.toString() in saved.originalRules)
                activations += world.uid
            },
            onRestored = { world, original ->
                assertEquals(original, get(world))
                assertFalse(world.uid.toString() in saved.originalRules)
                restorations += world.uid to original
            })
    }
}
