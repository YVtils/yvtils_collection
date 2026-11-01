package yv.tils.yv_smp.logic.start

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.RenderType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class DeathCounterTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun initializeCriteria() {
            // Paper resolves these constants through the server when Criteria is first loaded.
            mockStatic(Bukkit::class.java).use { bukkit ->
                bukkit.`when`<Criteria> { Bukkit.getScoreboardCriteria(anyString()) }.thenAnswer {
                    TestCriteria(it.getArgument(0))
                }
                assertEquals("deathCount", Criteria.DEATH_COUNT.name)
            }
        }
    }

    private data class TestCriteria(private val id: String) : Criteria {
        override fun getName(): String = id
        override fun isReadOnly(): Boolean = false
        override fun getDefaultRenderType(): RenderType = RenderType.INTEGER
    }

    @Test
    fun `both displays are seeded including zero deaths without replacing scoreboards or teams`() {
        val scoreboard = mock(Scoreboard::class.java)
        val tab = mock(Objective::class.java)
        val below = mock(Objective::class.java)
        `when`(scoreboard.registerNewObjective(eq("yvsmp_deaths"), eq(Criteria.DEATH_COUNT), any(Component::class.java)))
            .thenReturn(tab)
        `when`(scoreboard.registerNewObjective(eq("yvsmp_deaths_name"), eq(Criteria.DEATH_COUNT), any(Component::class.java)))
            .thenReturn(below)
        val alice = player("Alice", 0)
        val bob = player("Bob", 7)
        val scores = listOf(tab, below).flatMap { objective ->
            listOf(alice, bob).map { player ->
                val score = mock(Score::class.java)
                `when`(objective.getScore(player.name)).thenReturn(score)
                score to player.getStatistic(Statistic.DEATHS)
            }
        }

        DeathCounter.configure(scoreboard, listOf(alice, bob))

        verify(tab).displaySlot = DisplaySlot.PLAYER_LIST
        verify(below).displaySlot = DisplaySlot.BELOW_NAME
        scores.forEach { (score, deaths) -> verify(score).score = deaths }
        verify(alice, never()).scoreboard = any(Scoreboard::class.java)
        verify(bob, never()).scoreboard = any(Scoreboard::class.java)
        verify(scoreboard, never()).registerNewTeam(anyString())
    }

    @Test
    fun `existing objectives are reused on reconnect`() {
        val scoreboard = mock(Scoreboard::class.java)
        val tab = mock(Objective::class.java)
        val below = mock(Objective::class.java)
        `when`(scoreboard.getObjective("yvsmp_deaths")).thenReturn(tab)
        `when`(scoreboard.getObjective("yvsmp_deaths_name")).thenReturn(below)
        DeathCounter.configure(scoreboard, emptyList())
        verify(scoreboard, never()).registerNewObjective(anyString(), any(Criteria::class.java), any(Component::class.java))
        verify(tab).displaySlot = DisplaySlot.PLAYER_LIST
        verify(below).displaySlot = DisplaySlot.BELOW_NAME
    }

    private fun player(name: String, deaths: Int): Player = mock(Player::class.java).also {
        `when`(it.name).thenReturn(name)
        `when`(it.getStatistic(Statistic.DEATHS)).thenReturn(deaths)
    }
}
