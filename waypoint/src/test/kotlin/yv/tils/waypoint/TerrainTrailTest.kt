package yv.tils.waypoint

import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import yv.tils.waypoint.logic.TerrainTrail

class TerrainTrailTest {
    @Test
    fun `unloaded terrain is never read or force loaded`() {
        val world = mock(World::class.java)
        `when`(world.minHeight).thenReturn(-64)
        `when`(world.maxHeight).thenReturn(320)
        `when`(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(false)
        assertTrue(TerrainTrail.blocks(Location(world, 0.0, 65.0, 0.0), Location(world, 1000.0, 65.0, 0.0)).isEmpty())
        verify(world, never()).getBlockAt(anyInt(), anyInt(), anyInt())
        verify(world, never()).getChunkAt(anyInt(), anyInt())
    }

    @Test
    fun `arrows are relative to yaw including multiple turns`() {
        assertEquals("↑", TerrainTrail.direction(0f, 0.0, 10.0))
        assertEquals("→", TerrainTrail.direction(0f, -10.0, 0.0))
        assertEquals("←", TerrainTrail.direction(0f, 10.0, 0.0))
        assertEquals("↓", TerrainTrail.direction(0f, 0.0, -10.0))
        assertEquals("↑", TerrainTrail.direction(720f, 0.0, 10.0))
    }
}
