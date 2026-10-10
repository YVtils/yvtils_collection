package yv.tils.remadeEnderDragon.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FightMathTest {
    @Test
    fun `effective damage and healing preserve units behind capped attribute`() {
        val conversion = FightMath.healthConversion(1024.0, 13800.0)
        val displayed = 1024.0 * 0.5
        assertEquals(6890.0, (displayed - 10.0 * conversion) / conversion, 0.000001)
        assertEquals(6901.0, (displayed + conversion) / conversion, 0.000001)
        assertEquals(1.0, FightMath.healthConversion(260.0, 260.0))
    }

    @Test
    fun `breath sector wraps across negative pi and leaves safe sides and center`() {
        assertTrue(FightMath.inSweep(-10.0, -0.1, Math.PI - 0.01, 24.0, 12.0))
        assertFalse(FightMath.inSweep(10.0, 0.0, Math.PI, 24.0, 12.0))
        assertFalse(FightMath.inSweep(0.0, 10.0, 0.0, 24.0, 12.0))
        assertFalse(FightMath.inSweep(25.0, 0.0, 0.0, 24.0, 12.0))
        assertFalse(FightMath.inSweep(3.0, 0.0, 0.0, 24.0, 12.0))
        assertTrue(FightMath.inSweep(4.0, 0.0, 0.0, 24.0, 12.0))
    }

    @Test
    fun `long encounter health scales for five to fifteen mixed gear fighters`() {
        assertEquals(4800.0, FightMath.health(5, 20, 1200.0, 900.0, 18000.0))
        assertEquals(13800.0, FightMath.health(15, 20, 1200.0, 900.0, 18000.0))
        assertEquals(18000.0, FightMath.health(100, 20, 1200.0, 900.0, 18000.0))
    }

    @Test
    fun `partial piercing preserves some protection without amplifying unarmored damage`() {
        assertEquals(-3.2, FightMath.piercedReduction(-8.0, 0.6))
        assertEquals(-8.0, FightMath.piercedReduction(-8.0, 0.0))
        assertEquals(0.0, FightMath.piercedReduction(-8.0, 1.0), 0.000001)
        assertEquals(0.0, FightMath.piercedReduction(0.0, 0.6))
        assertEquals(2.0, FightMath.piercedReduction(2.0, 0.6))
    }

    @Test
    fun `fast wave checks entire travelled band not just current radius`() {
        assertTrue(FightMath.crossedWave(3.0, 1.8, 3.6, 9.0))
        assertTrue(FightMath.crossedWave(0.2, 0.0, 1.8, 9.0))
        assertFalse(FightMath.crossedWave(0.5, 3.6, 5.4, 9.0))
        assertFalse(FightMath.crossedWave(7.0, 3.6, 5.4, 9.0))
        assertFalse(FightMath.crossedWave(9.1, 8.5, 9.0, 9.0))
    }

    @Test
    fun `scaling clamps empty parties and oversized groups`() {
        assertEquals(260.0, FightMath.health(0, 20, 260.0, 100.0, 2200.0))
        assertEquals(360.0, FightMath.health(2, 20, 260.0, 100.0, 2200.0))
        assertEquals(2160.0, FightMath.health(200, 20, 260.0, 100.0, 2200.0))
        assertEquals(1000.0, FightMath.health(20, 20, 260.0, 100.0, 1000.0))
        assertEquals(2, FightMath.count(0, 20, 2, 0.5, 12))
        assertEquals(3, FightMath.count(2, 20, 2, 0.5, 12))
        assertEquals(5, FightMath.count(100, 20, 1, 0.5, 5))
    }

    @Test
    fun `magnet landing retains small damage but cannot kill`() {
        assertEquals(3.0, FightMath.landingDamage(15.0, 3.0, 20.0))
        assertEquals(1.0, FightMath.landingDamage(1.0, 3.0, 20.0))
        assertEquals(0.5, FightMath.landingDamage(15.0, 3.0, 1.5))
        assertEquals(0.0, FightMath.landingDamage(15.0, 3.0, 0.5))
        assertEquals(0.0, FightMath.landingDamage(0.0, 3.0, 20.0))
    }

    @Test
    fun `enderman cone is ninety degrees and radius bounded`() {
        assertTrue(FightMath.inCone(0.0, 4.0, 0.0, 1.0, 6.0))
        assertTrue(FightMath.inCone(3.0, 3.0, 0.0, 1.0, 6.0))
        assertTrue(FightMath.inCone(-3.0, 3.0, 0.0, 1.0, 6.0))
        assertFalse(FightMath.inCone(4.0, 1.0, 0.0, 1.0, 6.0))
        assertFalse(FightMath.inCone(0.0, -3.0, 0.0, 1.0, 6.0))
        assertFalse(FightMath.inCone(0.0, 7.0, 0.0, 1.0, 6.0))
    }
}
