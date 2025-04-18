package yv.tils.regions.commands

import coroutine.CoroutineHandler
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.*
import dev.jorel.commandapi.wrappers.Location2D
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regions.data.Permissions
import yv.tils.regions.data.RegionManager
import yv.tils.regions.data.RegionRoles
import yv.tils.regions.logic.InformationalLogic
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
            //withPermission(Permissions.REGION_INFO.permission) TODO
            withPermission(CommandPermission.NONE)

            textArgument("regionName", true) {
                replaceSuggestions(ArgumentSuggestions.strings { sender ->
                    getRegions(sender.sender)
                })

                anyExecutor { sender, args ->
                    val regionName = args["regionName"] as String?

                    CoroutineHandler.launchTask(
                        suspend {
                            InformationalLogic.getRegionInfoAsMessage(sender, regionName)
                        },
                        null,
                        isOnce = true,
                    )
                }
            }
        }

        // /regions list <role> <player>
        literalArgument("list", false) {
            //withPermission(Permissions.REGION_LIST_SELF.permission) TODO
            withPermission(CommandPermission.NONE)
            // Command to list all regions

            val literals = RegionRoles.entries.map { it.name }.toTypedArray()

            multiLiteralArgument("role", *literals, optional = true) {
                //withPermission(Permissions.REGION_LIST_OTHER.permission) TODO
                withPermission(CommandPermission.NONE)

                entitySelectorArgumentManyPlayers("player", optional = true) {
                    //withPermission(Permissions.REGION_LIST_OTHER.permission) TODO
                    withPermission(CommandPermission.OP)

                    anyExecutor { sender, args ->
                        val player = args["player"] as Collection<*>?
                        val role = args["role"] as String?

                        CoroutineHandler.launchTask(
                            suspend {
                                InformationalLogic.listRegionsAsMessage(sender, player, role)
                            },
                            null,
                            isOnce = true,
                        )
                    }
                }
            }
        }

        literalArgument("player") {
            // Command to manage players in a region
        }
    }

    private fun getRegions(sender: CommandSender, role: RegionRoles = RegionRoles.NONE): Array<String> {
        if (sender !is Player) {
            // List all regions
            val regions = RegionManager.getAllRegions()
            return regions.map { it.name }.toTypedArray()
        }

        // List regions owned by the player
        return RegionManager.getRegions(sender, role).map { it.name }.toTypedArray()
    }
}