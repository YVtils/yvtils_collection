/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.essentials.inventory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InventoryLayoutTest {
    @Test
    fun `every target slot appears exactly once and decorations cannot edit inventory`() {
        InventoryKind.entries.forEach { kind ->
            val targets = (0 until kind.viewSize).mapNotNull(kind::targetSlot)
            assertEquals((0 until kind.slots).toList(), targets.sorted())
            assertNull(kind.targetSlot(-999))
            assertNull(kind.targetSlot(kind.viewSize))
        }
        (listOf(0, 1, 6, 8).plusElement((36..44))).forEach { assertNull(InventoryKind.PLAYER.targetSlot(it)) }
        (0..8).forEach { assertNull(InventoryKind.ENDER_CHEST.targetSlot(it)) }
    }

    @Test
    fun `player layout follows helmet to boots then storage and separated hotbar`() {
        val kind = InventoryKind.PLAYER
        assertEquals(listOf(39, 38, 37, 36), (2..5).map(kind::targetSlot))
        assertEquals(40, kind.targetSlot(7))
        assertEquals((9..35).toList(), (9..35).map(kind::targetSlot))
        assertEquals((0..8).toList(), (45..53).map(kind::targetSlot))
    }
}
