package yv.tils.multiMine.utils

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.UUID

class ChainedBlockBreakTest {
    @Test
    fun `denied player break is not treated as a successful chained break`() {
        val player = mock(Player::class.java)
        val block = mock(Block::class.java)
        val uuid = UUID.randomUUID()
        `when`(player.uniqueId).thenReturn(uuid)
        `when`(player.breakBlock(block)).thenAnswer {
            assertTrue(ChainedBlockBreak.isChained(uuid))
            assertFalse(ChainedBlockBreak.breakBlock(player, block))
            false
        }
        assertFalse(ChainedBlockBreak.breakBlock(player, block))
        assertFalse(ChainedBlockBreak.isChained(uuid))
        verify(player, times(1)).breakBlock(block)
    }

    @Test
    fun `throwing break releases recursion guard`() {
        val player = mock(Player::class.java)
        val block = mock(Block::class.java)
        val uuid = UUID.randomUUID()
        `when`(player.uniqueId).thenReturn(uuid)
        `when`(player.breakBlock(block)).thenThrow(IllegalStateException("protection failure"))
        assertThrows(IllegalStateException::class.java) { ChainedBlockBreak.breakBlock(player, block) }
        assertFalse(ChainedBlockBreak.isChained(uuid))
    }
}
