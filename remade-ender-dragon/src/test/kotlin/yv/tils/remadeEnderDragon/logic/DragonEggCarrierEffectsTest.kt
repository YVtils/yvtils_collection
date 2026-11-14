/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DragonEggCarrierEffectsTest {
    @Test
    fun `glowing never sets a nametag color even when an existing team has no color`() {
        val board = Board()
        val alice = CarrierPlayer("Alice", board)
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val original = board.create("default")
        original.team.addEntry("Alice")
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player))
        val team = board.board.getEntryTeam("Alice")!!
        assertSame(original.team, team)
        assertTrue(alice.glowing)
        assertFalse(team.hasColor())
        assertFailsWith<IllegalStateException> { team.color() }
        effects.sync(listOf(alice.player))
        assertFalse(team.hasColor())
        team.color(NamedTextColor.GREEN)
        effects.sync(listOf(alice.player))
        assertEquals(NamedTextColor.GREEN, team.color())
        assertEquals(0, alice.scoreboardReads)
        effects.close()
    }

    @Test
    fun `inventory offhand nested eggs and cursor glow continuously until the last egg is gone`() {
        val board = Board()
        val player = CarrierPlayer("Alice", board)
        val effects = DragonEggCarrierEffects()
        val meta = eggStub<BundleMeta> { name, _ ->
            when (name) { "getItems" -> listOf(EggTestStack(Material.DRAGON_EGG)); else -> error(name) }
        }
        listOf(0, 40).forEach { slot ->
            player.slots[slot] = EggTestStack(Material.BUNDLE, meta)
            effects.sync(listOf(player.player))
            assertTrue(player.glowing)
            player.glowing = false // The effect is sustained, not a one-time toggle.
            effects.sync(listOf(player.player))
            assertTrue(player.glowing)
            player.slots[slot] = null
            effects.sync(listOf(player.player))
            assertFalse(player.glowing)
            assertTrue(board.teams.isEmpty())
        }
        player.cursor = EggTestStack(Material.DRAGON_EGG)
        effects.sync(listOf(player.player))
        assertTrue(player.glowing)
        effects.close()
        effects.close()
        assertFalse(player.glowing)
        assertTrue(board.teams.isEmpty())
    }

    @Test
    fun `glowing preserves shared team membership nametag colors and status settings`() {
        val board = Board()
        val alice = CarrierPlayer("Alice", board)
        val bob = CarrierPlayer("Bob", board)
        val original = board.create("status")
        original.team.prefix(Component.text("[Status] "))
        original.team.suffix(Component.text("!"))
        original.team.color(NamedTextColor.GREEN)
        original.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        original.team.addEntry("Alice")
        original.team.addEntry("Bob")
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        alice.glowing = true
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player, bob.player))
        assertSame(original.team, board.board.getEntryTeam("Alice"))
        assertEquals(Component.text("[Status] "), original.team.prefix())
        assertEquals(Component.text("!"), original.team.suffix())
        assertEquals(Team.OptionStatus.NEVER, original.team.getOption(Team.Option.COLLISION_RULE))
        assertSame(original.team, board.board.getEntryTeam("Bob"))
        assertEquals(NamedTextColor.GREEN, original.team.color())
        original.team.prefix(Component.text("[Edited] "))
        effects.sync(listOf(alice.player, bob.player))
        assertEquals(Component.text("[Edited] "), original.team.prefix())
        assertEquals(0, alice.scoreboardReads)
        assertEquals(0, bob.scoreboardReads)
        effects.close()
        assertTrue(alice.glowing) // Pre-existing glow is not removed.
        assertSame(original.team, board.board.getEntryTeam("Alice"))
        assertEquals(setOf("status"), board.teams.keys)
    }

    @Test
    fun `late viewers and scoreboard switches retain their own team colors and nametags`() {
        val first = Board()
        val alice = CarrierPlayer("Alice", first)
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val firstTeam = first.create("first")
        firstTeam.team.color(NamedTextColor.GREEN)
        firstTeam.team.addEntry("Alice")
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player))
        val second = Board()
        val bob = CarrierPlayer("Bob", second)
        val secondTeam = second.create("second")
        secondTeam.team.color(NamedTextColor.BLUE)
        secondTeam.team.addEntry("Alice")
        effects.sync(listOf(alice.player, bob.player))
        assertEquals(NamedTextColor.BLUE, second.board.getEntryTeam("Alice")?.color())
        alice.board = second
        effects.sync(listOf(alice.player, bob.player))
        assertSame(firstTeam.team, first.board.getEntryTeam("Alice"))
        assertEquals(NamedTextColor.GREEN, firstTeam.team.color())
        effects.quit(PlayerQuitEvent(alice.player, Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED))
        assertFalse(alice.glowing)
        assertSame(secondTeam.team, second.board.getEntryTeam("Alice"))
        assertEquals(NamedTextColor.BLUE, secondTeam.team.color())
    }

    @Test
    fun `external team removal does not resurrect an old status on cleanup`() {
        val board = Board()
        val alice = CarrierPlayer("Alice", board)
        val original = board.create("status")
        original.team.addEntry("Alice")
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player))
        board.board.getEntryTeam("Alice")!!.unregister()
        effects.sync(listOf(alice.player))
        effects.close()
        assertNull(board.board.getEntryTeam("Alice"))
    }

    @Test
    fun `death cleans glow without changing teams but cancelled death leaves the carrier marked`() {
        val board = Board()
        val alice = CarrierPlayer("Alice", board)
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val original = board.create("status")
        original.team.addEntry("Alice")
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player))
        val source = eggStub<DamageSource> { method, _ -> error(method) }
        val event = PlayerDeathEvent(alice.player, source, mutableListOf(), 0, Component.empty(), true)
        event.isCancelled = true
        effects.death(event)
        assertTrue(alice.glowing)
        assertTrue(board.teams.isNotEmpty())
        event.isCancelled = false
        effects.death(event)
        assertFalse(alice.glowing)
        assertSame(original.team, board.board.getEntryTeam("Alice"))
        effects.close()
    }

    @Test
    fun `forced End particles trail horizontal walking using server support not the client ground flag`() {
        val alice = CarrierPlayer("Alice", Board())
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val effects = DragonEggCarrierEffects()
        fun sync() { effects.sync(listOf(alice.player)); effects.tickTrails() }
        sync()
        alice.at.yaw = 180f
        sync()
        assertTrue(alice.particles.isEmpty())
        alice.at.x = 1.0
        sync()
        assertEquals(2, alice.particles.size)
        assertEquals(Particle.PORTAL, alice.particles.first().first)
        assertEquals(Particle.DRAGON_BREATH, alice.particles.last().first)
        assertEquals(0.4, alice.particles.first().second.x, 0.0001)
        assertEquals(70.45, alice.particles.first().second.y, 0.0001)
        alice.particleCalls.forEach { call ->
            assertEquals(true, call.last()) // Forced rendering even with reduced particle settings.
            assertSame(alice.player, call[2]) // Respect source-player visibility.
        }
        assertEquals(1.0f, alice.particleCalls.last()[11]) // Paper requires Float dragon-breath data.
        alice.at.x = 0.0 // Walking backwards puts the trail ahead of facing, behind movement.
        sync()
        assertEquals(0.6, alice.particles.last().second.x, 0.0001)
        alice.grounded = false
        alice.at.x = 1.0
        sync()
        alice.grounded = true
        effects.teleport(PlayerTeleportEvent(alice.player, alice.at.clone(), alice.at.clone().add(1.0, 0.0, 0.0)))
        alice.at.x = 2.0
        sync()
        assertEquals(4, alice.particles.size)
        alice.dead = true
        sync()
        assertFalse(alice.glowing)
        effects.close()
    }

    @Test
    fun `slow walking emits a trail between inventory scans and losing the egg stops it`() {
        val alice = CarrierPlayer("Alice", Board())
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player))
        alice.at.x += 0.01
        effects.tickTrails()
        assertEquals(2, alice.particles.size)
        alice.at.x += 0.01
        effects.tickTrails()
        assertEquals(4, alice.particles.size)
        alice.slots[0] = null
        effects.sync(listOf(alice.player))
        alice.at.x += 0.01
        effects.tickTrails()
        assertEquals(4, alice.particles.size)
        assertFalse(alice.glowing)
        effects.close()
    }

    @Test
    fun `vehicles flight gliding and upward movement never produce a walking trail`() {
        val alice = CarrierPlayer("Alice", Board())
        alice.slots[0] = EggTestStack(Material.DRAGON_EGG)
        val effects = DragonEggCarrierEffects()
        effects.sync(listOf(alice.player))
        alice.vehicle = true
        alice.at.x += 1.0
        effects.tickTrails()
        alice.vehicle = false
        alice.flying = true
        alice.at.x += 1.0
        effects.tickTrails()
        alice.flying = false
        alice.gliding = true
        alice.at.x += 1.0
        effects.tickTrails()
        alice.gliding = false
        alice.verticalVelocity = 0.2
        alice.at.x += 1.0
        effects.tickTrails()
        assertTrue(alice.particles.isEmpty())
        effects.close()
    }

    private class CarrierPlayer(val name: String, var board: Board) {
        val id = UUID.randomUUID()
        val slots = arrayOfNulls<org.bukkit.inventory.ItemStack>(41)
        var cursor = EggTestStack(Material.AIR)
        var glowing = false
        var grounded = true
        var dead = false
        var vehicle = false
        var flying = false
        var gliding = false
        var verticalVelocity = 0.0
        var scoreboardReads = 0
        val particles = mutableListOf<Pair<Particle, Location>>()
        val particleCalls = mutableListOf<List<Any?>>()
        private val world: World = eggStub { method, args ->
            when (method) {
                "spawnParticle" -> {
                    particles += (args[0] as Particle) to Location(world, args[3] as Double, args[4] as Double, args[5] as Double)
                    particleCalls += args.toList()
                    null
                }
                else -> error(method)
            }
        }
        val at = Location(world, 0.0, 70.0, 0.0)
        private val inventory = eggStub<PlayerInventory> { method, _ ->
            when (method) { "getContents" -> slots; else -> error(method) }
        }
        val player = eggStub<Player> { method, args ->
            when (method) {
                "getUniqueId" -> id
                "getName" -> name
                "getInventory" -> inventory
                "getItemOnCursor" -> cursor
                "isDead" -> dead
                "isGlowing" -> glowing
                "setGlowing" -> { glowing = args[0] as Boolean; null }
                "getScoreboard" -> { scoreboardReads++; board.board }
                "getLocation" -> at.clone()
                "getWorld" -> world
                "isOnline" -> true
                "isOnGround" -> false // Reproduce unreliable client-reported ground state.
                "getVelocity" -> Vector(0.0, verticalVelocity, 0.0)
                "getBoundingBox" -> BoundingBox(at.x - 0.3, at.y, at.z - 0.3, at.x + 0.3, at.y + 1.8, at.z + 0.3)
                "wouldCollideUsing" -> grounded
                "isInsideVehicle" -> vehicle
                "isFlying" -> flying
                "isGliding" -> gliding
                else -> error(method)
            }
        }
    }

    private class Board {
        val teams = mutableMapOf<String, TestTeam>()
        val board = eggStub<Scoreboard> { method, args ->
            when (method) {
                "getTeam" -> teams[args[0]]?.team
                "getEntryTeam" -> teams.values.firstOrNull { args[0] in it.entries }?.team
                "registerNewTeam" -> create(args[0] as String).team
                else -> error(method)
            }
        }
        fun create(name: String) = TestTeam(name, this).also { teams[name] = it }
    }

    private class TestTeam(val name: String, private val board: Board) {
        val entries = mutableSetOf<String>()
        private var color: NamedTextColor? = null
        private val properties = mutableMapOf<String, Any?>(
            "displayName" to Component.text(name), "prefix" to Component.empty(), "suffix" to Component.empty(),
            "AllowFriendlyFire" to true, "CanSeeFriendlyInvisibles" to false,
        )
        private val options = Team.Option.entries.associateWith { Team.OptionStatus.ALWAYS }.toMutableMap()
        val team = eggStub<Team> { method, args ->
            when (method) {
                "getName" -> name
                "getSize" -> entries.size
                "hasEntry" -> args[0] in entries
                "addEntry" -> {
                    board.teams.values.forEach { it.entries.remove(args[0]) }
                    entries.add(args[0] as String); null
                }
                "removeEntry" -> entries.remove(args[0])
                "unregister" -> { board.teams.remove(name); null }
                "hasColor" -> color != null
                "color" -> if (args.isEmpty()) color ?: error("Team does not have a color!")
                    else { color = args[0] as NamedTextColor?; null }
                "displayName", "prefix", "suffix" -> if (args.isEmpty()) properties[method]
                    else { properties[method] = args[0]; null }
                "allowFriendlyFire" -> properties["AllowFriendlyFire"]
                "canSeeFriendlyInvisibles" -> properties["CanSeeFriendlyInvisibles"]
                "setAllowFriendlyFire", "setCanSeeFriendlyInvisibles" -> { properties[method.removePrefix("set")] = args[0]; null }
                "getOption" -> options[args[0]]
                "setOption" -> { options[args[0] as Team.Option] = args[1] as Team.OptionStatus; null }
                else -> error(method)
            }
        }
    }
}
