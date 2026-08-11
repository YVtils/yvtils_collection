/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.yv_smp.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

/**
 * Configuration file handler for the YV_SMP module.
 *
 * Manages configuration values for:
 * - music.master_volume: Master volume for all audio playback (0.0 - 1.0)
 * - music.enabled: Whether music system is enabled
 * - start.warmup_seconds: Countdown broadcast before /yvsmp start begins moving players
 * - start.min_players: Minimum online players required to run /yvsmp start
 */
class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: YVSMPConfigState = YVSMPConfigState()

        /**
         * Get the master volume for audio playback.
         * @return Volume level between 0.0 (mute) and 1.0 (full volume), default 0.5
         */
        fun getMasterVolume(): Float {
            return state.music.master_volume.toFloat().coerceIn(0.0f, 1.0f)
        }

        /**
         * Check if music system is enabled.
         * @return true if enabled, false otherwise
         */
        fun isMusicEnabled(): Boolean = state.music.enabled

        /**
         * Get the warmup countdown duration for `/yvsmp start`, in seconds.
         * @return Seconds to count down before the cinematic sequence begins, 0 for no warmup.
         */
        fun getWarmupSeconds(): Long = state.start.warmup_seconds.coerceAtLeast(0)

        /**
         * Get the minimum number of online players required to run `/yvsmp start`.
         * @return Minimum player count, at least 0.
         */
        fun getMinPlayers(): Int = state.start.min_players.coerceAtLeast(0)
    }

    private val filePath = "/yv_smp/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, YVSMPConfigState(), format = ConfigFormat.YAML)
        registerStrings()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }
}
