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

package yv.tils.regionsv2.commands

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import yv.tils.configv2.language.LanguageHandler
import yv.tils.regionsv2.data.Permissions


/**
 * Example command scaffolded by scripts/new-module.sh - replace with real subcommands, or
 * delete this file if the module ends up not needing this particular one.
 */
class RegionsV2Command {
    val command = commandTree("regionsv2") {
        withPermission(Permissions.EXAMPLE.permission.name)
        withUsage("/regionsv2")

        playerExecutor { player, _ ->
            player.sendMessage(
                LanguageHandler.getMessage("command.regionsv2.example", player)
            )
        }

        // Very basic check to sanity-test the WorldGuard hook - lists the WorldGuard region IDs
        // applicable to the executing player's current location. This subcommand only exists
        // once the module is enabled, which only happens when WorldGuard is present (see
        // RegionsV2YVtils.enablePlugin), so the direct API calls below are always safe here.
        literalArgument("worldguard") {
            withPermission(Permissions.WORLDGUARD.permission.name)

            playerExecutor { player, _ ->
                val regionIds = getApplicableRegionIds(player.location)

                val message = if (regionIds.isEmpty()) {
                    LanguageHandler.getMessage("command.regionsv2.worldguard.none", player)
                } else {
                    LanguageHandler.getMessage(
                        "command.regionsv2.worldguard.found",
                        player,
                        mapOf("regions" to regionIds.joinToString(", "))
                    )
                }

                player.sendMessage(message)
            }
        }
    }

    /**
     * The IDs of every WorldGuard region applicable to [location], or an empty list if the
     * location's world has no region manager (e.g. region support is disabled for that world).
     */
    private fun getApplicableRegionIds(location: org.bukkit.Location): List<String> {
        val regionContainer = WorldGuard.getInstance().platform.regionContainer
        val weWorld = BukkitAdapter.adapt(location.world)
        val regionManager = regionContainer.get(weWorld) ?: return emptyList()

        return regionManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(location)).toList()
    }
}
