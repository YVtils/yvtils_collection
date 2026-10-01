package yv.tils.regionsv2.logic

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.language.LangStrings
import yv.tils.regionsv2.language.RegionFailure

object ClaimCurrency {
    fun price(bounds: ClaimBounds): Long =
        ClaimPricing.price(bounds.sides[0].toLong(), bounds.sides[2].toLong(), ConfigFile.state)

    fun credit(claim: Claim): Long = claim.metadata.currencyCredit ?: price(
        ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint)
    )

    fun quote(sender: CommandSender, bounds: ClaimBounds, credit: Long = 0): Long =
        if (!ConfigFile.state.currencyEnabled || ClaimService.admin(sender)) 0 else ClaimPricing.due(
            price(bounds),
            credit
        )

    /** Main-thread transaction: restore the exact storage contents if persistence fails. */
    fun <T> pay(sender: CommandSender, amount: Long, operation: () -> T): T {
        if (amount == 0L) return operation()
        check(sender is Player)
        val inventory = sender.inventory
        val snapshot = inventory.storageContents.map { it?.clone() }.toTypedArray()
        val available = snapshot.filterNotNull().filter { it.type == Material.DIAMOND }.sumOf { it.amount.toLong() }
        if (available < amount) throw RegionFailure(
            LangStrings.INSUFFICIENT_CURRENCY,
            mapOf("cost" to amount, "available" to available)
        )
        var remaining = amount
        val updated = snapshot.map { it?.clone() }.toTypedArray()
        for (index in updated.indices) {
            val stack = updated[index] ?: continue
            if (stack.type != Material.DIAMOND) continue
            val taken = minOf(remaining, stack.amount.toLong()).toInt()
            stack.amount -= taken
            remaining -= taken
            if (stack.amount == 0) updated[index] = null
            if (remaining == 0L) break
        }
        inventory.storageContents = updated
        try {
            return operation()
        } catch (e: Exception) {
            inventory.storageContents = snapshot
            throw e
        }
    }
}
