package yv.tils.regionsv2.logic

import com.sk89q.worldguard.protection.flags.StateFlag
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regionsv2.language.*
import yv.tils.regionsv2.language.LangStrings.*
import yv.tils.utils.message.MessageUtils
import java.time.Instant

object ClaimInformation {
    fun valueName(sender: CommandSender, value: Any?): String {
        val string = when (value) {
            null -> UNSET; true -> YES; false -> NO; StateFlag.State.ALLOW -> ALLOW; StateFlag.State.DENY -> DENY; else -> null
        }
        return if (string == null) value.toString().take(100) else MessageUtils.strip(
            string.message().component(sender)
        )
    }

    fun roleName(sender: CommandSender, role: ClaimRole?): String = MessageUtils.strip(
        when (role) {
            ClaimRole.OWNER -> ROLE_OWNER; ClaimRole.MEMBER -> ROLE_MEMBER; ClaimRole.VISITOR -> ROLE_VISITOR; null -> GLOBAL
        }.message().component(sender)
    )

    fun messages(sender: CommandSender, claim: Claim): List<RegionMessage> {
        val r = claim.region
        fun names(ids: Collection<java.util.UUID>) =
            ids.joinToString(", ") { Bukkit.getOfflinePlayer(it).name ?: it.toString() }.ifEmpty { "—" }

        val result = mutableListOf(
            INFO_NAME.message("region" to claim.name),
            INFO_UUID.message("uuid" to claim.uuid),
            INFO_WORLD.message("world" to claim.world.name),
            INFO_CORNERS.message("corners" to "${r.minimumPoint.x()}, ${r.minimumPoint.z()} → ${r.maximumPoint.x()}, ${r.maximumPoint.z()}"),
            INFO_AREA.message(
                "area" to (r.maximumPoint.x().toLong() - r.minimumPoint.x() + 1) * (r.maximumPoint.z()
                    .toLong() - r.minimumPoint.z() + 1)
            ),
            INFO_CREATED.message("created" to Instant.ofEpochMilli(claim.metadata.created)),
            INFO_OWNERS.message("owners" to names(r.owners.uniqueIds)),
            INFO_MEMBERS.message("members" to names(r.members.uniqueIds)),
            INFO_PLAYERS.message("count" to ClaimOccupancy.playersIn(claim.uuid).size)
        )
        if (sender is Player) result.add(
            1,
            INFO_ROLE.message("role" to roleName(sender, ClaimService.role(claim, sender.uniqueId)))
        )
        for (role in listOf(null, ClaimRole.OWNER, ClaimRole.MEMBER)) {
            ClaimService.target(claim, role).flags.entries.filterNot { ClaimFlags.locked(it.key) }
                .forEach { (flag, value) ->
                    val scope = role ?: if (ClaimFlags.policy(flag).roleBased) ClaimRole.VISITOR else null
                    result += INFO_FLAG.message(
                        "scope" to roleName(sender, scope),
                        "flag" to flag.name,
                        "value" to valueName(sender, value)
                    )
                }
        }
        return result
    }

    fun lines(sender: CommandSender, claim: Claim): List<Component> =
        messages(sender, claim).map { it.component(sender) }

    fun send(sender: CommandSender, claim: Claim) {
        messages(sender, claim).forEach { RegionText.send(sender, it) }
    }
}
