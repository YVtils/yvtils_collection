package yv.tils.server.maintenance

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import yv.tils.config.language.Language
import yv.tils.server.configs.ConfigFile
import yv.tils.server.configs.ServerConfigState
import yv.tils.server.listeners.PlayerLogin
import yv.tils.utils.modules.Core
import yv.tils.utils.server.ServerUtils
import java.net.InetAddress
import java.nio.file.Path
import java.util.UUID

class MaintenanceTest {
    @TempDir
    lateinit var directory: Path

    private lateinit var server: Server
    private lateinit var regular: Player
    private lateinit var bypass: Player

    @BeforeEach
    fun setup() {
        val plugin = mock(JavaPlugin::class.java)
        server = mock(Server::class.java)
        `when`(plugin.server).thenReturn(server)
        `when`(plugin.dataFolder).thenReturn(directory.toFile())
        Core.instance = plugin
        Core.core = Core.YVtilsCore(
            name = "Test", version = "test", instance = plugin,
            key = mock(NamespacedKey::class.java), pluginShort = "test"
        )

        regular = player(false)
        bypass = player(true)
        `when`(server.onlinePlayers).thenReturn(listOf(regular, bypass))
        MaintenanceHandler.applyState(false, announce = false)
        ConfigFile.state = ServerConfigState()
        ConfigFile.config.clear()
    }

    @AfterEach
    fun cleanup() {
        MaintenanceHandler.applyState(false, announce = false)
        Language.langStrings.clear()
    }

    @Test
    fun `enabling kicks only non-bypass players and repeated enable does not kick again`() {
        ConfigFile.set("maintenance.enabled", true)
        ConfigFile.set("maintenance.enabled", true)

        verify(regular, times(1)).kick(any(Component::class.java))
        verify(bypass, never()).kick(any(Component::class.java))
        assertState(true)
    }

    @Test
    fun `disabling maintenance never kicks players`() {
        MaintenanceHandler.applyState(true, announce = false)
        ConfigFile.set("maintenance.enabled", false)

        verify(regular, never()).kick(any(Component::class.java))
        verify(bypass, never()).kick(any(Component::class.java))
        assertState(false)
    }

    @Test
    fun `GUI updates enforce maintenance immediately and startup restores the saved state`() {
        val config = ConfigFile()
        config.applyState(ServerConfigState(maintenance = ServerConfigState.Maintenance(enabled = true)))
        verify(regular).kick(any(Component::class.java))
        assertState(true)

        MaintenanceHandler.applyState(false, announce = false)
        ConfigFile.state = ServerConfigState()
        config.loadConfig()

        assertState(true)
        verify(regular, times(1)).kick(any(Component::class.java))
    }

    @Test
    fun `login is blocked during maintenance except for bypass players`() {
        MaintenanceHandler.applyState(true, announce = false)
        val denied = login(regular)
        val allowed = login(bypass)

        PlayerLogin().onEvent(denied)
        PlayerLogin().onEvent(allowed)

        assertEquals(PlayerLoginEvent.Result.KICK_OTHER, denied.result)
        assertEquals(PlayerLoginEvent.Result.ALLOWED, allowed.result)
    }

    @Test
    fun `maintenance bypass does not override existing login denials`() {
        MaintenanceHandler.applyState(true, announce = false)
        for (player in listOf(regular, bypass)) {
            val event = login(player)
            val reason = Component.text("Existing ban")
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, reason)

            PlayerLogin().onEvent(event)

            assertEquals(PlayerLoginEvent.Result.KICK_BANNED, event.result)
            assertEquals(reason, event.kickMessage())
        }
    }

    @Test
    fun `normal logins are allowed outside maintenance`() {
        val event = login(regular)
        PlayerLogin().onEvent(event)
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.result)
    }

    @Test
    fun `command toggles persist state and render the translated state as text`() {
        val sender = mock(CommandSender::class.java)
        Language.langStrings["maintenance.state.on"] = Language.LangString(
            "maintenance.state.on", mutableMapOf("en" to "<red>enabled</red>")
        )
        Language.langStrings["maintenance.announcement.sender.changed"] = Language.LangString(
            "maintenance.announcement.sender.changed", mutableMapOf("en" to "Maintenance <state>")
        )

        MaintenanceHandler().maintenance(sender)
        assertState(true)
        verify(sender).sendMessage(
            net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("Maintenance <red>enabled</red>")
        )

        MaintenanceHandler().maintenance(sender)
        assertState(false)
        verify(regular, times(1)).kick(any(Component::class.java))
    }

    private fun assertState(enabled: Boolean) {
        assertEquals(enabled, ConfigFile.state.maintenance.enabled)
        assertEquals(enabled, ConfigFile.get("maintenance.enabled"))
        assertEquals(enabled, MaintenanceHandler.maintenance)
        assertEquals(enabled, ServerUtils.serverInMaintenance)
    }

    private fun player(hasBypass: Boolean): Player = mock(Player::class.java).also {
        `when`(it.uniqueId).thenReturn(UUID.randomUUID())
        `when`(it.hasPermission(MaintenanceHandler.BYPASS_PERMISSION)).thenReturn(hasBypass)
    }

    private fun login(player: Player) = PlayerLoginEvent(player, "localhost", InetAddress.getLoopbackAddress())
}
