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

package yv.tils.yv_smp.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.*
import yv.tils.yv_smp.gui.YvSmpGui
import yv.tils.yv_smp.logic.SetupLogic
import yv.tils.yv_smp.logic.StartLogic
import yv.tils.yv_smp.permissions.Permissions

/**
 * Merged command for managing the YV SMP session lifecycle.
 * Usage:
 * - /yvsmp - Opens the GUI control menu (players only)
 * - /yvsmp gui - Same as above, explicit
 * - /yvsmp start - Runs the cinematic SMP start sequence
 * - /yvsmp stop - Aborts a currently running start sequence
 * - /yvsmp setup border <size> [time] - Sets the world border size
 * - /yvsmp setup bordercenter - Centers the world border on your location
 * - /yvsmp setup spawn - Sets world spawn to your location
 * - /yvsmp setup rescan - Clears the cached island edge scan
 * - /yvsmp setup all [size] - Resets border + spawn cache in one go
 * - /yvsmp setup status - Shows current border/spawn/run status
 */
class YVSmpCommand {

    init {
        commandTree("yvsmp") {
            withPermission(CommandPermission.NONE)
            withUsage("/yvsmp <gui|start|stop|setup>")

            // /yvsmp (no args) - open the GUI for players, show usage otherwise
            playerExecutor { player, _ ->
                if (player.hasPermission(Permissions.COMMAND_GUI.permission.name)) {
                    YvSmpGui().openMainMenu(player)
                }
            }

            // /yvsmp gui
            literalArgument("gui") {
                withPermission(Permissions.COMMAND_GUI.permission.name)
                playerExecutor { player, _ ->
                    YvSmpGui().openMainMenu(player)
                }
            }

            // /yvsmp start
            literalArgument("start") {
                withPermission(Permissions.COMMAND_START.permission.name)
                anyExecutor { sender, _ ->
                    StartLogic().start(sender)
                }
            }

            // /yvsmp stop
            literalArgument("stop") {
                withPermission(Permissions.COMMAND_START_STOP.permission.name)
                anyExecutor { sender, _ ->
                    StartLogic().stop(sender)
                }
            }

            // /yvsmp setup ...
            literalArgument("setup") {
                withPermission(Permissions.COMMAND_SETUP.permission.name)

                // /yvsmp setup border <size> [time]
                literalArgument("border") {
                    withPermission(Permissions.COMMAND_SETUP_BORDER.permission.name)
                    doubleArgument("size", min = 1.0) {
                        anyExecutor { sender, args ->
                            val size = args["size"] as Double
                            SetupLogic().setupBorder(sender, size)
                        }

                        longArgument("time", min = 0) {
                            anyExecutor { sender, args ->
                                val size = args["size"] as Double
                                val time = args["time"] as Long
                                SetupLogic().setupBorder(sender, size, time)
                            }
                        }
                    }
                }

                // /yvsmp setup bordercenter
                literalArgument("bordercenter") {
                    withPermission(Permissions.COMMAND_SETUP_BORDERCENTER.permission.name)
                    playerExecutor { player, _ ->
                        SetupLogic().setupBorderCenter(player)
                    }
                }

                // /yvsmp setup spawn
                literalArgument("spawn") {
                    withPermission(Permissions.COMMAND_SETUP_SPAWN.permission.name)
                    playerExecutor { player, _ ->
                        SetupLogic().setupSpawn(player)
                    }
                }

                // /yvsmp setup rescan
                literalArgument("rescan") {
                    withPermission(Permissions.COMMAND_SETUP_RESCAN.permission.name)
                    anyExecutor { sender, _ ->
                        SetupLogic().setupRescan(sender)
                    }
                }

                // /yvsmp setup all [size]
                literalArgument("all") {
                    withPermission(Permissions.COMMAND_SETUP_ALL.permission.name)
                    anyExecutor { sender, _ ->
                        SetupLogic().setupAll(sender)
                    }

                    doubleArgument("size", min = 1.0) {
                        anyExecutor { sender, args ->
                            val size = args["size"] as Double
                            SetupLogic().setupAll(sender, size)
                        }
                    }
                }

                // /yvsmp setup status
                literalArgument("status") {
                    withPermission(Permissions.COMMAND_SETUP_STATUS.permission.name)
                    anyExecutor { sender, _ ->
                        SetupLogic().status(sender)
                    }
                }
            }
        }
    }
}
