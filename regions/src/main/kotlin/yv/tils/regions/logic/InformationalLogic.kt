package yv.tils.regions.logic

import language.LanguageHandler
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regions.data.PlayerManager
import yv.tils.regions.data.RegionManager
import yv.tils.regions.data.RegionRoles
import yv.tils.regions.language.LangStrings
import java.util.*

class InformationalLogic {
    companion object {
        fun getRegionInfoAsMessage(sender: CommandSender, regionName: String? = null) {
            var regionName = regionName
            if (regionName == null && sender is Player) {
                regionName = RegionLogic.getRegion(sender.location)?.name

                if (regionName == null) {
                    sender.sendMessage(LanguageHandler.getMessage(LangStrings.REGION_GENERIC_NONE.key, sender))
                    return
                }
            } else if (regionName == null) {
                sender.sendMessage(LanguageHandler.getMessage(LangStrings.REGION_INFO_FAIL_GENERIC.key, sender))
                return
            }

            val regionInfo: RegionInfo?

            when (RegionLogic().getRegionListSize(regionName)) {
                0 ->  {
                    sender.sendMessage(LanguageHandler.getMessage(LangStrings.REGION_GENERIC_NONE.key, sender))
                    return
                }
                1 -> {
                    val region = RegionManager.getRegions(regionName)
                    val rUUID = UUID.fromString(region[0].id)

                    regionInfo = InformationalLogic().getRegionInfo(rUUID, sender)

                    if (regionInfo == null) {
                        sender.sendMessage(LanguageHandler.getMessage(LangStrings.REGION_GENERIC_NONE.key, sender))
                        return
                    }
                }
                else -> {
                    val regions = RegionManager.getRegions(regionName)
                    val regionList: MutableList<String> = mutableListOf()
                    for (region in regions) {
                        val owner = PlayerManager.getRegionOwner(UUID.fromString(region.id))
                        regionList.add("<click:suggest_command:/rg remove ${region.id}>${region.name}: ${region.id} (${owner})</click>")
                    }

                    sender.sendMessage(LanguageHandler.getMessage(
                        LangStrings.REGION_GENERIC_MULTIPLE.key,
                        sender,
                        mapOf<String, Any>(
                            "regions" to regionList
                        )
                    ))

                    return
                }
            }

            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.REGION_INFO_SUCCESS.key,
                    sender,
                    mapOf(
                        "name" to regionInfo.name,
                        "world" to regionInfo.world,
                        "location1" to regionInfo.location1,
                        "location2" to regionInfo.location2,
                        "owner" to regionInfo.owner,
                        "role" to regionInfo.role.toString(),
                        "members" to regionInfo.members,
                        "created" to regionInfo.created,
                        "flags" to regionInfo.flags
                    )
                )
            )
        }

        fun listRegionsAsMessage(sender: CommandSender, targets: Collection<*>?, role: String?) {
            val regions = InformationalLogic().getRegionList(sender, targets, role)

            if (regions.isEmpty()) {
                sender.sendMessage(LanguageHandler.getMessage(LangStrings.REGION_GENERIC_NONE.key, sender))
                return
            }

            val lines: MutableList<String> = mutableListOf()
            for (region in regions) {
                lines.add(
                    LanguageHandler.getRawMessage(
                        LangStrings.REGION_LIST_LINE.key,
                        sender,
                        mapOf(
                            "name" to region.name,
                            "id" to region.id,
                            "owner" to region.owner
                        )
                    )
                )
            }

            val message = LanguageHandler.getMessage(
                LangStrings.REGION_LIST_SUCCESS.key,
                sender,
                mapOf(
                    "lines" to lines.joinToString("<newline>"),
                )
            )

            sender.sendMessage(message)
        }
    }

    /**
     * Gets the region information for a given region UUID.
     *
     * @param rUUID The UUID of the region.
     * @param sender The command sender (used for permissions).
     * @return RegionInfo object containing region information, or null if the region does not exist.
     */
    fun getRegionInfo(rUUID: UUID, sender: CommandSender): RegionInfo? {
        val region = RegionManager.getRegion(rUUID) ?: return null

        val senderRole = if (sender !is Player) {
            RegionRoles.OWNER
        } else {
            PlayerChecks.regionRole(sender, region)
        }

        val regionOwner = PlayerManager.getRegionOwnerAsPlayer(rUUID)
        val regionMembers = PlayerManager.getRegionMembersAsPlayers(rUUID)

        val creationDate = java.text.SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(Date(region.created))

        val regionInfo = RegionInfo (
            name = region.name,
            world = region.world,
            location1 = "${region.x}, ${region.z}",
            location2 = "${region.x2}, ${region.z2}",
            owner = regionOwner?.name ?: "-",
            role = senderRole,
            members = regionMembers?.joinToString(", ") { it.name ?: "-" } ?: "-",
            created = creationDate,
            flags = ""
        )

        if (senderRole.permLevel <= RegionRoles.MEMBER.permLevel && senderRole != RegionRoles.NONE) {
            var flags = ""

            for (flag in region.flags.global) {
                flags += "${flag.key}: ${flag.value}, "
            }

            for (flag in region.flags.roleBased) {
                flags += "${flag.key}: ${RegionRoles.fromID(flag.value)}, "
            }

            regionInfo.flags = flags
        } else {
            regionInfo.flags = LanguageHandler.getRawMessage(LangStrings.REGION_INFO_FLAGS_NOT_ALLOWED.key, sender)
        }

        return regionInfo
    }

    fun getRegionList(sender: CommandSender, targets: Collection<*>?, role: String?): List<RegionList> {
        val targetList: MutableList<Player> = mutableListOf()
        val role = RegionRoles.fromString(role ?: "")

        if ((targets == null || targets.isEmpty()) && sender is Player) {
            targetList.add(sender)
        } else if (targets == null || targets.isEmpty()) {
            sender.sendMessage(LanguageHandler.getMessage(
                yv.tils.common.language.LangStrings.COMMAND_MISSING_PLAYER.key,
                sender
            ))
            return emptyList()
        } else {
            for (target in targets) {
                if (target is Player) {
                    targetList.add(target)
                }
            }
        }

        val regions: MutableList<RegionList> = mutableListOf()
        for (target in targetList) {
            val pRegions = RegionManager.getRegions(target, role)
            if (pRegions.isEmpty()) continue
            for (region in pRegions) {
                regions.add(
                    RegionList(
                        name = region.name,
                        id = region.id,
                        owner = target.name
                    )
                )
            }
        }

        return regions
    }

    data class RegionInfo (
        val name: String,
        val world: String,
        val location1: String,
        val location2: String,
        val owner: String,
        val role: RegionRoles,
        val members: String,
        val created: String,
        var flags: String
    )

    data class RegionList (
        val name: String,
        val id: String,
        val owner: String
    )
}