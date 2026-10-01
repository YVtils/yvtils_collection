package yv.tils.regions.logic

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.language.LangStrings
import yv.tils.regions.language.RegionFailure
import java.util.UUID

object ClaimCurrency {
    fun price(bounds: ClaimBounds): Long =
        ClaimPricing.price(bounds.sides[0].toLong(), bounds.sides[2].toLong(), ConfigFile.state)

    fun credit(claim: Claim): Long = claim.metadata.currencyCredit ?: price(
        ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint)
    )

    data class Payment(val due: Long, val resultingCredit: Long)

    fun plan(
        sender: CommandSender,
        bounds: ClaimBounds,
        owners: Set<UUID>,
        replaced: List<Claim> = emptyList()
    ): Payment {
        val excluded = replaced.map { it.uuid }.toSet()
        val ownCredit = replaced.fold(0L) { total, claim -> Math.addExact(total, credit(claim)) }
        fun footprint(id: UUID, bounds: ClaimBounds, owners: Set<UUID>, credit: Long) = ClaimClusters.Footprint(
            id, bounds.world.uid, owners, bounds.min.x(), bounds.min.z(), bounds.max.x(), bounds.max.z(), credit
        )

        val candidate = footprint(replaced.firstOrNull()?.uuid ?: UUID.randomUUID(), bounds, owners, ownCredit)
        val existing = ClaimService.claims(bounds.world).filter { it.uuid !in excluded }.map { claim ->
            footprint(
                claim.uuid, ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint),
                claim.region.owners.uniqueIds, credit(claim)
            )
        }
        val quote = ClaimClusters.quote(candidate, existing, ConfigFile.state)
        val exempt = !ConfigFile.state.currencyEnabled || yv.tils.regions.data.Permissions.BYPASS_COST.allowed(sender)
        // Credit only the newly covered deficit to the changed claim; neighbouring credits remain untouched.
        return Payment(if (exempt) 0 else quote.due, Math.addExact(ownCredit, quote.due))
    }

    fun quote(sender: CommandSender, bounds: ClaimBounds): Long {
        check(sender is Player)
        return plan(sender, bounds, setOf(sender.uniqueId)).due
    }

    fun resizeQuote(sender: CommandSender, claim: Claim, bounds: ClaimBounds): Long =
        plan(sender, bounds, claim.region.owners.uniqueIds, listOf(claim)).due

    fun mergeQuote(sender: CommandSender, keep: Claim, other: Claim, bounds: ClaimBounds): Long =
        plan(sender, bounds, keep.region.owners.uniqueIds, listOf(keep, other)).due

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
