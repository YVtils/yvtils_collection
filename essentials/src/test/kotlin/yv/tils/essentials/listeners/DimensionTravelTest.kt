package yv.tils.essentials.listeners

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import yv.tils.config.language.Language
import yv.tils.essentials.config.StatesFile
import yv.tils.essentials.language.LangStrings
import yv.tils.essentials.permissions.Permissions
import yv.tils.utils.modules.Core
import java.util.UUID

class DimensionTravelTest {
    private lateinit var overworld: World
    private lateinit var end: World
    private lateinit var player: Player
    private lateinit var previousState: StatesFile.EssentialsState
    private var previousTranslation: Language.LangString? = null

    @BeforeEach
    fun setup() {
        val plugin = mock(JavaPlugin::class.java)
        Core.core = Core.YVtilsCore(
            name = "Test", version = "test", instance = plugin,
            key = mock(NamespacedKey::class.java), pluginShort = "test"
        )

        overworld = world("world")
        end = world("world_the_end")
        player = mock(Player::class.java)
        `when`(player.uniqueId).thenReturn(UUID.randomUUID())

        previousState = StatesFile.state
        StatesFile.state = StatesFile.EssentialsState(dimensions = mutableMapOf(end.name to false))
        val key = LangStrings.DIMENSION_TRAVEL_DISABLED.key
        previousTranslation = Language.langStrings.put(
            key, Language.LangString(key, mutableMapOf("en" to "Travel to <dimension> is disabled."))
        )
    }

    @AfterEach
    fun cleanup() {
        StatesFile.state = previousState
        val key = LangStrings.DIMENSION_TRAVEL_DISABLED.key
        val previous = previousTranslation
        if (previous == null) Language.langStrings.remove(key)
        else Language.langStrings[key] = previous
    }

    @ParameterizedTest
    @EnumSource(TeleportCause::class)
    fun `all teleport causes including ender pearls are blocked into a disabled world`(cause: TeleportCause) {
        val event = teleport(cause = cause)

        PlayerTeleport().onEvent(event)

        assertTrue(event.isCancelled)
        verify(player).sendActionBar(Component.text("Travel to world_the_end is disabled."))
    }

    @ParameterizedTest
    @EnumSource(TeleportCause::class, names = ["END_PORTAL", "NETHER_PORTAL"])
    fun `normal portal entry still uses the dimension restriction`(cause: TeleportCause) {
        val destination = if (cause == TeleportCause.END_PORTAL) end else world("world_nether")
        StatesFile.state.dimensions[destination.name] = false
        val event = PlayerPortalEvent(player, location(overworld), location(destination), cause)

        PlayerPortal().onEvent(event)

        assertTrue(event.isCancelled)
        verify(player).sendActionBar(Component.text("Travel to ${destination.name} is disabled."))
    }

    @Test
    fun `same-world ender pearls remain allowed inside a disabled world`() {
        val event = teleport(from = end)

        PlayerTeleport().onEvent(event)

        assertAllowed(event)
    }

    @Test
    fun `players can leave a disabled world`() {
        val event = teleport(from = end, to = overworld)

        PlayerTeleport().onEvent(event)

        assertAllowed(event)
    }

    @Test
    fun `explicitly enabled destinations remain allowed`() {
        StatesFile.state.dimensions[end.name] = true
        val event = teleport()

        PlayerTeleport().onEvent(event)

        assertAllowed(event)
    }

    @Test
    fun `unconfigured destinations remain allowed`() {
        StatesFile.state.dimensions.clear()
        val event = teleport()

        PlayerTeleport().onEvent(event)

        assertAllowed(event)
    }

    @Test
    fun `bypass permission permits cross-world pearl travel`() {
        `when`(player.hasPermission(Permissions.BYPASS_DIMENSION_BLOCK.permission.name)).thenReturn(true)
        val event = teleport()

        PlayerTeleport().onEvent(event)

        assertAllowed(event)
    }

    @Test
    fun `existing cancellation is preserved without another denial message`() {
        val event = teleport()
        event.isCancelled = true

        PlayerTeleport().onEvent(event)

        assertTrue(event.isCancelled)
        verify(player, never()).sendActionBar(any(Component::class.java))
    }

    @Test
    fun `a destination without a world is ignored safely`() {
        val event = teleport(to = null)

        PlayerTeleport().onEvent(event)

        assertAllowed(event)
    }

    @Test
    fun `teleport checks the destination after earlier listeners redirect it`() {
        val event = teleport(to = overworld)
        event.to = location(end)

        PlayerTeleport().onEvent(event)

        assertTrue(event.isCancelled)
    }

    @Test
    fun `portal and teleport listeners check late and ignore cancelled events`() {
        for ((listener, event) in listOf(
            PlayerPortal::class.java to PlayerPortalEvent::class.java,
            PlayerTeleport::class.java to PlayerTeleportEvent::class.java
        )) {
            val annotation = listener.getMethod("onEvent", event).getAnnotation(EventHandler::class.java)
            assertEquals(EventPriority.HIGHEST, annotation.priority)
            assertTrue(annotation.ignoreCancelled)
        }
        assertNotSame(PlayerPortalEvent.getHandlerList(), PlayerTeleportEvent.getHandlerList())
    }

    private fun world(name: String): World = mock(World::class.java).also {
        `when`(it.name).thenReturn(name)
    }

    private fun location(world: World?) = Location(world, 0.0, 64.0, 0.0)

    private fun teleport(
        from: World = overworld,
        to: World? = end,
        cause: TeleportCause = TeleportCause.ENDER_PEARL
    ) = PlayerTeleportEvent(player, location(from), location(to), cause)

    private fun assertAllowed(event: PlayerTeleportEvent) {
        assertFalse(event.isCancelled)
        verify(player, never()).sendActionBar(any(Component::class.java))
    }
}
