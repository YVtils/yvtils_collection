package yv.tils.yv_smp.logic.start

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Scoreboard
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import yv.tils.utils.modules.Core
import yv.tils.utils.player.TemporaryFlight
import java.util.UUID
import java.util.function.Consumer

class CinematicRuntimeTest {
    @AfterEach
    fun clearHook() {
        TemporaryFlight.release = Consumer { }
        PhaseHandler.currentPhase = Phase.NONE
    }

    @Test
    fun `temporary flight is released before capture and cannot leak out of spectator mode`() {
        val player = player()
        var allowed = true
        `when`(player.allowFlight).thenAnswer { allowed }
        TemporaryFlight.release = Consumer { allowed = false }

        CinematicRuntime.capture(listOf(player))
        `when`(player.gameMode).thenReturn(GameMode.SPECTATOR)
        CinematicRuntime.restore(player, returnToOrigin = false)

        verify(player).gameMode = GameMode.SURVIVAL
        verify(player).allowFlight = false
        verify(player, times(2)).isFlying = false
    }

    @Test
    fun `legitimate preexisting flight survives the cinematic`() {
        val player = player()
        `when`(player.allowFlight).thenReturn(true)
        `when`(player.isFlying).thenReturn(true)
        CinematicRuntime.capture(listOf(player))
        CinematicRuntime.restore(player, returnToOrigin = false)
        verify(player).allowFlight = true
        verify(player).isFlying = true
    }

    @Test
    fun `late joiners enter the shared participant list and restore original state and visibility`() {
        Core.instance = mock(JavaPlugin::class.java)
        val first = player()
        val late = player()
        PhaseHandler.currentPhase = Phase.PREPARATION
        `when`(late.location).thenReturn(Location(null, 4.0, 5.0, 6.0))
        val firstBoard = first.scoreboard
        val lateBoard = late.scoreboard
        val lateOrigin = late.location.clone()
        `when`(first.canSee(late)).thenReturn(true)
        `when`(late.canSee(first)).thenReturn(true)
        val participants = CinematicRuntime.prepare(listOf(first))
        assertTrue(CinematicRuntime.addPlayer(late))
        assertFalse(CinematicRuntime.addPlayer(late))
        assertEquals(listOf(first, late), participants)
        verify(late).gameMode = GameMode.SPECTATOR
        verify(late).teleport(first.location)
        verify(late).hidePlayer(Core.instance, first)
        verify(first).hidePlayer(Core.instance, late)

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Player> { Bukkit.getPlayer(first.uniqueId) }.thenReturn(first)
            bukkit.`when`<Player> { Bukkit.getPlayer(late.uniqueId) }.thenReturn(late)
            CinematicRuntime.restore(first, returnToOrigin = true)
            CinematicRuntime.restore(late, returnToOrigin = true)
        }
        verify(first).scoreboard = firstBoard
        verify(late).scoreboard = lateBoard
        verify(late).teleport(lateOrigin)
        verify(first).showPlayer(Core.instance, late)
        verify(late).showPlayer(Core.instance, first)
    }

    private fun player(): Player = mock(Player::class.java).also {
        `when`(it.uniqueId).thenReturn(UUID.randomUUID())
        `when`(it.location).thenReturn(Location(null, 1.0, 2.0, 3.0))
        `when`(it.gameMode).thenReturn(GameMode.SURVIVAL)
        `when`(it.scoreboard).thenReturn(mock(Scoreboard::class.java))
        `when`(it.activePotionEffects).thenReturn(emptyList())
    }
}
