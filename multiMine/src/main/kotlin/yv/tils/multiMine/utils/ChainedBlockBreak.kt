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

package yv.tils.multiMine.utils

import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.*

/** Paper's player break path enforces cancellation, drops, experience and tool damage. */
object ChainedBlockBreak {
    private val players = mutableSetOf<UUID>()
    fun isChained(uuid: UUID) = uuid in players

    fun breakBlock(player: Player, block: Block): Boolean {
        return players.add(player.uniqueId) && try {
            player.breakBlock(block)
        } finally {
            players.remove(player.uniqueId)
        }
    }
}
