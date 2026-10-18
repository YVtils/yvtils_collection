package yv.tils.vanish

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VanishPolicyTest {
    @Test
    fun `ordinary players cannot see vanished staff and hierarchy remains directional`() {
        assertTrue(VanishPolicy.visible(0, 0))
        assertFalse(VanishPolicy.visible(0, 1))
        assertTrue(VanishPolicy.visible(2, 1))
        assertFalse(VanishPolicy.visible(1, 2))
        assertTrue(VanishPolicy.visible(2, 2))
        assertTrue(VanishPolicy.visible(100, 0))
    }
}
