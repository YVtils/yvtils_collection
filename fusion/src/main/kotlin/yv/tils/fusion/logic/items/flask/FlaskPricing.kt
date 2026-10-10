/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.logic.items.flask

/** Pure raw-XP pricing rules, independent of Bukkit item/interaction handling. */
object FlaskPricing {
    data class Saturation(val amount: Float, val cost: Int)

    fun saturation(foodLevel: Int, current: Float, points: Int, xpPerPoint: Int, perUse: Int): Saturation {
        require(foodLevel in 0..20 && current.isFinite() && points >= 0 && xpPerPoint > 0 && perUse >= 0)
        val amount = minOf((foodLevel - current).coerceAtLeast(0f), perUse.toFloat(), (points / xpPerPoint).toFloat())
        return Saturation(amount, kotlin.math.ceil(amount.toDouble() * xpPerPoint).toInt())
    }
    data class Feeding(val food: Int, val cost: Int)

    fun quote(foodLevel: Int, points: Int, xpPerPoint: Int, foodPerUse: Int): Feeding {
        require(foodLevel in 0..20 && points >= 0 && xpPerPoint > 0 && foodPerUse > 0)
        val food = minOf(20 - foodLevel, foodPerUse, points / xpPerPoint)
        return Feeding(food, food * xpPerPoint)
    }
}
