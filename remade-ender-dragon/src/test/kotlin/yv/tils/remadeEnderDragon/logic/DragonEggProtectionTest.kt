/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.util.Vector
import yv.tils.remadeEnderDragon.configs.DragonEggSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DragonEggProtectionTest {
    @Test
    fun `forced egg death drops override both keep inventory states without dropping other inventory items`() {
        listOf(false, true).forEach { keepInventory ->
            val player = LogoutPlayer()
            val egg = EggTestStack(Material.DRAGON_EGG, count = 3)
            val stone = EggTestStack(Material.STONE)
            player.slots[0] = stone
            player.slots[40] = egg
            val event = player.death(keepInventory, if (keepInventory) mutableListOf() else mutableListOf(egg.clone(), stone))
            event.keepLevel = true
            event.droppedExp = 7
            DragonEggProtection { DragonEggSettings(alwaysDropOnDeath = true) }.death(event)
            assertNull(player.slots[40])
            assertSame(stone, player.slots[0])
            assertEquals(keepInventory, event.keepInventory)
            assertTrue(event.keepLevel)
            assertEquals(7, event.droppedExp)
            assertEquals(3, event.drops.filter(DragonEggRules::containsEgg).sumOf { it.amount })
            assertEquals(if (keepInventory) 1 else 2, event.drops.size)
            assertTrue(player.drops.isEmpty()) // Death pipeline, not a second manual world drop.
        }
    }

    @Test
    fun `forced egg drops account for partial queued stacks across multiple inventory slots`() {
        val player = LogoutPlayer()
        player.slots[0] = EggTestStack(Material.DRAGON_EGG, count = 3)
        player.slots[1] = EggTestStack(Material.DRAGON_EGG, count = 4)
        val event = player.death(false, mutableListOf(EggTestStack(Material.DRAGON_EGG, count = 2)))
        DragonEggProtection { DragonEggSettings(alwaysDropOnDeath = true) }.death(event)
        assertNull(player.slots[0])
        assertNull(player.slots[1])
        assertEquals(7, event.drops.sumOf { it.amount })
    }

    @Test
    fun `forced egg drops remove retained egg containers while preserving other retained items`() {
        val player = LogoutPlayer()
        val meta = eggStub<BundleMeta> { name, _ ->
            when (name) {
                "getItems" -> listOf(EggTestStack(Material.DRAGON_EGG))
                else -> error(name)
            }
        }
        val bundle = EggTestStack(Material.BUNDLE, meta)
        val stone = EggTestStack(Material.STONE)
        player.slots[4] = bundle
        val event = player.death(true)
        event.itemsToKeep.addAll(listOf(bundle, stone))
        DragonEggProtection { DragonEggSettings(alwaysDropOnDeath = true) }.death(event)
        assertNull(player.slots[4])
        assertEquals(1, event.drops.size)
        assertTrue(event.drops.single().isSimilar(bundle))
        assertEquals(1, event.itemsToKeep.size)
        assertSame(stone, event.itemsToKeep.single())
    }

    @Test
    fun `death closes inventory before taking returned egg inputs and any remaining cursor egg`() {
        val player = LogoutPlayer()
        player.onClose = { player.slots[2] = EggTestStack(Material.DRAGON_EGG, count = 2) }
        player.cursor = EggTestStack(Material.DRAGON_EGG)
        val event = player.death(true)
        DragonEggProtection { DragonEggSettings(alwaysDropOnDeath = true) }.death(event)
        assertEquals(1, player.closes)
        assertNull(player.slots[2])
        assertEquals(Material.AIR, player.cursor.type)
        assertEquals(3, event.drops.sumOf { it.amount })
    }

    @Test
    fun `disabled forced egg drops and cancelled deaths do not mutate inventory or death drops`() {
        listOf(false, true).forEach { cancelled ->
            val player = LogoutPlayer()
            val egg = EggTestStack(Material.DRAGON_EGG)
            player.slots[0] = egg
            val event = player.death(true)
            event.isCancelled = cancelled
            DragonEggProtection { DragonEggSettings(alwaysDropOnDeath = cancelled) }.death(event)
            assertSame(egg, player.slots[0])
            assertEquals(0, player.closes)
            assertTrue(event.drops.isEmpty())
        }
    }

    @Test
    fun `logout drops carried eggs including offhand and portable containers but keeps other items`() {
        val player = LogoutPlayer()
        val egg = EggTestStack(Material.DRAGON_EGG, count = 3)
        val bundleMeta = eggStub<BundleMeta> { name, _ ->
            when (name) {
                "getItems" -> listOf(egg)
                else -> error(name)
            }
        }
        val bundle = EggTestStack(Material.BUNDLE, bundleMeta)
        val stone = EggTestStack(Material.STONE)
        player.slots[0] = stone
        player.slots[12] = bundle
        player.slots[40] = egg
        DragonEggProtection { DragonEggSettings(logoutable = false) }.quit(player.event())
        assertSame(stone, player.slots[0])
        assertNull(player.slots[12])
        assertNull(player.slots[40])
        assertEquals(2, player.drops.size)
        assertSame(bundle, player.drops[0])
        assertSame(egg, player.drops[1])
        assertEquals(3, player.drops[1].amount)
    }

    @Test
    fun `logout inspects inputs returned by closing inventory without dropping the cursor twice`() {
        val player = LogoutPlayer()
        val egg = EggTestStack(Material.DRAGON_EGG)
        player.cursor = egg
        player.onClose = {
            player.slots[4] = player.cursor
            player.cursor = EggTestStack(Material.AIR)
        }
        DragonEggProtection { DragonEggSettings(logoutable = false) }.quit(player.event())
        assertEquals(1, player.closes)
        assertNull(player.slots[4])
        assertEquals(1, player.drops.size)
        assertSame(egg, player.drops.single())
    }

    @Test
    fun `logout also clears any remaining cursor egg`() {
        val player = LogoutPlayer()
        val egg = EggTestStack(Material.DRAGON_EGG)
        player.cursor = egg
        DragonEggProtection { DragonEggSettings(logoutable = false) }.quit(player.event())
        assertEquals(Material.AIR, player.cursor.type)
        assertSame(egg, player.drops.single())
    }

    @Test
    fun `allowing logout leaves inventory and cursor unchanged`() {
        val player = LogoutPlayer()
        val egg = EggTestStack(Material.DRAGON_EGG)
        player.slots[0] = egg
        player.cursor = egg
        DragonEggProtection { DragonEggSettings() }.quit(player.event())
        assertSame(egg, player.slots[0])
        assertSame(egg, player.cursor)
        assertTrue(player.drops.isEmpty())
        assertEquals(0, player.closes)
    }

    @Test
    fun `void damage recovers dropped and falling eggs at centered world spawn surface`() {
        listOf(false, true).forEach { falling ->
            val egg = VoidEntity(falling)
            val event = egg.damage(EntityDamageEvent.DamageCause.VOID)
            DragonEggProtection { DragonEggSettings(voidDisappearing = false) }.voidDamage(event)
            assertTrue(event.isCancelled)
            assertEquals(10.5, egg.teleported?.x)
            assertEquals(65.5, egg.teleported?.y)
            assertEquals(-2.5, egg.teleported?.z)
            assertEquals(Vector(), egg.velocity)
            assertEquals(0f, egg.fallDistance)
        }
    }

    @Test
    fun `void toggle and other damage causes preserve vanilla behavior`() {
        val egg = VoidEntity()
        val settings = DragonEggSettings()
        val protection = DragonEggProtection { settings }
        val void = egg.damage(EntityDamageEvent.DamageCause.VOID)
        protection.voidDamage(void)
        assertFalse(void.isCancelled)
        assertNull(egg.teleported)
        settings.voidDisappearing = false // Listeners must read the live state, not a cached draft.
        val fire = egg.damage(EntityDamageEvent.DamageCause.FIRE)
        protection.voidDamage(fire)
        assertFalse(fire.isCancelled)
        assertNull(egg.teleported)
        protection.voidDamage(void)
        assertTrue(void.isCancelled)
    }

    @Test
    fun `ordinary dropped items are not protected from the void`() {
        val stone = VoidEntity(material = Material.STONE)
        val event = stone.damage(EntityDamageEvent.DamageCause.VOID)
        DragonEggProtection { DragonEggSettings(voidDisappearing = false) }.voidDamage(event)
        assertFalse(event.isCancelled)
        assertNull(stone.teleported)
    }

    private class LogoutPlayer {
        val slots = mutableMapOf<Int, ItemStack?>()
        val drops = mutableListOf<ItemStack>()
        var cursor: ItemStack = EggTestStack(Material.AIR)
        var closes = 0
        var onClose: () -> Unit = {}
        private val inventory = eggStub<PlayerInventory> { name, args ->
            when (name) {
                "getSize" -> 43
                "getItem" -> slots[args[0] as Int]
                "setItem" -> { slots[args[0] as Int] = args[1] as ItemStack?; null }
                else -> error("Unexpected inventory call: $name")
            }
        }
        private val world = eggStub<World> { name, args ->
            when (name) {
                "dropItem" -> {
                    drops.add(args[1] as ItemStack)
                    eggStub<Item> { method, _ -> error("Unexpected drop call: $method") }
                }
                else -> error("Unexpected world call: $name")
            }
        }
        private val player = eggStub<Player> { name, args ->
            when (name) {
                "getInventory" -> inventory
                "getWorld" -> world
                "getLocation" -> Location(world, 20.0, 70.0, 30.0)
                "getItemOnCursor" -> cursor
                "setItemOnCursor" -> { cursor = args[0] as ItemStack? ?: EggTestStack(Material.AIR); null }
                "closeInventory" -> { closes++; onClose(); null }
                else -> error("Unexpected player call: $name")
            }
        }
        fun event() = PlayerQuitEvent(player, Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED)
        fun death(keepInventory: Boolean, drops: MutableList<ItemStack> = mutableListOf()): PlayerDeathEvent {
            val source = eggStub<DamageSource> { name, _ -> error("Unexpected damage source call: $name") }
            return PlayerDeathEvent(player, source, drops, 0, Component.empty(), true).apply {
                this.keepInventory = keepInventory
            }
        }
    }

    private class VoidEntity(falling: Boolean = false, material: Material = Material.DRAGON_EGG) {
        var teleported: Location? = null
        var velocity: Vector? = null
        var fallDistance = 99f
        private val world: World = eggStub { name, _ ->
            when (name) {
                "getSpawnLocation" -> Location(null, 10.1, 50.0, -2.1)
                "getHighestBlockYAt" -> 64
                "getMinHeight" -> -64
                "getMaxHeight" -> 320
                else -> error("Unexpected world call: $name")
            }
        }
        private val blockData = eggStub<BlockData> { name, _ ->
            when (name) {
                "getMaterial" -> material
                else -> error("Unexpected block data call: $name")
            }
        }
        private val call: (String, Array<out Any?>) -> Any? = { name, args ->
            when (name) {
                "getWorld" -> world
                "getItemStack" -> EggTestStack(material)
                "getBlockData" -> blockData
                "teleport" -> { teleported = args[0] as Location; true }
                "setVelocity" -> { velocity = args[0] as Vector; null }
                "setFallDistance" -> { fallDistance = args[0] as Float; null }
                else -> error("Unexpected entity call: $name")
            }
        }
        private val entity: Entity = if (falling) eggStub<FallingBlock>(call) else eggStub<Item>(call)
        private val source = eggStub<DamageSource> { name, _ -> error("Unexpected damage source call: $name") }
        fun damage(cause: EntityDamageEvent.DamageCause) = EntityDamageEvent(entity, cause, source, 1.0)
    }
}
