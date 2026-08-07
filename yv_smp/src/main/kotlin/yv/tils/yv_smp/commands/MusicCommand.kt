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

import dev.jorel.commandapi.kotlindsl.*
import yv.tils.configv2.language.LanguageHandler
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.logic.music.MusicAPI
import yv.tils.yv_smp.permissions.Permissions

/**
 * Command for managing music playback via Simple Voice Chat.
 * Usage:
 * - /music play <url> - Play audio from URL
 * - /music stop - Stop current audio
 * - /music broadcast <url> - Broadcast audio to all players
 * - /music fade <seconds> - Stop with fade-out
 * - /music status - Check if audio is playing
 */
class MusicCommand {

    init {
        commandTree("music") {
            withPermission(Permissions.COMMAND_MUSIC.permission.name)
            withUsage("/music <play|stop|broadcast|fade|status>")

            // /music play <url>
            literalArgument("play") {
                withPermission(Permissions.COMMAND_MUSIC_PLAY.permission.name)
                greedyStringArgument("url") {
                    playerExecutor { player, args ->
                        val url = args["url"] as String

                        if (!MusicAPI.isAvailable()) {

                            player.sendMessage(
                                LanguageHandler.getMessage(LangStrings.MUSIC_UNAVAILABLE, player)
                            )
                            return@playerExecutor
                        }

                        if (MusicAPI.playUrl(player, url)) {
                            player.sendMessage(
                                LanguageHandler.getMessage(
                                    LangStrings.MUSIC_PLAY_SUCCESS,
                                    player,
                                    mapOf("url" to url)
                                )
                            )
                        } else {
                            player.sendMessage(
                                LanguageHandler.getMessage(LangStrings.MUSIC_PLAY_FAILED, player)
                            )
                        }
                    }
                }
            }

            // /music stop
            literalArgument("stop") {
                withPermission(Permissions.COMMAND_MUSIC_STOP.permission.name)
                playerExecutor { player, _ ->
                    if (MusicAPI.isPlaying(player)) {
                        MusicAPI.stop(player)
                        player.sendMessage(
                            LanguageHandler.getMessage(LangStrings.MUSIC_STOP_SUCCESS, player)
                        )
                    } else {
                        player.sendMessage(
                            LanguageHandler.getMessage(LangStrings.MUSIC_STOP_NOT_PLAYING, player)
                        )
                    }
                }
            }

            // /music broadcast <url>
            literalArgument("broadcast") {
                withPermission(Permissions.COMMAND_MUSIC_BROADCAST.permission.name)
                greedyStringArgument("url") {
                    playerExecutor { player, args ->
                        val url = args["url"] as String

                        if (!MusicAPI.isAvailable()) {
                            player.sendMessage(
                                LanguageHandler.getMessage(LangStrings.MUSIC_UNAVAILABLE, player)
                            )
                            return@playerExecutor
                        }

                        if (MusicAPI.broadcastUrl(url)) {
                            player.sendMessage(
                                LanguageHandler.getMessage(
                                    LangStrings.MUSIC_BROADCAST_SUCCESS,
                                    player,
                                    mapOf("url" to url)
                                )
                            )
                        } else {
                            player.sendMessage(
                                LanguageHandler.getMessage(LangStrings.MUSIC_BROADCAST_FAILED, player)
                            )
                        }
                    }
                }
            }

            // /music fade <seconds>
            literalArgument("fade") {
                withPermission(Permissions.COMMAND_MUSIC_FADE.permission.name)
                integerArgument("seconds", min = 1, max = 10) {
                    playerExecutor { player, args ->
                        val seconds = args["seconds"] as Int
                        val milliseconds = seconds * 1000L

                        if (MusicAPI.isPlaying(player)) {
                            MusicAPI.stop(player, milliseconds)
                            player.sendMessage(
                                LanguageHandler.getMessage(
                                    LangStrings.MUSIC_FADE_SUCCESS,
                                    player,
                                    mapOf("seconds" to seconds.toString())
                                )
                            )
                        } else {
                            player.sendMessage(
                                LanguageHandler.getMessage(LangStrings.MUSIC_STOP_NOT_PLAYING, player)
                            )
                        }
                    }
                }
            }

            // /music status
            literalArgument("status") {
                withPermission(Permissions.COMMAND_MUSIC_STATUS.permission.name)
                playerExecutor { player, _ ->
                    val available = MusicAPI.isAvailable()
                    val playing = MusicAPI.isPlaying(player)

                    player.sendMessage(
                        LanguageHandler.getMessage(LangStrings.MUSIC_STATUS_TITLE, player)
                    )
                    player.sendMessage(
                        LanguageHandler.getMessage(
                            LangStrings.MUSIC_STATUS_AVAILABLE,
                            player,
                            mapOf(
                                "status" to LanguageHandler.getMessage(
                                    if (available) LangStrings.MUSIC_STATUS_YES else LangStrings.MUSIC_STATUS_NO,
                                    player
                                )
                            )
                        )
                    )
                    player.sendMessage(
                        LanguageHandler.getMessage(
                            LangStrings.MUSIC_STATUS_PLAYING,
                            player,
                            mapOf(
                                "status" to LanguageHandler.getMessage(
                                    if (playing) LangStrings.MUSIC_STATUS_YES else LangStrings.MUSIC_STATUS_NO,
                                    player
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}



