package yv.tils.regions.commands

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RegionAliasTest {
    private fun routesToWorldGuard(granted: Set<String>, effective: Set<String> = granted) =
        RegionAlias.hasWorldGuardRights({ it in granted }, effective)

    @Test
    fun `ordinary claim permissions and bypass rights do not select WorldGuard commands`() {
        assertFalse(routesToWorldGuard(setOf("yvtils.regions.worldguard", "yvtils.regions.admin")))
        assertFalse(routesToWorldGuard(setOf("worldguard.region.bypass.world")))
    }

    @Test
    fun `granted region permissions and wildcards select WorldGuard`() {
        assertTrue(routesToWorldGuard(setOf("worldguard.region.info.own.*")))
        assertTrue(routesToWorldGuard(setOf("worldguard.region.*")))
        assertTrue(routesToWorldGuard(setOf("worldguard.*")))
    }

    @Test
    fun `effective permissions must still be currently granted`() {
        assertFalse(routesToWorldGuard(emptySet(), setOf("worldguard.region.info.*", "worldguard.region.*")))
    }
}
