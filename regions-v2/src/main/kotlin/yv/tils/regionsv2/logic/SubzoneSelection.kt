package yv.tils.regionsv2.logic

import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.entity.Player
import yv.tils.regionsv2.language.LangStrings.*
import java.util.UUID

/** Independent 3D selection; does not replace the player's full-height claim selection. */
object SubzoneSelection {
    private data class Selection(val claim: UUID, var first: BlockVector3? = null, var second: BlockVector3? = null)

    private val selections = mutableMapOf<UUID, Selection>()

    fun select(player: Player, claim: Claim, first: Boolean) {
        yv.tils.regionsv2.data.Permissions.SUBZONES_CREATE.require(player)
        ClaimService.requireOwner(player, claim)
        check(player.world == claim.world) { SAME_WORLD.key }
        val at = BlockVector3.at(player.location.blockX, player.location.blockY, player.location.blockZ)
        check(
            SubzonePolicies.contained(
                at,
                at,
                claim.region.minimumPoint,
                claim.region.maximumPoint
            )
        ) { SUBZONE_OUTSIDE.key }
        val selection = selections[player.uniqueId]?.takeIf { it.claim == claim.uuid }
            ?: Selection(claim.uuid).also { selections[player.uniqueId] = it }
        if (first) selection.first = at else selection.second = at
    }

    fun description(player: Player, claim: Claim, first: Boolean): String? {
        val selection = selections[player.uniqueId]?.takeIf { it.claim == claim.uuid } ?: return null
        return (if (first) selection.first else selection.second)?.let { "${it.x()}, ${it.y()}, ${it.z()}" }
    }

    fun bounds(player: Player, claim: Claim): ClaimBounds {
        val selection = selections[player.uniqueId]?.takeIf { it.claim == claim.uuid } ?: error(SUBZONE_SELECT.key)
        val a = selection.first ?: error(SUBZONE_SELECT.key)
        val b = selection.second ?: error(SUBZONE_SELECT.key)
        return ClaimBounds(
            claim.world, BlockVector3.at(minOf(a.x(), b.x()), minOf(a.y(), b.y()), minOf(a.z(), b.z())),
            BlockVector3.at(maxOf(a.x(), b.x()), maxOf(a.y(), b.y()), maxOf(a.z(), b.z()))
        )
    }

    fun clear(player: UUID) {
        selections.remove(player)
    }

    fun shutdown() {
        selections.clear()
    }
}
