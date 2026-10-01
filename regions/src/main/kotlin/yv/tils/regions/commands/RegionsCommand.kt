/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regions.data.Permissions
import yv.tils.regions.gui.ClaimsGui
import yv.tils.regions.language.LangStrings.*
import yv.tils.regions.language.RegionText
import yv.tils.regions.language.message
import yv.tils.regions.logic.*

class RegionsCommand {
    private fun names(sender: CommandSender, owned: Boolean): Array<String> = ClaimService.all().filter {
        !owned || ClaimService.admin(sender) || sender is Player && ClaimService.role(
            it,
            sender.uniqueId
        ) == ClaimRole.OWNER
    }.flatMap { listOf(it.uuid.toString(), it.name).filter { value -> !value.contains(' ') } }.distinct().toTypedArray()

    private fun claim(sender: CommandSender, name: String?): Claim = if (name == null) {
        check(sender is Player) { CONSOLE_CLAIM.key }
        ClaimService.at(sender.location) ?: error(NO_CLAIM_HERE.key)
    } else ClaimService.resolve(name, if (sender is Player) sender.world else null)

    private fun completed(sender: CommandSender) = RegionText.send(sender, COMPLETED.message())

    val command = commandTree("region") {
        withAliases("regions")
        withPermission(Permissions.MANAGE.permission.name)
        anyExecutor { sender, _ ->
            if (sender is Player) ClaimsGui.open(sender) else
                RegionText.send(sender, HELP.message())
        }
        for (corner in listOf("pos1", "pos2")) literalArgument(corner) {
            withPermission(Permissions.SELECT.permission.name)
            playerExecutor { player, _ ->
                RegionText.action(player) {
                    ClaimSelection.select(
                        player,
                        corner == "pos1"
                    ); RegionText.send(player, POSITION_SET.message("position" to if (corner == "pos1") 1 else 2))
                }
            }
        }
        literalArgument("preview") {
            withPermission(Permissions.PREVIEW.permission.name)
            playerExecutor { player, _ ->
                RegionText.action(player) {
                    ClaimSelection.preview(
                        player
                    ); RegionText.send(player, PREVIEW_STARTED.message())
                }
            }
        }
        literalArgument("clear") {
            playerExecutor { player, _ ->
                ClaimSelection.clear(player.uniqueId); RegionText.send(
                player,
                SELECTION_CLEARED.message()
            )
            }
        }
        literalArgument("cost") {
            playerExecutor { player, _ ->
                RegionText.action(player) {
                    val bounds = ClaimSelection.bounds(player)
                    RegionText.send(player, COST.message("cost" to ClaimCurrency.quote(player, bounds)))
                }
            }
        }
        literalArgument("create") {
            withPermission(Permissions.CLAIM.permission.name)
            stringArgument("name") {
                playerExecutor { player, args ->
                    RegionText.action(player) {
                        ClaimsGui.details(
                            player,
                            ClaimService.create(player, args["name"] as String)
                        )
                    }
                }
                integerArgument("x1", -29999984, 29999984) {
                    integerArgument("z1", -29999984, 29999984) {
                        integerArgument("x2", -29999984, 29999984) {
                            integerArgument("z2", -29999984, 29999984) {
                                playerExecutor { player, args ->
                                    RegionText.action(player) {
                                        val bounds = ClaimService.bounds(
                                            player.world,
                                            args["x1"] as Int,
                                            args["z1"] as Int,
                                            args["x2"] as Int,
                                            args["z2"] as Int
                                        )
                                        ClaimsGui.details(
                                            player,
                                            ClaimService.create(player, args["name"] as String, player.uniqueId, bounds)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Explicit owner/world makes this suitable for console administration too.
        literalArgument("createat") {
            withPermission(Permissions.ADMIN_CREATE.permission.name)
            stringArgument("world") {
                replaceSuggestions(ArgumentSuggestions.strings { Bukkit.getWorlds().map { it.name }.toTypedArray() })
                stringArgument("owner") {
                    stringArgument("name") {
                        integerArgument("x1", -29999984, 29999984) {
                            integerArgument("z1", -29999984, 29999984) {
                                integerArgument("x2", -29999984, 29999984) {
                                    integerArgument("z2", -29999984, 29999984) {
                                        anyExecutor { sender, args ->
                                            RegionText.action(sender) {
                                                val world = Bukkit.getWorld(args["world"] as String)
                                                    ?: error(WORLD_NOT_FOUND.key)
                                                val bounds = ClaimService.bounds(
                                                    world,
                                                    args["x1"] as Int,
                                                    args["z1"] as Int,
                                                    args["x2"] as Int,
                                                    args["z2"] as Int
                                                )
                                                PlayerProfiles.resolve(sender, args["owner"] as String) { uuid ->
                                                    Permissions.ADMIN_CREATE.require(sender)
                                                    ClaimInformation.send(
                                                        sender,
                                                        ClaimService.create(
                                                            sender,
                                                            args["name"] as String,
                                                            uuid,
                                                            bounds
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        literalArgument("info") {
            withPermission(Permissions.INFO.permission.name)
            anyExecutor { sender, _ ->
                RegionText.action(sender) {
                    ClaimInformation.send(
                        sender,
                        claim(sender, null)
                    )
                }
            }
            stringArgument("claim") {
                replaceSuggestions(ArgumentSuggestions.strings { names(it.sender, false) })
                anyExecutor { sender, args ->
                    RegionText.action(sender) {
                        ClaimInformation.send(
                            sender,
                            claim(sender, args["claim"] as String)
                        )
                    }
                }
            }
        }
        literalArgument("list") {
            withPermission(Permissions.LIST.permission.name)
            anyExecutor { sender, _ ->
                RegionText.action(sender) {
                    ClaimService.all().forEach {
                        RegionText.send(
                            sender,
                            LIST_LINE.message("region" to it.name, "uuid" to it.uuid, "world" to it.world.name)
                        )
                    }
                }
            }
        }
        literalArgument("manage") {
            stringArgument("claim") {
                replaceSuggestions(ArgumentSuggestions.strings { names(it.sender, false) })
                playerExecutor { player, args ->
                    RegionText.action(player) {
                        ClaimsGui.details(
                            player,
                            claim(player, args["claim"] as String)
                        )
                    }
                }
            }
        }
        for (operation in listOf("delete", "rename", "resize", "merge", "role", "flag", "message")) literalArgument(
            operation
        ) {
            when (operation) {
                "delete" -> withPermission(Permissions.DELETE.permission.name)
                "rename" -> withPermission(Permissions.RENAME.permission.name)
                "resize" -> withPermission(Permissions.RESIZE.permission.name)
                "merge" -> withPermission(Permissions.MERGE.permission.name)
                "flag" -> withPermission(Permissions.FLAGS_EDIT.permission.name)
                "message" -> withPermission(Permissions.ADMIN_MESSAGES.permission.name)
            }
            stringArgument("claim") {
                replaceSuggestions(ArgumentSuggestions.strings { names(it.sender, true) })
                when (operation) {
                    "delete" -> anyExecutor { sender, args ->
                        RegionText.action(sender) {
                            ClaimService.delete(
                                sender,
                                claim(sender, args["claim"] as String)
                            ); completed(sender)
                        }
                    }

                    "rename" -> greedyStringArgument("name") {
                        anyExecutor { sender, args ->
                            RegionText.action(sender) {
                                ClaimService.rename(
                                    sender,
                                    claim(sender, args["claim"] as String),
                                    args["name"] as String
                                ); completed(sender)
                            }
                        }
                    }

                    "resize" -> integerArgument("x1", -29999984, 29999984) {
                        integerArgument("z1", -29999984, 29999984) {
                            integerArgument("x2", -29999984, 29999984) {
                                integerArgument("z2", -29999984, 29999984) {
                                    anyExecutor { sender, args ->
                                        RegionText.action(sender) {
                                            val target = claim(sender, args["claim"] as String)
                                            ClaimService.resize(
                                                sender,
                                                target,
                                                ClaimService.bounds(
                                                    target.world,
                                                    args["x1"] as Int,
                                                    args["z1"] as Int,
                                                    args["x2"] as Int,
                                                    args["z2"] as Int
                                                )
                                            ); completed(sender)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "merge" -> stringArgument("other") {
                        replaceSuggestions(ArgumentSuggestions.strings { names(it.sender, true) })
                        anyExecutor { sender, args ->
                            RegionText.action(sender) {
                                ClaimService.merge(
                                    sender,
                                    claim(sender, args["claim"] as String),
                                    claim(sender, args["other"] as String)
                                ); completed(sender)
                            }
                        }
                    }

                    "role" -> stringArgument("player") {
                        replaceSuggestions(ArgumentSuggestions.strings {
                            Bukkit.getOnlinePlayers().map { it.name }.toTypedArray()
                        })
                        stringArgument("role") {
                            replaceSuggestions(ArgumentSuggestions.strings(*ClaimRole.entries.map { it.name }
                                .toTypedArray()))
                            anyExecutor { sender, args ->
                                RegionText.action(sender) {
                                    val target = claim(sender, args["claim"] as String)
                                    ClaimService.requireOwner(sender, target)
                                    val role =
                                        ClaimRole.entries.firstOrNull { it.name.equals(args["role"] as String, true) }
                                            ?: error(INVALID_ROLE.key)
                                    PlayerProfiles.resolve(
                                        sender,
                                        args["player"] as String
                                    ) { uuid ->
                                        ClaimService.setRole(
                                            sender,
                                            target,
                                            uuid,
                                            role
                                        ); RegionText.send(sender, ROLE_UPDATED.message())
                                    }
                                }
                            }
                        }
                    }

                    "flag" -> stringArgument("flag") {
                        replaceSuggestions(ArgumentSuggestions.strings {
                            ClaimFlags.all().filterNot(ClaimFlags::locked).map { it.name }.toTypedArray()
                        })
                        stringArgument("scope") {
                            replaceSuggestions(ArgumentSuggestions.strings("GLOBAL", "OWNER", "MEMBER", "VISITOR"))
                            greedyStringArgument("value") {
                                anyExecutor { sender, args ->
                                    RegionText.action(sender) {
                                        val target = claim(sender, args["claim"] as String)
                                        ClaimService.requireOwner(sender, target)
                                        val flag =
                                            ClaimFlags.all().firstOrNull { it.name == args["flag"] as String } ?: error(
                                                UNKNOWN_FLAG.key
                                            )
                                        val scope = args["scope"] as String
                                        val role = if (scope.equals(
                                                "GLOBAL",
                                                true
                                            )
                                        ) null else ClaimRole.entries.firstOrNull { it.name.equals(scope, true) }
                                            ?: error(INVALID_ROLE.key)
                                        ClaimFlags.set(
                                            sender,
                                            target,
                                            flag,
                                            role,
                                            ClaimFlags.parse(sender, flag, args["value"] as String)
                                        ); completed(sender)
                                    }
                                }
                            }
                        }
                    }

                    "message" -> stringArgument("type") {
                        withPermission(Permissions.ADMIN_MESSAGES.permission.name)
                        replaceSuggestions(ArgumentSuggestions.strings("welcome", "goodbye"))
                        greedyStringArgument("text") {
                            anyExecutor { sender, args ->
                                RegionText.action(sender) {
                                    val type = args["type"] as String
                                    check(type in listOf("welcome", "goodbye")) { INVALID_INPUT.key }
                                    val text = args["text"] as String
                                    ClaimService.messages(
                                        sender,
                                        claim(sender, args["claim"] as String),
                                        type == "welcome",
                                        if (text == "unset") "" else text
                                    ); completed(sender)
                                }
                            }
                        }
                    }
                }
            }
        }
        literalArgument("admin") {
            playerExecutor { player, _ ->
                ClaimsGui.admin(
                    player
                )
            }
        }
        literalArgument("worldguard") {
            withPermission(Permissions.WORLDGUARD.permission.name)
            playerExecutor { player, _ ->
                RegionText.action(player) {
                    RegionText.send(
                        player,
                        REGIONS.message("regions" to Regions.applicableRegionIds(player.location).joinToString(", "))
                    )
                }
            }
        }
    }
}
