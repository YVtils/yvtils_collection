package yv.tils.vanish

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.vanish.configs.VanishSettings
import yv.tils.vanish.permissions.VanishPermissions

class VanishSettingsTest {
    @Test
    fun `personal capability toggles do not change unrelated preferences`() {
        val defaults = VanishSettings()
        val edited = defaults.toggle(VanishPermissions.SILENT_CONTAINERS)
        assertFalse(edited.enabled(VanishPermissions.SILENT_CONTAINERS))
        assertTrue(edited.enabled(VanishPermissions.PICKUP))
        assertTrue(edited.announce)
        assertEquals(defaults, edited.toggle(VanishPermissions.SILENT_CONTAINERS))
        assertFalse(defaults.enabled("unknown"))
    }
}
