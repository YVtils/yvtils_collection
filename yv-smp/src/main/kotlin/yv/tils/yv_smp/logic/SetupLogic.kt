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

package yv.tils.yv_smp.logic

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.modules.Core
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.logic.start.PhaseHandler
import yv.tils.yv_smp.utils.IslandScanner

/**
 * Backing logic for `/yvsmp setup`. Bundles the one-off preparation steps an
 * admin typically needs before running `/yvsmp start` - resetting the world
 * border, recentering it and world spawn, and clearing the cached island
 * outline scan so it picks up any recent terrain edits.
 */
class SetupLogic {
    private val borderHandler = BorderHandler()

    /**
     * Sets the world border size (and, optionally, a transition time) for all worlds.
     */
    fun setupBorder(sender: CommandSender, size: Double, time: Long = 0) {
        borderHandler.changeBorderSize(size, time)

        sender.sendMessage(
            LanguageHandler.getMessage(
                LangStrings.SETUP_BORDER_SUCCESS,
                sender,
                mapOf("size" to size.toString(), "time" to time.toString())
            )
        )
    }

    /**
     * Centers the world border of every world on the sender's current location.
     * Player-only, since a console has no meaningful location.
     */
    fun setupBorderCenter(sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage(LanguageHandler.getMessage(LangStrings.SETUP_PLAYER_ONLY, sender))
            return false
        }

        borderHandler.setBorderCenter(sender.location)

        sender.sendMessage(
            LanguageHandler.getMessage(
                LangStrings.SETUP_BORDERCENTER_SUCCESS,
                sender,
                mapOf("x" to sender.location.blockX.toString(), "z" to sender.location.blockZ.toString())
            )
        )
        return true
    }

    /**
     * Sets the current world's spawn location to the sender's current location.
     * Player-only, since a console has no meaningful location.
     */
    fun setupSpawn(sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage(LanguageHandler.getMessage(LangStrings.SETUP_PLAYER_ONLY, sender))
            return false
        }

        sender.world.setSpawnLocation(sender.location)

        sender.sendMessage(LanguageHandler.getMessage(LangStrings.SETUP_SPAWN_SUCCESS, sender))
        return true
    }

    /**
     * Clears the cached island edge scan so the next cinematic phase re-scans
     * the (potentially edited) terrain instead of reusing stale data.
     */
    fun setupRescan(sender: CommandSender) {
        IslandScanner.clearCache()

        sender.sendMessage(LanguageHandler.getMessage(LangStrings.SETUP_RESCAN_SUCCESS, sender))
    }

    /**
     * Runs the full setup routine: resets every world's border to a small
     * default size centered on its own spawn point, and clears the island
     * scan cache. Intended as a single "reset everything before we start"
     * command.
     *
     * @param defaultBorderSize The size the border is reset to (default: 500 blocks).
     */
    fun setupAll(sender: CommandSender, defaultBorderSize: Double = 500.0) {
        borderHandler.setupDefaultBorder(defaultBorderSize)
        IslandScanner.clearCache()

        sender.sendMessage(LanguageHandler.getMessage(LangStrings.SETUP_ALL_SUCCESS, sender))
    }

    /**
     * Prints the current border/spawn/run status for every world plus whether
     * a start sequence is currently active.
     */
    fun status(sender: CommandSender) {
        sender.sendMessage(LanguageHandler.getMessage(LangStrings.SETUP_STATUS_TITLE, sender))

        Core.instance.server.worlds.forEach { world ->
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.SETUP_STATUS_BORDER,
                    sender,
                    mapOf("world" to world.name, "status" to borderHandler.getBorderStatus(world))
                )
            )

            val spawn = world.spawnLocation
            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.SETUP_STATUS_SPAWN,
                    sender,
                    mapOf(
                        "world" to world.name,
                        "x" to spawn.blockX.toString(),
                        "y" to spawn.blockY.toString(),
                        "z" to spawn.blockZ.toString(),
                    )
                )
            )
        }

        sender.sendMessage(
            LanguageHandler.getMessage(
                LangStrings.SETUP_STATUS_RUNNING,
                sender,
                mapOf("status" to PhaseHandler.isRunning.toString())
            )
        )
    }
}
