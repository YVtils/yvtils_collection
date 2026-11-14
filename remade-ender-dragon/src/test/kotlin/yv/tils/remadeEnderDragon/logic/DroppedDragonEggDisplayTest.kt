/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import org.bukkit.Color
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.damage.DamageSource
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import java.util.UUID
import java.util.function.Consumer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DroppedDragonEggDisplayTest {
    private val block = eggStub<BlockData> { method, _ -> error(method) }

    @Test
    fun `dropped eggs use enlarged glowing nonpersistent displays without altering the stack`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG, count = 3))
        val stack = drop.stack
        val effects = DroppedDragonEggDisplay { block }
        effects.track(drop.item)
        effects.track(drop.item)
        assertTrue(drop.displays.isEmpty()) // Item registration never spawns another entity inline.
        effects.sync()
        assertEquals(1, drop.displays.size)
        assertFalse(drop.visible)
        assertFalse(drop.ages)
        assertFalse(drop.gravity)
        assertEquals(Vector(), drop.velocity)
        assertSame(stack, drop.stack)
        val display = drop.displays.single()
        assertSame(block, display.settings["setBlock"])
        assertEquals(true, display.settings["setGlowing"])
        assertEquals(Color.fromRGB(0xAA00AA), display.settings["setGlowColorOverride"])
        assertEquals(false, display.settings["setPersistent"])
        assertEquals(true, display.settings["setInvulnerable"])
        assertEquals(false, display.settings["setGravity"])
        val transformation = display.settings["setTransformation"] as Transformation
        assertEquals(1.5f, transformation.scale.x)
        assertEquals(-0.75f, transformation.translation.x)
        effects.close()
        effects.close()
        assertTrue(drop.visible)
        assertTrue(drop.ages)
        assertTrue(drop.gravity)
        assertFalse(display.valid)
        assertTrue(drop.data.isEmpty())
        assertSame(stack, drop.stack)
    }

    @Test
    fun `egg containers cannot despawn and unrelated items retain vanilla behavior`() {
        val meta = eggStub<BundleMeta> { method, _ ->
            when (method) { "getItems" -> listOf(EggTestStack(Material.DRAGON_EGG)); else -> error(method) }
        }
        val effects = DroppedDragonEggDisplay { block }
        listOf(EggTestStack(Material.DRAGON_EGG), EggTestStack(Material.BUNDLE, meta), EggTestStack(Material.STONE))
            .forEach { stack ->
                val drop = Drop(stack)
                val event = ItemDespawnEvent(drop.item, drop.at.clone())
                effects.despawn(event)
                assertEquals(stack.type != Material.STONE, event.isCancelled)
                effects.track(drop.item)
                effects.sync()
                assertEquals(stack.type != Material.STONE, drop.displays.isNotEmpty())
                assertEquals(stack.type == Material.STONE, drop.gravity)
                assertSame(stack, drop.stack)
            }
        effects.close()
    }

    @Test
    fun `displays follow backing recovery then disappear on pickup or merging`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        drop.lifecycle { effects.added(EntityAddToWorldEvent(drop.item, drop.world)) }
        assertTrue(drop.displays.isEmpty())
        drop.at.y = 65.5
        effects.sync()
        assertEquals(65.5, drop.displays.single().at.y)
        drop.lifecycle { effects.removed(EntityRemoveFromWorldEvent(drop.item, drop.world)) }
        assertTrue(drop.displays.single().valid) // Visual removal also waits until the chunk callback ends.
        assertTrue(drop.visible)
        assertTrue(drop.ages)
        assertTrue(drop.gravity)
        effects.sync()
        assertFalse(drop.displays.single().valid)
        assertEquals(1, drop.displays.size)
        effects.close()
    }

    @Test
    fun `chunk reload and restart recover saved backing state without saving duplicate eggs`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val before = DroppedDragonEggDisplay { block }
        before.track(drop.item)
        before.sync()
        val after = DroppedDragonEggDisplay { block }
        drop.displays.single().valid = false // Displays are not persisted across restart.
        after.track(drop.item)
        after.sync()
        assertEquals(2, drop.displays.size)
        after.close()
        assertTrue(drop.visible)
        assertTrue(drop.ages)
        assertTrue(drop.gravity)

        drop.visible = false
        drop.ages = false
        drop.gravity = false
        val alreadyProtected = DroppedDragonEggDisplay { block }
        alreadyProtected.track(drop.item)
        alreadyProtected.close()
        assertFalse(drop.visible)
        assertFalse(drop.ages)
        assertFalse(drop.gravity)
    }

    @Test
    fun `entity chunk unload restores backing state and chunk load creates only a new visual`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        effects.track(drop.item)
        effects.sync()
        val chunk = eggStub<Chunk> { method, _ ->
            when (method) { "getWorld" -> drop.world; else -> error(method) }
        }
        drop.lifecycle { effects.unloaded(EntitiesUnloadEvent(chunk, listOf(drop.item))) }
        assertTrue(drop.displays.single().valid)
        assertTrue(drop.visible)
        assertTrue(drop.ages)
        assertTrue(drop.gravity)
        assertTrue(drop.data.isEmpty())
        drop.chunkLoaded = false
        effects.sync()
        assertFalse(drop.displays.single().valid)
        drop.chunkLoaded = true
        drop.lifecycle { effects.added(EntityAddToWorldEvent(drop.item, drop.world)) }
        effects.sync()
        assertEquals(2, drop.displays.size)
        assertFalse(drop.visible)
        assertFalse(drop.ages)
        assertFalse(drop.gravity)
        effects.close()
    }

    @Test
    fun `destroyed visuals are recreated and changed backing stacks stop being egg displays`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        effects.track(drop.item)
        effects.sync()
        drop.displays.single().valid = false
        effects.sync()
        assertEquals(2, drop.displays.size)
        assertTrue(drop.displays.last().valid)
        drop.stack = EggTestStack(Material.STONE)
        effects.sync()
        assertFalse(drop.displays.last().valid)
        assertTrue(drop.visible)
        assertTrue(drop.ages)
        assertTrue(drop.gravity)
        effects.close()
    }

    @Test
    fun `hover stops late throw velocity and gently bobs only the display`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        effects.track(drop.item)
        assertFalse(drop.gravity)
        assertEquals(Vector(), drop.velocity)
        drop.velocity = Vector(0.3, 0.2, -0.1) // Assigned after the add-to-world event.
        drop.gravity = true
        effects.sync()
        assertFalse(drop.gravity)
        assertEquals(Vector(), drop.velocity)
        repeat(20) { effects.sync() }
        assertEquals(70.1, drop.displays.single().at.y, 0.0001)
        assertEquals(70.0, drop.at.y)
        repeat(40) { effects.sync() }
        assertEquals(69.9, drop.displays.single().at.y, 0.0001)
        assertEquals(70.0, drop.at.y)
        effects.close()
        assertTrue(drop.gravity)
    }

    @Test
    fun `legacy saved item state captures gravity before hovering and restores it on cleanup`() {
        listOf(false, true).forEach { gravity ->
            val drop = Drop(EggTestStack(Material.DRAGON_EGG))
            drop.visible = false
            drop.ages = false
            drop.gravity = gravity
            drop.data[NamespacedKey("yvtils", "dragon_egg_item_state")] = 3.toByte()
            val effects = DroppedDragonEggDisplay { block }
            effects.track(drop.item)
            assertFalse(drop.gravity)
            effects.close()
            assertTrue(drop.visible)
            assertTrue(drop.ages)
            assertEquals(gravity, drop.gravity)
        }
    }

    @Test
    fun `add callbacks defer display spawning and duplicate callbacks create only one visual`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        drop.lifecycle {
            effects.added(EntityAddToWorldEvent(drop.item, drop.world))
            effects.added(EntityAddToWorldEvent(drop.item, drop.world))
        }
        assertTrue(drop.displays.isEmpty())
        assertFalse(drop.gravity)
        assertFalse(drop.ages)
        effects.sync()
        assertEquals(1, drop.displays.size)
        assertTrue(drop.displays.single().valid)
        effects.close()
    }

    @Test
    fun `items picked up unloaded or changed before the queued spawn never create an orphan display`() {
        for (reason in listOf("removed", "not-in-world", "unloaded", "changed")) {
            val drop = Drop(EggTestStack(Material.DRAGON_EGG))
            val effects = DroppedDragonEggDisplay { block }
            drop.lifecycle { effects.added(EntityAddToWorldEvent(drop.item, drop.world)) }
            when (reason) {
                "removed" -> drop.valid = false
                "not-in-world" -> drop.inWorld = false
                "unloaded" -> drop.chunkLoaded = false
                "changed" -> drop.stack = EggTestStack(Material.STONE)
            }
            effects.sync()
            assertTrue(drop.displays.isEmpty(), reason)
            assertTrue(drop.visible, reason)
            assertTrue(drop.gravity, reason)
            assertTrue(drop.ages, reason)
            assertTrue(drop.data.isEmpty(), reason)
            effects.close()
        }
    }

    @Test
    fun `remove and readd before the next tick replace only the visual without duplicating the stack`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG, count = 3))
        val original = drop.stack
        val effects = DroppedDragonEggDisplay { block }
        effects.track(drop.item)
        effects.sync()
        drop.lifecycle {
            effects.removed(EntityRemoveFromWorldEvent(drop.item, drop.world))
            effects.added(EntityAddToWorldEvent(drop.item, drop.world))
        }
        assertEquals(1, drop.displays.size)
        effects.sync()
        assertEquals(2, drop.displays.size)
        assertEquals(1, drop.displays.count { it.valid })
        assertSame(original, drop.stack)
        assertEquals(3, drop.stack.amount)
        effects.close()
    }

    @Test
    fun `disable cancels a queued spawn and restores backing state without creating a display`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        drop.lifecycle { effects.added(EntityAddToWorldEvent(drop.item, drop.world)) }
        effects.close()
        effects.sync()
        assertTrue(drop.displays.isEmpty())
        assertTrue(drop.visible)
        assertTrue(drop.gravity)
        assertTrue(drop.ages)
        assertTrue(drop.data.isEmpty())
    }

    @Test
    fun `lava fire cactus contact and hot floors cannot damage egg drops before or after display creation`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG, count = 3))
        val original = drop.stack
        val effects = DroppedDragonEggDisplay { block }
        val source = eggStub<DamageSource> { method, _ -> error(method) }
        val causes = listOf(EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.CONTACT,
            EntityDamageEvent.DamageCause.HOT_FLOOR)
        for (rendered in listOf(false, true)) {
            if (rendered) { effects.track(drop.item); effects.sync() }
            for (cause in causes) {
                drop.fireTicks = 100
                val event = EntityDamageEvent(drop.item, cause, source, 20.0)
                effects.damage(event)
                assertTrue(event.isCancelled, cause.name)
                assertEquals(0, drop.fireTicks)
                assertSame(original, drop.stack)
                assertEquals(3, drop.stack.amount)
            }
        }
        assertTrue(drop.displays.single().valid)
        effects.close()
    }

    @Test
    fun `egg bearing containers cannot ignite or take lava and cactus damage`() {
        val meta = eggStub<BundleMeta> { method, _ ->
            when (method) { "getItems" -> listOf(EggTestStack(Material.DRAGON_EGG)); else -> error(method) }
        }
        val drop = Drop(EggTestStack(Material.BUNDLE, meta))
        val original = drop.stack
        val effects = DroppedDragonEggDisplay { block }
        val source = eggStub<DamageSource> { method, _ -> error(method) }
        drop.fireTicks = 100
        val combust = EntityCombustEvent(drop.item, 10.0f)
        effects.combust(combust)
        assertTrue(combust.isCancelled)
        assertEquals(0, drop.fireTicks)
        for (cause in listOf(EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.CONTACT)) {
            val damage = EntityDamageEvent(drop.item, cause, source, 20.0)
            effects.damage(damage)
            assertTrue(damage.isCancelled)
        }
        assertSame(original, drop.stack)
        effects.close()
    }

    @Test
    fun `burning eggs are extinguished on registration and subsequent updates without changing metadata`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        drop.fireTicks = 200
        effects.track(drop.item)
        assertEquals(0, drop.fireTicks)
        drop.fireTicks = 200
        effects.sync()
        assertEquals(0, drop.fireTicks)
        assertTrue(drop.displays.single().valid)
        effects.close()
    }

    @Test
    fun `unrelated items and players retain normal fire and contact behavior`() {
        val drop = Drop(EggTestStack(Material.STONE))
        val effects = DroppedDragonEggDisplay { block }
        val source = eggStub<DamageSource> { method, _ -> error(method) }
        val player = eggStub<Player> { method, _ -> error(method) }
        for (entity in listOf(drop.item, player)) {
            val combust = EntityCombustEvent(entity, 10.0f)
            effects.combust(combust)
            assertFalse(combust.isCancelled)
            for (cause in listOf(EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.CONTACT)) {
                val damage = EntityDamageEvent(entity, cause, source, 20.0)
                effects.damage(damage)
                assertFalse(damage.isCancelled)
            }
        }
        effects.close()
    }

    @Test
    fun `egg damage protection does not override explosions or the separate void rule`() {
        val drop = Drop(EggTestStack(Material.DRAGON_EGG))
        val effects = DroppedDragonEggDisplay { block }
        val source = eggStub<DamageSource> { method, _ -> error(method) }
        for (cause in listOf(EntityDamageEvent.DamageCause.VOID, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            val damage = EntityDamageEvent(drop.item, cause, source, 20.0)
            effects.damage(damage)
            assertFalse(damage.isCancelled, cause.name)
        }
        effects.close()
    }

    private class Drop(var stack: ItemStack) {
        val id = UUID.randomUUID()
        var visible = true
        var ages = true
        var gravity = true
        var velocity = Vector(0.2, 0.1, 0.3)
        var valid = true
        var inWorld = true
        var chunkLoaded = true
        var fireTicks = 0
        private var receivingChunkUpdate = false
        val data = mutableMapOf<NamespacedKey, Any>()
        val displays = mutableListOf<TestDisplay>()
        private val pdc = eggStub<PersistentDataContainer> { method, args ->
            when (method) {
                "get" -> data[args[0]]
                "set" -> { data[args[0] as NamespacedKey] = args[2]!!; null }
                "remove" -> { data.remove(args[0]); null }
                else -> error(method)
            }
        }
        val world: World = eggStub { method, args ->
            when (method) {
                "spawn" -> {
                    check(!receivingChunkUpdate) { "Cannot spawn a display while its entity chunk is receiving update" }
                    check(args[1] == BlockDisplay::class.java)
                    val display = TestDisplay(args[0] as Location) {
                        check(!receivingChunkUpdate) { "Cannot mutate a display while its entity chunk is receiving update" }
                    }
                    @Suppress("UNCHECKED_CAST")
                    (args[2] as Consumer<BlockDisplay>).accept(display.display)
                    displays += display
                    display.display
                }
                "isChunkLoaded" -> chunkLoaded
                else -> error(method)
            }
        }
        val at = Location(world, 0.0, 70.0, 0.0)
        val item = eggStub<Item> { method, args ->
            when (method) {
                "getUniqueId" -> id
                "getItemStack" -> stack
                "getPersistentDataContainer" -> pdc
                "isVisibleByDefault" -> visible
                "setVisibleByDefault" -> { visible = args[0] as Boolean; null }
                "willAge" -> ages
                "setWillAge" -> { ages = args[0] as Boolean; null }
                "hasGravity" -> gravity
                "setGravity" -> { gravity = args[0] as Boolean; null }
                "getVelocity" -> velocity.clone()
                "setVelocity" -> { velocity = (args[0] as Vector).clone(); null }
                "getWorld" -> world
                "getLocation" -> at.clone()
                "isValid" -> valid
                "isInWorld" -> inWorld
                "getFireTicks" -> fireTicks
                "setFireTicks" -> { fireTicks = args[0] as Int; null }
                else -> error("Unexpected backing item mutation: $method")
            }
        }

        fun lifecycle(action: () -> Unit) {
            receivingChunkUpdate = true
            try { action() } finally { receivingChunkUpdate = false }
        }
    }

    private class TestDisplay(var at: Location, private val checkMutation: () -> Unit) {
        var valid = true
        val settings = mutableMapOf<String, Any?>()
        val display = eggStub<BlockDisplay> { method, args ->
            when {
                method.startsWith("set") -> { settings[method] = args[0]; null }
                method == "isValid" -> valid
                method == "getLocation" -> at.clone()
                method == "getWorld" -> at.world
                method == "teleport" -> { checkMutation(); at = (args[0] as Location).clone(); true }
                method == "remove" -> { checkMutation(); valid = false; null }
                else -> error(method)
            }
        }
    }
}
