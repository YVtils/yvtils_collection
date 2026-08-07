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

package yv.tils.essentials.commands.handler

import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerPortalEvent
import yv.tils.configv2.language.LanguageHandler
import yv.tils.essentials.config.StatesFile
import yv.tils.essentials.language.LangStrings
import yv.tils.essentials.permissions.Permissions
import yv.tils.utils.logger.Logger

class DimensionHandler {
    fun setDimensionState(sender: CommandSender, world: World, state: Boolean?) {
        val currentState = getDimensionState(world)

        if (state == null) {
            setDimensionState(sender, world, !currentState)
            return
        }

        if (state == currentState) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_DIMENSION_ALREADY,
                    sender,
                    mapOf("dimension" to world.name)
                )
            )
            return
        }

        StatesFile().updateDimensionState(world.name, state)

        sender.sendMessage(
            LanguageHandler.getMessage(
                if (state) LangStrings.COMMAND_DIMENSION_ENABLED else LangStrings.COMMAND_DIMENSION_DISABLED,
                sender,
                mapOf("dimension" to world.name)
            )
        )

        if (sender is Player) {
            Logger.info("Player ${sender.name} set dimension travel for world ${world.name} to ${if (state) "enabled" else "disabled"}.")
        }
    }

    fun checkDimensionState(sender: CommandSender, world: World) {
        val state = getDimensionState(world)

        if (state) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_DIMENSION_STATE_ENABLED,
                    sender,
                    mapOf("dimension" to world.name)
                )
            )
        } else {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.COMMAND_DIMENSION_STATE_DISABLED,
                    sender,
                    mapOf("dimension" to world.name)
                )
            )
        }
    }

    fun onTravelAttempt(e: PlayerPortalEvent) {
        // TODO: Add bypass perm

        val player = e.player
        val fromWorld = e.from.world
        val toWorld = e.to.world ?: return

        if (fromWorld == toWorld) return
        if (player.hasPermission(Permissions.BYPASS_DIMENSION_BLOCK.permission.name)) return

        if (!getDimensionState(toWorld)) {
            e.isCancelled = true
            player.sendActionBar(
                LanguageHandler.getMessage(
                    LangStrings.DIMENSION_TRAVEL_DISABLED,
                    player,
                    mapOf("dimension" to toWorld.name)
                )
            )
        }
    }

    /**
     * Checks if dimension travel is allowed for the specified world.
     * @param world The world the player is changing to
     * @return `true` if dimension travel is allowed for the specified world, `false` if it is denied
     */
    fun getDimensionState(world: World): Boolean {
        return StatesFile.state.dimensions[world.name] ?: true
    }
}