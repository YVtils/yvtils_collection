/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.*
import dev.jorel.commandapi.wrappers.Location2D
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.common.other.AsyncActionAnnounce
import yv.tils.regions.data.*
import yv.tils.regions.logic.*
import yv.tils.utils.coroutine.CoroutineHandler
import java.util.concurrent.CompletableFuture

@Suppress("UNCHECKED_CAST")
class RegionCommand {
    /**
     * Command to manage regions.
     * /region <subcommand>
     *     subcommand: The subcommand to execute. [String]
     *
     *     subcommands:
     *     - manage: Open the region management GUI.
     *     - create: Create a new region.
     *     - delete: Delete a region.
     *     - info: Get information about a region.
     *     - list: List all regions.
     *     - members: Manage members of a region.
     *     - flags: Manage flags of a region.
     */
    val command = commandTree("regions") {
        withPermission("yvtils.command.regions")
        withPermission(CommandPermission.NONE)
        withAliases("rg", "region", "claim")
        withUsage("/region <subcommand>")

        /**
         * Command to open the region management GUI.
         * /region [manage]
         */
//        literalArgument("manage", true) {
//            playerExecutor { sender, _ ->
//                sender.sendMessage("Not implemented yet")
//                // TODO: Implement GUI
//            }
//        }

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
                location2DArgument("corner1", LocationType.BLOCK_POSITION) {
                    location2DArgument("corner2", LocationType.BLOCK_POSITION) {
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

        /**
         * Command to delete a region.
         * /region delete <regionName>
         *     regionName: The name of the region. [String]
         */
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

        /**
         * Command to get information about a region.
         * /region info <regionName>
         *     regionName: The name of the region. [String]
         */
        literalArgument("info") {
            withPermission(Permissions.REGION_INFO.permission)
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

        /**
         * Command to list all regions.
         * /region list [role] [player]
         *     role: The role of the player in the region. [RegionRoles]
         *     player: The player to list regions for. [Player]
         */
        literalArgument("list", false) {
            withPermission(Permissions.REGION_LIST.permission)
            withPermission(CommandPermission.NONE)

            val literals = RegionRoles.entries.map { it.name }.toTypedArray()

            multiLiteralArgument("role", *literals, optional = true) {
                withPermission(Permissions.REGION_LIST_ROLE.permission)
                withPermission(CommandPermission.NONE)

                entitySelectorArgumentManyPlayers("yv/tils/player", optional = true) {
                    withPermission(Permissions.REGION_LIST_OTHER.permission)
                    withPermission(CommandPermission.OP)

                    anyExecutor { sender, args ->
                        val player = args["yv/tils/player"] as Collection<*>?
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

        /**
         * Command to manage a member of a region.
         * /region members ...
         */
        literalArgument("members", false) {
            withPermission(Permissions.REGION_MEMBER.permission)
            withPermission(CommandPermission.NONE)

            /**
             * Command to add a member to a region.
             * /region members add <regionName> <player> [role]
             *     regionName: The name of the region. [String]
             *     player: The player to add. [Player]
             *     role: The role of the player. [RegionRoles]
             */
            literalArgument("add") {
                withPermission(Permissions.REGION_MEMBER.permission)
                withPermission(CommandPermission.NONE)

                textArgument("regionName", false) {
                    replaceSuggestions(ArgumentSuggestions.strings { sender ->
                        getRegions(sender.sender, RegionRoles.MODERATOR)
                    })

                    asyncPlayerProfileArgument("yv/tils/player", false) {
                        withPermission(Permissions.REGION_MEMBER_ADD.permission)
                        withPermission(CommandPermission.OP)

                        val literals = RegionRoles.entries.map { it.name }.toTypedArray()

                        multiLiteralArgument("role", *literals, optional = true) {
                            withPermission(Permissions.REGION_MEMBER_ROLE.permission)
                            withPermission(CommandPermission.NONE)

                            anyExecutor { sender, args ->
                                val regionName = args["regionName"] as String
                                val player = args["yv/tils/player"] as CompletableFuture<OfflinePlayer>
                                val role = args["role"] as String?

                                AsyncActionAnnounce.announceAction(sender)

                                player.thenAccept { offlinePlayer ->
                                    CoroutineHandler.launchTask(
                                        suspend {
                                            MemberLogic.addPlayerToRegion(
                                                regionName,
                                                offlinePlayer,
                                                role,
                                                sender,
                                            )
                                        },
                                        null,
                                        isOnce = true,
                                    )
                                }.exceptionally { throwable ->
                                    val cause = throwable.cause
                                    val rootCause = if (cause is RuntimeException) cause.cause else cause

                                    sender.sendMessage(rootCause?.message ?: "An error occurred")
                                    null
                                }
                            }
                        }
                    }
                }
            }

            /**
             * Command to remove a member from a region.
             * /region members remove <regionName> <player>
             *     regionName: The name of the region. [String]
             *     player: The player to remove. [Player]
             */
            literalArgument("remove") {
                withPermission(Permissions.REGION_MEMBER.permission)
                withPermission(CommandPermission.NONE)

                textArgument("regionName", false) {
                    replaceSuggestions(ArgumentSuggestions.strings { sender ->
                        getRegions(sender.sender, RegionRoles.MODERATOR)
                    })

                    asyncPlayerProfileArgument("yv/tils/player", false) {
                        withPermission(Permissions.REGION_MEMBER_REMOVE.permission)
                        withPermission(CommandPermission.OP)

                        anyExecutor { sender, args ->
                            val regionName = args["regionName"] as String
                            val player = args["yv/tils/player"] as CompletableFuture<OfflinePlayer>

                            AsyncActionAnnounce.announceAction(sender)

                            player.thenAccept { offlinePlayer ->
                                CoroutineHandler.launchTask(
                                    suspend {
                                        MemberLogic.removePlayerFromRegion(
                                            regionName,
                                            offlinePlayer,
                                            sender,
                                        )
                                    },
                                    null,
                                    isOnce = true,
                                )
                            }.exceptionally { throwable ->
                                val cause = throwable.cause
                                val rootCause = if (cause is RuntimeException) cause.cause else cause

                                sender.sendMessage(rootCause?.message ?: "An error occurred")
                                null
                            }
                        }
                    }
                }
            }

            /**
             * Command to change the role of a member in a region.
             * /region members role <regionName> <player> <role>
             *     regionName: The name of the region. [String]
             *     player: The player to change the role for. [Player]
             *     role: The new role of the player. [RegionRoles]
             */
            literalArgument("role") {
                withPermission(Permissions.REGION_MEMBER.permission)
                withPermission(CommandPermission.NONE)

                textArgument("regionName", false) {
                    replaceSuggestions(ArgumentSuggestions.strings { sender ->
                        getRegions(sender.sender, RegionRoles.MODERATOR)
                    })

                    asyncPlayerProfileArgument("yv/tils/player", false) {
                        withPermission(Permissions.REGION_MEMBER_ROLE.permission)
                        withPermission(CommandPermission.OP)

                        val literals = RegionRoles.entries.map { it.name }.toTypedArray()

                        multiLiteralArgument("role", *literals, optional = true) {
                            withPermission(Permissions.REGION_MEMBER_ROLE.permission)
                            withPermission(CommandPermission.NONE)

                            anyExecutor { sender, args ->
                                val regionName = args["regionName"] as String
                                val player = args["yv/tils/player"] as CompletableFuture<OfflinePlayer>
                                val role = args["role"] as String?

                                AsyncActionAnnounce.announceAction(sender)

                                player.thenAccept { offlinePlayer ->
                                    CoroutineHandler.launchTask(
                                        suspend {
                                            MemberLogic.changePlayerRoleInRegion(
                                                regionName,
                                                offlinePlayer,
                                                sender,
                                                role,
                                            )
                                        },
                                        null,
                                        isOnce = true,
                                    )
                                }.exceptionally { throwable ->
                                    val cause = throwable.cause
                                    val rootCause = if (cause is RuntimeException) cause.cause else cause

                                    sender.sendMessage(rootCause?.message ?: "An error occurred")
                                    null
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Command to manage region flags.
         * /region flags ...
         */
        literalArgument("flags", false) {
            withPermission(Permissions.REGION_FLAGS.permission)
            withPermission(CommandPermission.NONE)

            val flags = Flag.entries.map { it.name }.toTypedArray()

            /**
             * Command to manage global flags.
             * /region flags global <flagType> <enabled> [regionName]
             *     flagType: The type of flag. [FlagType]
             *     enabled: Whether the flag is enabled or disabled. [Boolean]
             *     regionName: The name of the region. [String]
             */
            multiLiteralArgument("global_flags", *flags) {
                withPermission(Permissions.REGION_FLAGS_GLOBAL.permission)
                withPermission(CommandPermission.NONE)

                booleanArgument("enabled", false) {
                    textArgument("regionName", true) {
                        replaceSuggestions(ArgumentSuggestions.strings { sender ->
                            getRegions(sender.sender, RegionRoles.MODERATOR)
                        })

                        anyExecutor { sender, args ->
                            val flagType = args["global_flags"] as String
                            val enabled = args["enabled"] as Boolean
                            val regionName = args["regionName"] as String?

                            CoroutineHandler.launchTask(
                                suspend {
                                    FlagLogic.changeFlag(sender,
                                        regionName,
                                        flagType,
                                        enabled,
                                        null,
                                        )
                                },
                                null,
                                isOnce = true,
                            )
                        }
                    }
                }
            }

            /**
             * Command to manage role-based flags.
             * /region flags role <flagType> <role> [regionName]
             *     flagType: The type of flag. [FlagType]
             *     role: The role of the player. [RegionRoles]
             *     regionName: The name of the region. [String]
             */
            multiLiteralArgument("role_flags", *flags) {
                withPermission(Permissions.REGION_FLAGS_ROLE.permission)
                withPermission(CommandPermission.NONE)

                val literals = RegionRoles.entries.map { it.name }.toTypedArray()

                multiLiteralArgument("role", *literals) {
                    withPermission(Permissions.REGION_FLAGS.permission)
                    withPermission(CommandPermission.NONE)

                    textArgument("regionName", true) {
                        replaceSuggestions(ArgumentSuggestions.strings { sender ->
                            getRegions(sender.sender, RegionRoles.MODERATOR)
                        })

                        anyExecutor { sender, args ->
                            val flagType = args["role_flags"] as String
                            val role = args["role"] as String
                            val regionName = args["regionName"] as String?

                            CoroutineHandler.launchTask(
                                suspend {
                                    FlagLogic.changeFlag(
                                        sender,
                                        regionName,
                                        flagType,
                                        null,
                                        RegionRoles.valueOf(role),
                                    )
                                },
                                null,
                                isOnce = true,
                            )
                        }
                    }
                }
            }
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
