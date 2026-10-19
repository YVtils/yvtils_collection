package yv.tils.waypoint

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class WaypointPolicyTest {
    private val owner = UUID.randomUUID()
    private val visitor = UUID.randomUUID()

    @Test
    fun `shared waypoints are visible only to explicitly selected players and owner`() {
        val outsider = UUID.randomUUID()
        val recipients = setOf(visitor)
        assertTrue(WaypointPolicy.listed(owner, visitor, Visibility.SHARED, recipients))
        assertTrue(WaypointPolicy.accessible(owner, visitor, Visibility.SHARED, recipients))
        assertFalse(WaypointPolicy.listed(owner, outsider, Visibility.SHARED, recipients))
        assertFalse(WaypointPolicy.accessible(owner, outsider, Visibility.SHARED, recipients))
        assertTrue(WaypointPolicy.accessible(owner, owner, Visibility.SHARED))
        assertFalse(WaypointPolicy.accessible(owner, visitor, Visibility.PRIVATE, recipients))
        assertFalse(WaypointPolicy.accessible(owner, visitor, Visibility.SHARED, emptySet()))
    }

    @Test
    fun `private coordinates remain owner only`() {
        assertTrue(WaypointPolicy.listed(owner, owner, Visibility.PRIVATE))
        assertTrue(WaypointPolicy.accessible(owner, owner, Visibility.PRIVATE))
        assertFalse(WaypointPolicy.listed(owner, visitor, Visibility.PRIVATE))
        assertFalse(WaypointPolicy.accessible(owner, visitor, Visibility.PRIVATE))
    }

    @Test
    fun `unlisted coordinates are navigable without leaking through listings`() {
        assertTrue(WaypointPolicy.listed(owner, owner, Visibility.UNLISTED))
        assertFalse(WaypointPolicy.listed(owner, visitor, Visibility.UNLISTED))
        assertTrue(WaypointPolicy.accessible(owner, visitor, Visibility.UNLISTED))
        assertTrue(WaypointPolicy.listed(owner, visitor, Visibility.PUBLIC))
        assertTrue(WaypointPolicy.accessible(owner, visitor, Visibility.PUBLIC))
    }
}
