/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.logic.crafting

/** Integral max flow: overlapping ingredient alternatives cannot double-count inventory slots. */
object IngredientAllocation {
    data class Plan(val consumed: IntArray, val missing: IntArray) {
        val complete get() = missing.all { it == 0 }
    }

    fun plan(available: IntArray, required: IntArray, matches: (Int, Int) -> Boolean): Plan {
        require(available.all { it >= 0 } && required.all { it >= 0 })
        val source = 0
        val slots = 1
        val demands = slots + available.size
        val sink = demands + required.size
        val capacity = Array(sink + 1) { IntArray(sink + 1) }
        available.forEachIndexed { i, count -> capacity[source][slots + i] = count }
        required.forEachIndexed { i, count -> capacity[demands + i][sink] = count }
        available.indices.forEach { slot ->
            required.indices.forEach { demand ->
                if (matches(slot, demand)) capacity[slots + slot][demands + demand] = available[slot]
            }
        }
        val residual = Array(capacity.size) { capacity[it].clone() }
        while (true) {
            val parents = IntArray(capacity.size) { -1 }
            parents[source] = source
            val queue = ArrayDeque<Int>()
            queue.add(source)
            while (queue.isNotEmpty() && parents[sink] == -1) {
                val node = queue.removeFirst()
                residual[node].indices.forEach { next ->
                    if (parents[next] == -1 && residual[node][next] > 0) {
                        parents[next] = node
                        queue.add(next)
                    }
                }
            }
            if (parents[sink] == -1) break
            var amount = Int.MAX_VALUE
            var node = sink
            while (node != source) {
                amount = minOf(amount, residual[parents[node]][node]); node = parents[node]
            }
            node = sink
            while (node != source) {
                val parent = parents[node]
                residual[parent][node] -= amount
                residual[node][parent] += amount
                node = parent
            }
        }
        return Plan(
            IntArray(available.size) { available[it] - residual[source][slots + it] },
            IntArray(required.size) { residual[demands + it][sink] })
    }
}
