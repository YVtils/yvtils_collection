package yv.tils.regions.logic

import com.sk89q.worldedit.math.BlockVector3
import yv.tils.regions.language.LangStrings

object ClaimGeometry {
    /** Require the union itself to be a rectangle: never claim intervening land silently. */
    fun mergeBounds(
        a: BlockVector3,
        b: BlockVector3,
        c: BlockVector3,
        d: BlockVector3
    ): Pair<BlockVector3, BlockVector3> {
        val min = BlockVector3.at(minOf(a.x(), c.x()), minOf(a.y(), c.y()), minOf(a.z(), c.z()))
        val max = BlockVector3.at(maxOf(b.x(), d.x()), maxOf(b.y(), d.y()), maxOf(b.z(), d.z()))
        fun area(l: BlockVector3, h: BlockVector3) = (h.x().toLong() - l.x() + 1) * (h.z().toLong() - l.z() + 1)
        val ix = (minOf(b.x(), d.x()).toLong() - maxOf(a.x(), c.x()) + 1).coerceAtLeast(0)
        val iz = (minOf(b.z(), d.z()).toLong() - maxOf(a.z(), c.z()) + 1).coerceAtLeast(0)
        check(
            area(min, max) == area(a, b) + area(
                c,
                d
            ) - ix * iz
        ) { LangStrings.MERGE_GEOMETRY.key }
        return min to max
    }
}
