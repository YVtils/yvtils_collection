package yv.tils.regions.commands

import coroutine.CoroutineHandler
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.*
import dev.jorel.commandapi.wrappers.Location2D
import org.bukkit.entity.Player
import yv.tils.regions.data.Permissions
import yv.tils.regions.data.RegionManager
import yv.tils.regions.data.RegionRoles
import yv.tils.regions.logic.RegionLogic

class RegionCommand {
    val command = commandTree("regions") {
        withPermission("yvtils.command.regions")
        withPermission(CommandPermission.NONE)
        withAliases("rg", "region", "claim")
        withUsage("/region <subcommand>")

        literalArgument("manage", true) {
            // Default command if no subcommand is provided
            // Opens GUI for management
        }

        /**
         * Command to create a region.
         * /region create <regionName> <corner1> <corner2>
         *     regionName: The name of the region. [String]
         *     corner1: The first corner of the region. [Location2D]
         *     corner2: The second corner of the region. [Location2D]
         */
        literalArgument("create", false) {
            withPermission(Permissions.REGION_CREATE.permission)
            withPermission(CommandPermission.NONE)
            textArgument("regionName", false) {
                location2DArgument("corner1", LocationType.BLOCK_POSITION, false) {
                    location2DArgument("corner2", LocationType.BLOCK_POSITION, false) {
                        playerExecutor { player, args ->
                            val regionName = args["regionName"] as String
                            val corner1 = args["corner1"] as Location2D
                            val corner2 = args["corner2"] as Location2D

                            CoroutineHandler.launchTask(
                                suspend { RegionLogic().registerRegion(player, regionName, corner1, corner2) },
                                null,
                                isOnce = true,
                            )
                        }
                    }
                }
            }
        }

        literalArgument("delete") {
            withPermission(Permissions.REGION_DELETE.permission)
            withPermission(CommandPermission.NONE)
            textArgument("regionName", false) {
                replaceSuggestions(ArgumentSuggestions.strings { sender ->
                    if (sender.sender !is Player) {
                        // List all regions
                        val regions = RegionManager.getAllRegions()
                        return@strings regions.map { it.name }.toTypedArray()
                    }

                    // List regions owned by the player
                    RegionManager.getRegions(sender.sender as Player, RegionRoles.OWNER).map { it.name }.toTypedArray()
                })
                anyExecutor { sender, args ->
                    val regionName = args["regionName"] as String

                    CoroutineHandler.launchTask(
                        suspend {
                            RegionLogic().removeRegion(sender, regionName)
                        },
                        null,
                        isOnce = true,
                    )
                }
            }
        }

        literalArgument("info") {
            // Command to get information about a region
        }

        literalArgument("list") {
            // Command to list all regions
        }

        literalArgument("player") {
            // Command to manage players in a region
        }
    }
}