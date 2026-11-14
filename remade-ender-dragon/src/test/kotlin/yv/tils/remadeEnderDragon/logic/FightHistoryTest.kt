/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.util.NamingSchemes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FightHistoryTest {
    private val mapper = ObjectMapper.factoryBuilder().addDiscoverer(dataClassFieldDiscoverer())
        .defaultNamingScheme(NamingSchemes.PASSTHROUGH).build().get(FightHistory::class.java)

    @Test
    fun `old history retains world completions without inventing a dragon death timestamp`() {
        val node = BasicConfigurationNode.root()
        node.node("completedWorlds").setList(String::class.java, listOf("end-world"))
        val loaded = mapper.load(node)
        assertEquals(listOf("end-world"), loaded.completedWorlds)
        assertNull(loaded.lastDragonDeathAtMillis)
    }

    @Test
    fun `the latest dragon death timestamp roundtrips together with existing world history`() {
        val expected = FightHistory(listOf("first-end", "second-end"), 1_794_695_200_000L)
        val node = BasicConfigurationNode.root()
        mapper.save(expected, node)
        assertEquals(expected, mapper.load(node))
        assertEquals(expected.lastDragonDeathAtMillis, node.node("lastDragonDeathAtMillis").long)
    }
}
