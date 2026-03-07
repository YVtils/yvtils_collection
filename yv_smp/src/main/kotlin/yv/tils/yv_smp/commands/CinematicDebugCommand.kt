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
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import yv.tils.yv_smp.logic.start.*
import yv.tils.yv_smp.utils.CinematicCamera
import yv.tils.yv_smp.utils.IslandScanner
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeGlow
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgePillarRise
import yv.tils.yv_smp.utils.ParticleEffects.Companion.edgeShockwaveInward

/**
 * /cinematicdebug — developer command for in-game testing of the island
 * scanner and phase cinematics without running the full start sequence.
 *
 * /cinematicdebug phase <name>
 *      Calls cinematicEffects() on the real phase class directly.
 *      Names: preparation | heal | itemclear | deathvisual | border | final | final2
 *
 * /cinematicdebug scan [radius]        — debug ray scan from world spawn
 * /cinematicdebug scanhere [radius]    — debug ray scan from player position
 * /cinematicdebug clearcache           — clear IslandScanner cache
 * /cinematicdebug info                 — show spawn/surface block info
 * /cinematicdebug outline [seconds]    — show edge outline
 * /cinematicdebug edgepulse            — fire one edge pulse
 * /cinematicdebug edgeglow [seconds]   — show two-layer edge glow
 * /cinematicdebug edgepillar           — fire edge pillar rise
 * /cinematicdebug edgeshockwave        — fire inward edge shockwave
 * /cinematicdebug camera orbitlow      — low orbit camera (20 s)
 * /cinematicdebug camera orbithigh     — high orbit camera (20 s)
 * /cinematicdebug camera dollyin       — dolly-in camera (20 s)
 * /cinematicdebug stop                 — stop all camera tasks
 */
class CinematicDebugCommand {
    val command = commandTree("cinematicdebug") {
        withPermission(CommandPermission.OP)
        withUsage("/cinematicdebug phase <name> | scan | clearcache | info | outline | edgepulse | edgeglow | edgepillar | edgeshockwave | camera | stop")

        // ── phase <name> ─────────────────────────────────────────────────────
        literalArgument("phase") {
            stringArgument("name") {
                replaceSuggestions(ArgumentSuggestions.strings(
                    "preparation", "heal", "itemclear", "deathvisual", "border", "final", "final2"
                ))
                playerExecutor { player, args ->
                    val name = (args[0] as String).lowercase()
                    val phase: PhasePlugin? = when (name) {
                        "preparation" -> PreparationPhase()
                        "heal"        -> HealPhase()
                        "itemclear"   -> ItemClearPhase()
                        "deathvisual" -> DeathVisualizerPhase()
                        "border"      -> BorderPhase()
                        "final"       -> FinalPhase()
                        "final2"      -> FinalPhasePartTwo()
                        else          -> null
                    }
                    if (phase == null) {
                        player.sendMessage(msg("<red>Unknown phase '$name'. Valid: preparation | heal | itemclear | deathvisual | border | final | final2"))
                        return@playerExecutor
                    }
                    val players = listOf(player)
                    val savedMode = player.gameMode
                    player.sendMessage(msg("<gold>Running <white>$name</white> cinematicEffects…"))
                    Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                        phase.cinematicEffects(players)
                        // Restore game mode after the phase's own declared duration
                        Bukkit.getScheduler().runTaskLater(yv.tils.utils.data.Data.instance, Runnable {
                            if (player.gameMode == org.bukkit.GameMode.SPECTATOR) {
                                player.gameMode = savedMode
                            }
                            player.sendMessage(msg("<green><white>$name</white> finished."))
                        }, phase.getPhaseDuration() + 5L)
                    })
                }
            }
        }

        // ── scan ─────────────────────────────────────────────────────────────
        literalArgument("scan") {
            playerExecutor { player, _ ->
                val spawn = player.world.spawnLocation
                player.sendMessage(msg("<gold>Scanning from world spawn (${spawn.blockX}, ${spawn.blockZ})…"))
                Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                    IslandScanner.debugScan(spawn, player)
                })
            }
            integerArgument("radius", 10, 200) {
                playerExecutor { player, args ->
                    val radius = args[0] as Int
                    val spawn = player.world.spawnLocation
                    player.sendMessage(msg("<gold>Scanning from world spawn radius=$radius…"))
                    Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                        IslandScanner.debugScan(spawn, player, scanRadius = radius)
                    })
                }
            }
        }

        // ── scanhere ─────────────────────────────────────────────────────────
        literalArgument("scanhere") {
            playerExecutor { player, _ ->
                val pos = player.location
                player.sendMessage(msg("<gold>Scanning from YOUR position (${pos.blockX}, ${pos.blockZ})…"))
                Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                    IslandScanner.debugScan(pos, player)
                })
            }
            integerArgument("radius", 10, 200) {
                playerExecutor { player, args ->
                    val radius = args[0] as Int
                    val pos = player.location
                    player.sendMessage(msg("<gold>Scanning from YOUR position radius=$radius…"))
                    Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                        IslandScanner.debugScan(pos, player, scanRadius = radius)
                    })
                }
            }
        }

        // ── clearcache ───────────────────────────────────────────────────────
        literalArgument("clearcache") {
            playerExecutor { player, _ ->
                IslandScanner.clearCache()
                player.sendMessage(msg("<green>IslandScanner cache cleared."))
            }
        }

        // ── info ─────────────────────────────────────────────────────────────
        literalArgument("info") {
            playerExecutor { player, _ ->
                val mm    = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                val spawn = player.world.spawnLocation
                val world = player.world
                val topY  = world.getHighestBlockYAt(spawn.blockX, spawn.blockZ, org.bukkit.HeightMap.WORLD_SURFACE)
                val block = world.getBlockAt(spawn.blockX, topY, spawn.blockZ)
                player.sendMessage(mm.deserialize("<gold>World spawn: <white>${spawn.blockX}, ${spawn.blockY}, ${spawn.blockZ}"))
                player.sendMessage(mm.deserialize("<gold>Highest surface block at spawn: <white>${block.type} <gray>@ y=$topY"))
                player.sendMessage(mm.deserialize("<gold>Your position: <white>${player.location.blockX}, ${player.location.blockY}, ${player.location.blockZ}"))
                val myTopY  = world.getHighestBlockYAt(player.location.blockX, player.location.blockZ, org.bukkit.HeightMap.WORLD_SURFACE)
                val myBlock = world.getBlockAt(player.location.blockX, myTopY, player.location.blockZ)
                player.sendMessage(mm.deserialize("<gold>Highest surface block at YOUR pos: <white>${myBlock.type} <gray>@ y=$myTopY"))
            }
        }

        // ── outline ──────────────────────────────────────────────────────────
        literalArgument("outline") {
            playerExecutor { player, _ -> runOutline(player, 10) }
            integerArgument("seconds", 1, 120) {
                playerExecutor { player, args -> runOutline(player, args[0] as Int) }
            }
        }

        // ── edgepulse ────────────────────────────────────────────────────────
        literalArgument("edgepulse") {
            playerExecutor { player, _ ->
                val spawn = player.world.spawnLocation
                player.sendMessage(msg("<gold>Firing edge pulse from world spawn…"))
                IslandScanner.edgePulse(spawn, listOf(player), Particle.END_ROD, pulseHeight = 25.0)
            }
        }

        // ── edgeglow ─────────────────────────────────────────────────────────
        literalArgument("edgeglow") {
            playerExecutor { player, _ -> runEdgeGlow(player, 10) }
            integerArgument("seconds", 1, 120) {
                playerExecutor { player, args -> runEdgeGlow(player, args[0] as Int) }
            }
        }

        // ── edgepillar ───────────────────────────────────────────────────────
        literalArgument("edgepillar") {
            playerExecutor { player, _ ->
                val spawn = player.world.spawnLocation
                player.sendMessage(msg("<gold>Firing edge pillar rise from world spawn…"))
                Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                    val edges = IslandScanner.scan(spawn)
                    player.sendMessage(msg("<gold>Edge pillar — <white>${edges.size}</white> edge blocks."))
                    edgePillarRise(listOf(player), edges, Particle.END_ROD, riseHeight = 45, count = 2)
                })
            }
        }

        // ── edgeshockwave ────────────────────────────────────────────────────
        literalArgument("edgeshockwave") {
            playerExecutor { player, _ ->
                val spawn = player.world.spawnLocation
                player.sendMessage(msg("<gold>Firing inward edge shockwave from world spawn…"))
                Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
                    val edges = IslandScanner.scan(spawn)
                    player.sendMessage(msg("<gold>Edge shockwave — <white>${edges.size}</white> edge blocks."))
                    edgeShockwaveInward(listOf(player), edges, spawn,
                        Pair(Particle.ELECTRIC_SPARK, Particle.GLOW), waves = 3, waveDelay = 18L, steps = 22)
                })
            }
        }

        // ── camera ───────────────────────────────────────────────────────────
        literalArgument("camera") {
            literalArgument("orbitlow") {
                playerExecutor { player, _ -> runCamera(player, "orbitlow") }
            }
            literalArgument("orbithigh") {
                playerExecutor { player, _ -> runCamera(player, "orbithigh") }
            }
            literalArgument("dollyin") {
                playerExecutor { player, _ -> runCamera(player, "dollyin") }
            }
            literalArgument("spiraldown") {
                playerExecutor { player, _ -> runCamera(player, "spiraldown") }
            }
            literalArgument("orbitrising") {
                playerExecutor { player, _ -> runCamera(player, "orbitrising") }
            }
            literalArgument("pendulum") {
                playerExecutor { player, _ -> runCamera(player, "pendulum") }
            }
        }

        // ── stop ─────────────────────────────────────────────────────────────
        literalArgument("stop") {
            playerExecutor { player, _ ->
                CinematicCamera.stopAll()
                player.sendMessage(msg("<green>All cinematic camera tasks stopped."))
            }
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun runOutline(player: Player, seconds: Int) {
        val spawn = player.world.spawnLocation
        val durationTicks = seconds * 20L
        player.sendMessage(msg("<gold>Showing island outline for ${seconds}s from world spawn (${spawn.blockX},${spawn.blockZ})…"))
        Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
            IslandScanner.outlineGlowing(
                center         = spawn,
                players        = listOf(player),
                particle       = Particle.END_ROD,
                duration       = durationTicks,
                repeatInterval = 2L,
                forceRescan    = true,
            )
            val count = IslandScanner.scan(spawn).size
            player.sendMessage(msg("<gold>Outline active — <white>$count</white> edge points."))
        })
    }

    private fun runEdgeGlow(player: Player, seconds: Int) {
        val spawn         = player.world.spawnLocation
        val durationTicks = seconds * 20L
        player.sendMessage(msg("<gold>Showing edge glow for ${seconds}s from world spawn (${spawn.blockX},${spawn.blockZ})…"))
        Bukkit.getScheduler().runTask(yv.tils.utils.data.Data.instance, Runnable {
            val edges = IslandScanner.scan(spawn)
            if (edges.isEmpty()) {
                player.sendMessage(msg("<red>No edges found — is world spawn on the island?"))
                return@Runnable
            }
            player.sendMessage(msg("<gold>Edge glow active — <white>${edges.size}</white> edge blocks."))
            edgeGlow(listOf(player), edges, Particle.GLOW, Particle.END_ROD,
                stackHeight = 0.7, duration = durationTicks)
        })
    }

    private fun runCamera(player: Player, mode: String) {
        if (player.gameMode != org.bukkit.GameMode.SPECTATOR) {
            player.sendMessage(msg("<red>You must be in spectator mode to use the cinematic camera. (/gamemode spectator)"))
            return
        }
        val spawn   = player.world.spawnLocation
        val players = listOf(player)
        val ticks   = 400L
        when (mode) {
            "orbitlow"   -> { player.sendMessage(msg("<gold>Starting low orbit…"));    CinematicCamera.orbitLow(players, spawn, durationTicks = ticks) }
            "orbithigh"  -> { player.sendMessage(msg("<gold>Starting high orbit…"));   CinematicCamera.orbitHigh(players, spawn, durationTicks = ticks) }
            "dollyin"    -> { player.sendMessage(msg("<gold>Starting dolly-in…"));     CinematicCamera.dollyIn(players, spawn, durationTicks = ticks) }
            "spiraldown" -> { player.sendMessage(msg("<gold>Starting spiral down…"));  CinematicCamera.spiralDown(players, spawn, durationTicks = ticks) }
            "orbitrising"-> { player.sendMessage(msg("<gold>Starting orbit rising…")); CinematicCamera.orbitRising(players, spawn, durationTicks = ticks) }
            "pendulum"   -> { player.sendMessage(msg("<gold>Starting pendulum…"));     CinematicCamera.pendulum(players, spawn, durationTicks = ticks) }
        }
    }

    private fun msg(text: String): Component =
        net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
            "<dark_gray>[<aqua>CinematicDebug<dark_gray>] <white>$text"
        )
}


